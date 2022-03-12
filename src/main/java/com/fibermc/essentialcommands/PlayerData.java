package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.types.NamedLocationStorage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jpcode.eccore.util.TextUtil;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.PersistentState;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    private int rtpNextUsableTime;

    public PlayerData(ServerPlayerEntity player, File saveFile) {
        this.player = player;
        this.pUuid = player.getUuid();
        this.saveFile = saveFile;
        tpTimer = -1;
        incomingTeleportRequests = new LinkedHashMap<>();
        homes = new NamedLocationStorage();
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
            this.sendError(TextUtil.concat(
                ECText.getInstance().getText("cmd.home.feedback.1").setStyle(CONFIG.FORMATTING_ERROR.getValue()),
                new LiteralText(homeName).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                ECText.getInstance().getText("cmd.home.set.error.limit.2").setStyle(CONFIG.FORMATTING_ERROR.getValue()),
                new LiteralText(String.valueOf(playerMaxHomes)).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                ECText.getInstance().getText("cmd.home.set.error.limit.3").setStyle(CONFIG.FORMATTING_ERROR.getValue())
            ));
        }

        return outCode;
    }

    public void sendError(Text message) {
        this.player.sendSystemMessage(
            new LiteralText("")
                .append(message)
                .setStyle(CONFIG.FORMATTING_ERROR.getValue()),
            Util.NIL_UUID);
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

    public void fromNbt(NbtCompound tag) {
        NbtCompound dataTag = tag.getCompound("data");
        this.pUuid = dataTag.getUuid("playerUuid");

        NamedLocationStorage homes = new NamedLocationStorage();
        NbtElement homesTag = dataTag.get("homes");
        if (homesTag != null) {
            homes.loadNbt(homesTag);
        }
        this.homes = homes;

        if (dataTag.contains("nickname")){
            this.nickname = Text.Serializer.fromJson(dataTag.getString("nickname"));
            try {
                reloadFullNickname();
            } catch (NullPointerException ignore) {
                EssentialCommands.LOGGER.warn("Could not refresh player full nickanme, as ServerPlayerEntity was null in PlayerData.");
            }
        }

        if (this.player != null) {
            updatePlayer(this.player);
        }

    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putUuid("playerUuid", pUuid);

        NbtCompound homesNbt = new NbtCompound();
        homes.writeNbt(homesNbt);
        tag.put("homes", homesNbt);

        tag.putString("nickname", Text.Serializer.toJson(nickname));

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

    public MutableText getNickname() {
        return nickname != null ? nickname.shallowCopy() : null;
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
            if (nickname.getString().length() > CONFIG.NICKNAME_MAX_LENGTH.getValue()) {
                return -2;
            }
            // Ensure player has permissions required to set the specified nickname
            boolean hasRequiredPerms = NicknameText.checkPerms(nickname, this.player.getCommandSource());
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

    public void setRtpNextUsableTime(int i) {
        this.rtpNextUsableTime = i;
    }

    public int getRtpNextUsableTime() {
        return rtpNextUsableTime;
    }

    public void reloadFullNickname() {
        MutableText baseName = new LiteralText(this.getPlayer().getGameProfile().getName());
        MutableText tempFullNickname = new LiteralText("");
        // Note: this doesn't ever display if nickname is null,
        //  because our mixin to getDisplayName does a null check on getNickname
        if (this.nickname != null) {
            tempFullNickname
                .append(CONFIG.NICKNAME_PREFIX.getValue())
                .append(this.nickname );
        } else {
            tempFullNickname
                .append(baseName);
        }

        if (CONFIG.NICK_REVEAL_ON_HOVER.getValue()) {
            tempFullNickname.setStyle(tempFullNickname.getStyle().withHoverEvent(
                HoverEvent.Action.SHOW_TEXT.buildHoverEvent(baseName)
            ));
        }

        this.fullNickname = tempFullNickname;
    }

}
