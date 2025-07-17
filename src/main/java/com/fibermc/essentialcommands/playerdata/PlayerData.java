package com.fibermc.essentialcommands.playerdata;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.fibermc.essentialcommands.ECAbilitySources;
import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.codec.Codecs;
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
import me.drex.vanish.api.VanishAPI;
import org.jetbrains.annotations.NotNull;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
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
    private File saveFile;

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
    private boolean isSleepingFromCommand;

    public PlayerData(ServerPlayerEntity player, File saveFile) {
        incomingTeleportRequests = new LinkedHashMap<>();
        homes = new NamedLocationStorage();
        initializeRuntimeState(player, saveFile);
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
     * @param saveFile   The save file for this PlayerData instance.
     */
    public PlayerData(File saveFile) {
        this.saveFile = saveFile;
        incomingTeleportRequests = new LinkedHashMap<>();
        homes = new NamedLocationStorage();
    }

    // ONLY TO BE USED WITH CODECS
    private PlayerData() {
        this.saveFile = null; // must be set by factory
        this.incomingTeleportRequests = new LinkedHashMap<>();
    }

    public static PlayerData createWithData(
        NamedLocationStorage homes,
        Optional<MinecraftLocation> previousLocation,
        Optional<Text> nickname,
        long timeUsedRtpEpochMs,
        int tpCooldown
    ) {
        // This creates a PlayerData with serializable state only
        // Runtime state will be initialized by your factory methods
        PlayerData pd = new PlayerData();
        pd.homes = homes;
        pd.previousLocation = previousLocation.orElse(null);
        pd.nickname = nickname.orElse(null);
        pd.timeUsedRtp = TimeUtil.epochTimeMsToTicks(timeUsedRtpEpochMs);
        pd.tpCooldown = tpCooldown;
        return pd;
    }

    public void initializeSaveFileField(File saveFile)
    {
        this.saveFile = saveFile;
    }
    // Initialize runtime state after deserialization
    public void initializeRuntimeState(ServerPlayerEntity player, File saveFile) {
        initializeSaveFileField(saveFile);
        this.player = player;
        this.lastTickPos = player.getPos();
        this.lastActionTick = player.getServer().getTicks();
        this.pUuid = player.getUuid();

        // Re-register events
        playerActEvent.register((packet) -> {
            updateLastActionTick();
            setAfk(false);
        });

        // Recalculate derived state
        if (this.nickname != null) {
            try {
                reloadFullNickname();
            } catch (NullPointerException ignore) {
                EssentialCommands.LOGGER.warn("Could not refresh player full nickname, as ServerPlayerEntity was null in PlayerData.");
            }
        }

        // Revoke any abilities that shouldn't persist
        Pal.revokeAbility(player, VanillaAbilities.INVULNERABLE, ECAbilitySources.SLEEP_INVULN);

        updatePlayerEntity(player);
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

            if (!EssentialCommands.VANISH_PRESENT || !VanishAPI.isVanished(player)) {
                this.player.getServer().getPlayerManager().broadcast(
                    ECText.getInstance().getText(
                        "player.afk.enter",
                        this.player.getDisplayName()),
                    false);
            }

            // This assignment should happen after the message, otherwise
            // `getDisplayName` will include the `[AFK]` prefix.
            this.afk = true;
        } else {
            // This assignment should happen before the message, otherwise
            // `getDisplayName` will include the `[AFK]` prefix.
            this.afk = false;

            Pal.revokeAbility(this.player, VanillaAbilities.INVULNERABLE, ECAbilitySources.AFK_INVULN);

            if (!EssentialCommands.VANISH_PRESENT || !VanishAPI.isVanished(player)) {
                this.player.getServer().getPlayerManager().broadcast(
                    ECText.getInstance().getText(
                        "player.afk.exit",
                        this.player.getDisplayName()),
                    false);
            }
        }

        PlayerDataManager.getInstance().markNicknameDirty(this);
    }

    public boolean isAfk() {
        return afk;
    }

    public void onTickEnd() {
        var ticks = player.getServer().getTicks();
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
        return player.getServer().getTicks() - Math.max(lastMovedTick, lastActionTick);
    }

    public void updateLastActionTick() {
        this.lastActionTick = player.getServer().getTicks();
    }

    public boolean isSleepingFromCommand() {
        return isSleepingFromCommand;
    }

    public void setIsSleepingFromCommand(boolean sleepingFromCommand) {
        this.isSleepingFromCommand = sleepingFromCommand;
        if (CONFIG.SLEEP_INVULN && sleepingFromCommand) {
            Pal.grantAbility(player, VanillaAbilities.INVULNERABLE, ECAbilitySources.SLEEP_INVULN);
        }
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
        boolean couldFly = VanillaAbilities.ALLOW_FLYING.getTracker(this.player).isGrantedBy(ECAbilitySources.FLY_COMMAND);
        this.player = serverPlayerEntity;
        setFlight(couldFly);
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

        // This is to fix a bug with ability to fly being lost upon being teleported to a new dim via /execute...tp.
        PlayerDataManager.getInstance().scheduleTask(this::updateFlight);
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
            EssentialCommands.LOGGER.info(
                "Cleared {}'s nickname",
                this.player.getGameProfile().getName()
            );
        } else {
            // Ensure nickname does not exceed max length
            if (nickname.getString().length() > CONFIG.NICKNAME_MAX_LENGTH) {
                return -2;
            }
            // Ensure player has permissions required to set the specified nickname
            boolean hasRequiredPerms = NicknameTextUtil.checkPerms(nickname, this.player.getCommandSource());
            if (!hasRequiredPerms) {
                EssentialCommands.LOGGER.info(
                    "{} attempted to set nickname to '{}', with insufficient permissions to do so.",
                    this.player.getGameProfile().getName(),
                    nickname
                );
                return -1;
            } else {
                EssentialCommands.LOGGER.info(
                    "Set {}'s nickname to '{}'.",
                    this.player.getGameProfile().getName(),
                    nickname
                );
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
        NbtCompound data = CODEC.encodeStart(NbtOps.INSTANCE, this)
            .getOrThrow().asCompound().orElseThrow();

        try {
            NbtIo.writeCompressed(data, this.saveFile.toPath());
        } catch (IOException e) {
            EssentialCommands.LOGGER.error("Could not save data {}", this, e);
        }
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
                new HoverEvent.ShowText(baseName)
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

    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            // Homes storage
            NamedLocationStorage.CODEC
                .optionalFieldOf("homes", new NamedLocationStorage())
                .forGetter(pd -> pd.homes),

            // Previous location for /back
            Codecs.MINECRAFT_LOCATION
                .optionalFieldOf("previousLocation")
                .forGetter(pd -> Optional.ofNullable(pd.previousLocation)),

            // Nickname
            TextCodecs.CODEC
                .optionalFieldOf("nickname")
                .forGetter(pd -> Optional.ofNullable(pd.nickname)),

            // RTP time (stored as epoch ms for format compatibility)
            Codec.LONG
                .optionalFieldOf("timeUsedRtpEpochMs", 0L)
                .forGetter(pd -> TimeUtil.tickTimeToEpochMs(pd.timeUsedRtp)),

            // TP cooldown
            Codec.INT
                .optionalFieldOf("tpCooldown", 0)
                .forGetter(pd -> pd.tpCooldown)

        ).apply(instance, PlayerData::createWithData)
    );
}
