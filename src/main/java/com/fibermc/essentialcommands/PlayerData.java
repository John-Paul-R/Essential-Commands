package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.events.PlayerActCallback;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.types.NamedLocationStorage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jpcode.eccore.util.TextUtil;
import dev.jpcode.eccore.util.TimeUtil;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;

import java.io.File;
import java.util.*;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class PlayerData extends PersistentState {

    // ServerPlayerEntity
    private ServerPlayerEntity player;
    private UUID pUuid;
    private final File saveFile;

    // TELEPORT
    // Remaining time before current tp Ask (sent by this player) expires.
    private int tpTimer;
    // Target of tpAsk
    private TeleportRequest teleportRequest;

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
        tpTimer = -1;
        incomingTeleportRequests = new LinkedHashMap<>();
        homes = new NamedLocationStorage();
        PLAYER_ACT_EVENT.register((packet) -> {
            updateLastActionTick();
            setAfk(false);
        });
    }

    /**
     * DO NOT USE FOR LOGGED-IN PLAYERS.
     * This constructor should ONLY be used for temporarily
     * handling data of offline players.
     *
     * getPlayer() will always return null on an instance created in this fashion,
     * and any operations that would require a ServerPlayerEntity will fail.
     *
     * @param playerUuid UUID of the player whose data we want to grab or modify.
     * @param homes NamedLocationStorage of the player's homes (fills field)
     * @param saveFile The save file for this PlayerData instance.
     */
    public PlayerData(UUID playerUuid, NamedLocationStorage homes, File saveFile) {
        this.pUuid = playerUuid;
        this.saveFile = saveFile;
        tpTimer = -1;
        incomingTeleportRequests = new LinkedHashMap<>();
        this.homes = homes;
    }


    public int getTpTimer() {
        return tpTimer;
    }

    public void setTpTimer(int tpTimer) {
        this.tpTimer = tpTimer;
    }

    public void tickTpTimer() {
        tpTimer--;
    }

    public TeleportRequest getSentTeleportRequest() {
        return teleportRequest;
    }

    public void setSentTeleportRequest(TeleportRequest request) {
        this.teleportRequest = request;
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
    public int addHome(String homeName, MinecraftLocation minecraftLocation) throws CommandSyntaxException {
        int outCode = 0;
        int playerMaxHomes = ECPerms.getHighestNumericPermission(this.player.getCommandSource(), ECPerms.Registry.Group.home_limit_group);
        if (this.homes.size() < playerMaxHomes) {
            homes.putCommand(homeName, minecraftLocation);
            this.markDirty();
            outCode = 1;
        } else {
            var homeNameText = ECText.accent(homeName);
            var maxHomesText = ECText.accent(String.valueOf(playerMaxHomes));
            this.sendError(
                ECText.getInstance().getText(
                    "cmd.home.set.error.limit",
                    TextFormatType.Error,
                    homeNameText,
                    maxHomesText
                ));
        }

        return outCode;
    }

    public void sendError(Text message) {
        this.player.sendMessage(
            Text.empty()
                .append(message)
                .setStyle(CONFIG.FORMATTING_ERROR),
            MessageType.SYSTEM);
    }

    public Set<String> getHomeNames() {
        return homes.keySet();
    }

    public Set<Map.Entry<String, MinecraftLocation>> getHomeEntries() {
        return homes.entrySet();
    }

    public MinecraftLocation getHomeLocation(String homeName) {
        return homes.get(homeName);
    }

    public final Event<PlayerActCallback> PLAYER_ACT_EVENT = EventFactory.createArrayBacked(
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
            if (CONFIG.INVULN_WHILE_AFK) {
                Pal.grantAbility(this.player, VanillaAbilities.INVULNERABLE, ECAbilitySources.AFK_INVULN);
            }

            this.player.server.getPlayerManager().broadcast(
                ECText.getInstance().getText(
                    "player.afk.enter",
                    this.player.getDisplayName()),
                MessageType.SYSTEM);

            // This assignment should happen after the message, otherwise
            // `getDisplayName` will include the `[AFK]` prefix.
            this.afk = afk;
        } else {
            // This assignment should happen before the message, otherwise
            // `getDisplayName` will include the `[AFK]` prefix.
            this.afk = afk;

            Pal.revokeAbility(this.player, VanillaAbilities.INVULNERABLE, ECAbilitySources.AFK_INVULN);

            this.player.server.getPlayerManager().broadcast(
                ECText.getInstance().getText(
                    "player.afk.exit",
                    this.player.getDisplayName()),
                MessageType.SYSTEM);
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

    private static final class StorageKey
    {
        static final String playerUuid = "playerUuid";
        static final String homes = "homes";
        static final String nickname = "nickname";
        static final String timeUsedRtpEpochMs = "timeUsedRtpEpochMs";
    }

    public void fromNbt(NbtCompound tag) {
        NbtCompound dataTag = tag.getCompound("data");
        this.pUuid = dataTag.getUuid(StorageKey.playerUuid);

        NamedLocationStorage homes = new NamedLocationStorage();
        NbtElement homesTag = dataTag.get(StorageKey.homes);
        if (homesTag != null) {
            homes.loadNbt(homesTag);
        }
        this.homes = homes;

        if (dataTag.contains(StorageKey.nickname)){
            this.nickname = Text.Serializer.fromJson(dataTag.getString(StorageKey.nickname));
            try {
                reloadFullNickname();
            } catch (NullPointerException ignore) {
                EssentialCommands.LOGGER.warn("Could not refresh player full nickanme, as ServerPlayerEntity was null in PlayerData.");
            }
        }

        if (dataTag.contains(StorageKey.timeUsedRtpEpochMs)) {
            this.timeUsedRtp = TimeUtil.epochTimeMsToTicks(dataTag.getLong(StorageKey.timeUsedRtpEpochMs));
        }

        if (this.player != null) {
            updatePlayer(this.player);
        }

    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putUuid(StorageKey.playerUuid, pUuid);

        NbtCompound homesNbt = new NbtCompound();
        homes.writeNbt(homesNbt);
        tag.put(StorageKey.homes, homesNbt);

        tag.putString(StorageKey.nickname, Text.Serializer.toJson(nickname));

        tag.putLong(StorageKey.timeUsedRtpEpochMs, TimeUtil.tickTimeToEpochMs(timeUsedRtp));

        return tag;
    }

    public void setPreviousLocation(MinecraftLocation location) {
        this.previousLocation = location;
    }

    public MinecraftLocation getPreviousLocation() {
        return this.previousLocation;
    }

    /**
     * Removes a home & persists changes to storage. Permanent.
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

    public void updatePlayer(ServerPlayerEntity serverPlayerEntity) {
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
                .append(this.nickname );
        } else {
            tempFullNickname
                .append(baseName);
        }

        if (CONFIG.NICK_REVEAL_ON_HOVER) {
            tempFullNickname.setStyle(tempFullNickname.getStyle().withHoverEvent(
                HoverEvent.Action.SHOW_TEXT.buildHoverEvent(baseName)
            ));
        }

        this.fullNickname = tempFullNickname;
    }

}
