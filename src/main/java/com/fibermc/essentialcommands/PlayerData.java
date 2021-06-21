package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

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
    private MinecraftLocation previousLocation;
    private int tpCooldown;

    public PlayerData(ServerPlayerEntity player) {
        super(player.getUuid().toString());
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

    // Homes
    public int addHome(String homeName, MinecraftLocation minecraftLocation) {
        int outCode = 0;
        if (Config.HOME_LIMIT == -1 || this.homes.size() < Config.HOME_LIMIT) {
            homes.put(homeName, minecraftLocation);
            this.markDirty();
            outCode = 1;
        }

        return outCode;
    }

    public Set<String> getHomeNames() {
        return homes.keySet();
    }

    public MinecraftLocation getHomeLocation(String homeName) {
        return homes.get(homeName);
    }

    // IO
//    @Override
    @Override
    public void fromTag(NbtCompound tag) {
        fromNbt(tag);
    }

    public void fromNbt(NbtCompound tag) {
        NbtCompound dataTag = tag.getCompound("data");
        this.pUuid = dataTag.getUuid("playerUuid");
        NbtList homesNbtList = dataTag.getList("homes", 10);
        HashMap<String, MinecraftLocation> homes = Maps.newHashMap();
        for (NbtElement t : homesNbtList) {
            NbtCompound homeTag = (NbtCompound) t;
            homes.put(homeTag.getString("homeName"), new MinecraftLocation(homeTag));
        }
        this.homes = homes;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putUuid("playerUuid", pUuid);
        NbtList homesNbtList = new NbtList();
        homes.forEach((String key, MinecraftLocation homeLocation) -> {
            NbtCompound homeTag = homeLocation.writeNbt(new NbtCompound());
            homeTag.putString("homeName", key);
            homesNbtList.add(homeTag);
        });
        tag.put("homes", homesNbtList);

        return tag;
    }

    public void setPreviousLocation(MinecraftLocation location) {
        this.previousLocation = location;
    }

    public MinecraftLocation getPreviousLocation() {
        return this.previousLocation;
    }

    public boolean removeHome(String homeName) {
        //returns false if home does not exist - true if successful
        boolean out = false;
        MinecraftLocation old = this.homes.remove(homeName);
        if (old != null) {
            out = true;
            this.markDirty();
        }
        return out;
    }

    public void updatePlayer(ServerPlayerEntity serverPlayerEntity) {
        this.player = serverPlayerEntity;
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

}
