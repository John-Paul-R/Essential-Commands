package net.fabricmc.essentialcommands;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;

public class PlayerData extends PersistentState {

    // ServerPlayerEntity
    private ServerPlayerEntity player;
    private UUID pUuid;

    // TELEPORT
    // Remaining time before current tp Ask (sent by this player) expires.
    private int tpTimer;
    // Target of tpAsk
    private PlayerData tpTarget;

    // players that have Asked to tp to this player
    // This list exists for autofilling the 'tpaccept' command
    private LinkedList<PlayerData> tpAskers;

    // HOMES
    HashMap<String, MinecraftLocation> homes;

    public PlayerData(String keyUUID, ServerPlayerEntity player) {
        super(keyUUID);
        this.player = player;
        this.pUuid = player.getUuid();
        tpTimer = -1;
        tpTarget = null;
        tpAskers = new LinkedList<PlayerData>();
        homes = new HashMap<String, MinecraftLocation>();
    }

    public int getTpTimer() {
        return tpTimer;
    }

    public void setTpTimer(int tpTimer) {
        this.tpTimer = tpTimer;
    }

    public void tickTpTimer() {
        tpTimer--;
        if (tpTimer == -1) {
            this.tpTarget = null;
        }
    }

    public PlayerData getTpTarget() {
        return tpTarget;
    }

    public void setTpTarget(PlayerData tpTarget) {
        this.tpTarget = tpTarget;
    }

    public LinkedList<PlayerData> getTpAskers() {
        return tpAskers;
    }

    public boolean hasBeenAskedByPlayer(PlayerData tpAsker) {
        return tpAskers.contains(tpAsker);
    }

    public void addTpAsker(PlayerData tpAsker) {
        this.tpAskers.add(tpAsker);
    }

    public void removeTpAsker(PlayerData tpAsker) {
        this.tpAskers.remove(tpAsker);
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    //Homes
    public void addHome(String homeName, MinecraftLocation minecraftLocation) {
        homes.put(homeName, minecraftLocation);
    }

    public Set<String> getHomeNames() {
        return homes.keySet();
    }

    public MinecraftLocation getHomeLocation(String homeName) {
        return homes.get(homeName);
    }

    //IO
    @Override
    public void fromTag(CompoundTag tag) {
        this.pUuid = tag.getUuid("playerUuid");
        
        CompoundTag homesTag = tag.getCompound("homes");
        HashMap<String, MinecraftLocation> homes = Maps.newHashMap();
        Set<String> homeNames = homesTag.getKeys();
        for (String s : homeNames) {
            CompoundTag mcLoc = homesTag.getCompound(s);
            homes.put(s, new MinecraftLocation(mcLoc));
        }

    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putUuid("playerUuid", pUuid);
        CompoundTag homesCompoundTag = new CompoundTag();
        for(Entry<String, MinecraftLocation> entry : homes.entrySet()) {
            homesCompoundTag.put(entry.getKey(), entry.getValue().toTag(new CompoundTag()));
        }
        tag.put("homes", homesCompoundTag);
        
        return tag;
    }


}
