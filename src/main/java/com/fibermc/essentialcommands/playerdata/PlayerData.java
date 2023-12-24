package com.fibermc.essentialcommands.playerdata;

import java.io.File;
import java.util.*;

import com.fibermc.essentialcommands.ECAbilitySources;
import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.commands.InvulnCommand;
import com.fibermc.essentialcommands.commands.helpers.IFeedbackReceiver;
import com.fibermc.essentialcommands.events.PlayerActCallback;
import com.fibermc.essentialcommands.teleportation.OutgoingTeleportRequests;
import com.fibermc.essentialcommands.teleportation.TeleportRequest;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.types.NamedLocationStorage;
import com.fibermc.essentialcommands.types.NamedMinecraftLocation;
import com.fibermc.essentialcommands.util.NicknameTextUtil;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import org.jetbrains.annotations.NotNull;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import dev.jpcode.eccore.util.TextUtil;
import dev.jpcode.eccore.util.TimeUtil;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class PlayerData extends PersistentState implements IServerPlayerEntityData, IFeedbackReceiver {

    // ServerPlayerEntity
    private ServerPlayerEntity player;
    private UUID pUuid;
    private final File saveFile;

    // Target of tpAsk
    private final OutgoingTeleportRequests outgoingTeleportRequests = new OutgoingTeleportRequests();

    // players that have asked to teleport to this player
    // This list exists for autofilling the 'tpaccept' command
    private final LinkedHashMap<UUID, TeleportRequest> incomingTeleportRequests;

    // HOMES
    NamedLocationStorage homes;
    private MinecraftLocation previousLocation;
    private int tpCooldown;

    // Nickname
    private Text nickname;
    private MutableText fullNickname;

    // RTP Cooldown
    private int timeUsedRtp;

    private boolean afk;
    private Vec3d lastTickPos;
    private boolean isInCombat;
    private int lastActionTick;
    private int lastMovedTick;
    private boolean hasMovedThisTick;

    public PlayerData(ServerPlayerEntity player, File saveFile) {
        this.player = player;
        this.lastTickPos = player.getPos();
        this.lastActionTick = player.server.getTicks();
        this.pUuid = player.getUuid();
        this.saveFile = saveFile;
        incomingTeleportRequests = new LinkedHashMap<>();
        homes = new NamedLocationStorage();
        playerActEvent.register((packet) -> {
            updateLastActionTick();
            setAfk(false);
        });
    }

    /**
     * DO NOT USE FOR LOGGED-IN PLAYERS.
     * This constructor should ONLY be used for temporarily
     * handling data of offline players.
     *
     * <p>
     * getPlayer() will always return null on an instance created in this fashion,
     * and any operations that would require a ServerPlayerEntity will fail.
     * </p>
     *
     * @param playerUuid UUID of the player whose data we want to grab or modify.
     * @param saveFile   The save file for this PlayerData instance.
     */
    public PlayerData(UUID playerUuid, File saveFile) {
        this.pUuid = playerUuid;
        this.saveFile = saveFile;
        incomingTeleportRequests = new LinkedHashMap<>();
        homes = new NamedLocationStorage();
    }

    public OutgoingTeleportRequests getSentTeleportRequests() {
        return outgoingTeleportRequests;
    }

    public void addSentTeleportRequest(TeleportRequest request) {
        this.outgoingTeleportRequests.add(request);
    }

    public void removeSentTeleportRequest(TeleportRequest request) {
        this.outgoingTeleportRequests.remove(request);
    }

    public LinkedHashMap<UUID, TeleportRequest> getIncomingTeleportRequests() {
        return incomingTeleportRequests;
    }

    public TeleportRequest getIncomingTeleportRequest(UUID tpAsker) {
        return incomingTeleportRequests.get(tpAsker);
    }

    public void addIncomingTeleportRequest(TeleportRequest teleportRequest) {
        this.incomingTeleportRequests.put(teleportRequest.getSenderPlayer().getUuid(), teleportRequest);
    }

    public void removeIncomingTeleportRequest(UUID tpAsker) {
        this.incomingTeleportRequests.remove(tpAsker);
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    // Homes
    public void addHome(String homeName, MinecraftLocation minecraftLocation) throws CommandSyntaxException {
        int playerMaxHomes = ECPerms.getHighestNumericPermission(this.player.getCommandSource(), ECPerms.Registry.Group.home_limit_group);
        if (this.homes.size() < playerMaxHomes) {
            homes.putCommand(homeName, minecraftLocation);
            this.markDirty();
        } else {
            var ecText = ECText.access(this.player);
            var homeNameText = ecText.accent(homeName);
            var maxHomesText = ecText.accent(String.valueOf(playerMaxHomes));
            throw CommandUtil.createSimpleException(ecText.getText(
                "cmd.home.set.error.limit",
                TextFormatType.Error,
                homeNameText,
                maxHomesText));
        }
    }

    public boolean existsHome(String homeName) {
        return homes.containsKey(homeName);
    }

    public void sendCommandFeedback(Text text) {
        this.player.getCommandSource().sendFeedback(() -> text, CONFIG.BROADCAST_TO_OPS);
    }

    public void sendCommandFeedback(String messageKey, Text... args) {
        sendCommandFeedback(ECText.access(this.player).getText(messageKey, TextFormatType.Default, args));
    }

    public void sendCommandError(Text text) {
        this.player.getCommandSource().sendError(text);
    }

    public void sendCommandError(String messageKey, Text... args) {
        sendCommandError(ECText.access(this.player).getText(messageKey, TextFormatType.Error, args));
    }

    public void sendMessage(String messageKey, Text... args) {
        this.player.sendMessage(ECText.access(this.player).getText(messageKey, TextFormatType.Default, args));
    }

    public void sendError(String messageKey, Text... args) {
        this.player.sendMessage(ECText.access(this.player).getText(messageKey, TextFormatType.Error, args));
    }

    public Set<String> getHomeNames() {
        return homes.keySet();
    }

    public Set<Map.Entry<String, NamedMinecraftLocation>> getHomeEntries() {
        return homes.entrySet();
    }

    public NamedMinecraftLocation getHomeLocation(String homeName) {
        return homes.get(homeName);
    }

    public final Event<PlayerActCallback> playerActEvent = EventFactory.createArrayBacked(
        PlayerActCallback.class,
        (listeners) -> (packet) -> {
            for (PlayerActCallback event : listeners) {
                event.onPlayerAct(packet);
            }
        });

    public void setAfk(boolean afk) {
        if (this.afk == afk) {
            return;
        }

        if (afk) {
            if (!CONFIG.ENABLE_AFK) {
                return;
            }
            if (CONFIG.INVULN_WHILE_AFK) {
                Pal.grantAbility(this.player, VanillaAbilities.INVULNERABLE, ECAbilitySources.AFK_INVULN);
            }

            this.player.server.getPlayerManager().broadcast(
                ECText.getInstance().getText(
                    "player.afk.enter",
                    this.player.getDisplayName()),
                false);

            // This assignment should happen after the message, otherwise
            // `getDisplayName` will include the `[AFK]` prefix.
            this.afk = true;
        } else {
            // This assignment should happen before the message, otherwise
            // `getDisplayName` will include the `[AFK]` prefix.
            this.afk = false;

            Pal.revokeAbility(this.player, VanillaAbilities.INVULNERABLE, ECAbilitySources.AFK_INVULN);

            this.player.server.getPlayerManager().broadcast(
                ECText.getInstance().getText(
                    "player.afk.exit",
                    this.player.getDisplayName()),
                false);
        }

        PlayerDataManager.getInstance().markNicknameDirty(this);
    }

    public boolean isAfk() {
        return afk;
    }

    public void onTickEnd() {
        var ticks = player.server.getTicks();
        var currentPos = player.getPos();
        hasMovedThisTick = !this.lastTickPos.equals(currentPos);
        if (hasMovedThisTick) {
            lastMovedTick = ticks;
        }

        if (this.afk) {
            if (CONFIG.INVULN_WHILE_AFK) {
                player.requestTeleport(lastTickPos.x, lastTickPos.y, lastTickPos.z);
            } else if (hasMovedThisTick) {
                this.setAfk(false);
            }

        } else if (
            CONFIG.AUTO_AFK_ENABLED
                && (ticks - Math.max(lastMovedTick, lastActionTick)) > CONFIG.AUTO_AFK_TICKS
        ) {
            this.setAfk(true);
        }

        lastTickPos = player.getPos();
    }

    public Vec3d getLastTickPos() {
        return lastTickPos;
    }

    public boolean isInCombat() {
        return isInCombat;
    }

    public void setInCombat(boolean inCombat) {
        isInCombat = inCombat;
    }

    public boolean hasMovedThisTick() {
        return this.hasMovedThisTick;
    }

    public double distanceMovedThisTick() {
        return this.lastTickPos.distanceTo(this.player.getPos());
    }

    public int getLastActionTick() {
        return lastActionTick;
    }

    public int ticksSinceLastActionOrMove() {
        return player.server.getTicks() - Math.max(lastMovedTick, lastActionTick);
    }

    public void updateLastActionTick() {
        this.lastActionTick = player.server.getTicks();
    }

    private static final class StorageKey {
        static final String PLAYER_UUID = "playerUuid";
        static final String HOMES = "homes";
        static final String NICKNAME = "nickname";
        static final String TIME_USED_RTP_EPOCH_MS = "timeUsedRtpEpochMs";
        static final String PREVIOUS_LOCATION = "previousLocation";
    }

    public void fromNbt(NbtCompound tag) {
        NbtCompound dataTag = tag.getCompound("data");
        this.pUuid = dataTag.getUuid(StorageKey.PLAYER_UUID);

        NamedLocationStorage homes = new NamedLocationStorage();
        NbtElement homesTag = dataTag.get(StorageKey.HOMES);
        if (homesTag != null) {
            homes.loadNbt(homesTag);
        }
        this.homes = homes;

        if (dataTag.contains(StorageKey.NICKNAME)) {
            String nick = dataTag.getString(StorageKey.NICKNAME);
            if (!Objects.equals(nick, "null")) {
                this.nickname = Text.Serialization.fromJson(nick);
                try {
                    reloadFullNickname();
                } catch (NullPointerException ignore) {
                    EssentialCommands.LOGGER.warn("Could not refresh player full nickanme, as ServerPlayerEntity was null in PlayerData.");
                }
            }
        }

        if (dataTag.contains(StorageKey.TIME_USED_RTP_EPOCH_MS)) {
            this.timeUsedRtp = TimeUtil.epochTimeMsToTicks(dataTag.getLong(StorageKey.TIME_USED_RTP_EPOCH_MS));
        }

        if (CONFIG.PERSIST_BACK_LOCATION && dataTag.contains(StorageKey.PREVIOUS_LOCATION)) {
            this.previousLocation = MinecraftLocation.fromNbt(dataTag.getCompound(StorageKey.PREVIOUS_LOCATION));
        }

        if (this.player != null) {
            updatePlayerEntity(this.player);
        }

    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putUuid(StorageKey.PLAYER_UUID, pUuid);

        NbtCompound homesNbt = new NbtCompound();
        homes.writeNbt(homesNbt);
        tag.put(StorageKey.HOMES, homesNbt);

        if (nickname != null) {
            tag.putString(StorageKey.NICKNAME, Text.Serialization.toJsonString(nickname));
        }

        tag.putLong(StorageKey.TIME_USED_RTP_EPOCH_MS, TimeUtil.tickTimeToEpochMs(timeUsedRtp));

        if (CONFIG.PERSIST_BACK_LOCATION && previousLocation != null) {
            tag.put(StorageKey.PREVIOUS_LOCATION, previousLocation.asNbt());
        }

        return tag;
    }

    public void setPreviousLocation(MinecraftLocation location) {
        this.previousLocation = location;
        this.markDirty();
    }

    public MinecraftLocation getPreviousLocation() {
        return this.previousLocation;
    }

    /**
     * Removes a home & persists changes to storage. Permanent.
     *
     * @param homeName The name of the home to remove.
     * @return `true` if a home was successfully removed. `false` if there was
     * no home with the specified name.
     */
    public boolean removeHome(String homeName) {
        MinecraftLocation old = this.homes.remove(homeName);
        if (old != null) {
            this.markDirty();
            return true;
        }
        return false;
    }

    @Override
    public void updatePlayerEntity(ServerPlayerEntity serverPlayerEntity) {
        this.player = serverPlayerEntity;

        // This is to fix a bug with ability to fly being lost upon being teleported to a new dim via /execute...tp.
        PlayerDataManager.getInstance().scheduleTask(this::updateFlight);
    }

    private void updateFlight() {
        this.player.sendAbilitiesUpdate();
    }

    public void setFlight(boolean canFly) {
        setFlight(canFly, false);
    }

    public void setFlight(boolean canFly, boolean flyImmediately) {
        PlayerAbilities abilities = this.player.getAbilities();
        if (canFly) {
            Pal.grantAbility(this.player, VanillaAbilities.ALLOW_FLYING, ECAbilitySources.FLY_COMMAND);
            if (flyImmediately) {
                abilities.flying = true;
            }
        } else {
            Pal.revokeAbility(this.player, VanillaAbilities.ALLOW_FLYING, ECAbilitySources.FLY_COMMAND);
        }
        this.player.sendAbilitiesUpdate();
    }

    public void clearAbilitiesWithoutPermisisons() {
        var grantedAbilityPerms = ECPerms.getGrantedStatefulPlayerAbilityPermissions(this.player).toList();

        var flyPermisisons = List.of(ECPerms.Registry.Group.fly_group);
        if (grantedAbilityPerms.stream().noneMatch(flyPermisisons::contains)) {
            setFlight(false);
        }

        var invulnPermissions = List.of(ECPerms.Registry.Group.invuln_group);
        if (grantedAbilityPerms.stream().noneMatch(invulnPermissions::contains)) {
            InvulnCommand.exec(this.player, false);
        }
    }

    public void tickTpCooldown() {
        this.tpCooldown--;
    }

    public int getTpCooldown() {
        return tpCooldown;
    }

    public void setTpCooldown(int cooldown) {
        this.tpCooldown = cooldown;
    }

    public Optional<MutableText> getNickname() {
        return Optional.ofNullable(nickname != null ? nickname.copy() : null);
    }

    public MutableText getFullNickname() {
        return fullNickname;
    }

    public MutableText copyFullNickname() {
        return fullNickname != null ? TextUtil.deepCopy(fullNickname) : null;
    }

    public int setNickname(Text nickname) {
        int resultCode = 0;
        // Reset nickname
        if (nickname == null) {
            this.nickname = null;
            resultCode = 1;
            EssentialCommands.LOGGER.info(String.format(
                "Cleared %s's nickname",
                this.player.getGameProfile().getName()
            ));
        } else {
            // Ensure nickname does not exceed max length
            if (nickname.getString().length() > CONFIG.NICKNAME_MAX_LENGTH) {
                return -2;
            }
            // Ensure player has permissions required to set the specified nickname
            boolean hasRequiredPerms = NicknameTextUtil.checkPerms(nickname, this.player.getCommandSource());
            if (!hasRequiredPerms) {
                EssentialCommands.LOGGER.info(String.format(
                    "%s attempted to set nickname to '%s', with insufficient permissions to do so.",
                    this.player.getGameProfile().getName(),
                    nickname
                ));
                return -1;
            } else {
                EssentialCommands.LOGGER.info(String.format(
                    "Set %s's nickname to '%s'.",
                    this.player.getGameProfile().getName(),
                    nickname
                ));
            }

            // Set nickname
            this.nickname = nickname;
        }

        reloadFullNickname();
        PlayerDataManager.getInstance().markNicknameDirty(this);
        this.markDirty();
        // Return codes based on fail/success
        //  ex: caused by profanity filter.
        return resultCode;
    }

    public void save() {
        super.save(saveFile);
    }

    public void setTimeUsedRtp(int i) {
        this.timeUsedRtp = i;
        this.markDirty();
    }

    public int getTimeUsedRtp() {
        return timeUsedRtp;
    }

    private void reloadFullNickname() {
        MutableText baseName = Text.literal(this.getPlayer().getGameProfile().getName());
        MutableText tempFullNickname = Text.empty();
        // Note: this doesn't ever display if nickname is null,
        //  because our mixin to getDisplayName does a null check on getNickname
        if (this.nickname != null) {
            tempFullNickname
                .append(CONFIG.NICKNAME_PREFIX)
                .append(this.nickname);
        } else {
            tempFullNickname
                .append(baseName);
        }

        if (CONFIG.NICK_REVEAL_ON_HOVER) {
            tempFullNickname.setStyle(tempFullNickname.getStyle().withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, baseName)
            ));
        }

        this.fullNickname = tempFullNickname;
    }

    public static PlayerData access(@NotNull ServerPlayerEntity player) {
        return ((ServerPlayerEntityAccess) player).ec$getPlayerData();
    }

    public static PlayerData accessFromContextOrThrow(CommandContext<ServerCommandSource> context)
        throws CommandSyntaxException
    {
        return access(context.getSource().getPlayerOrThrow());
    }
}
