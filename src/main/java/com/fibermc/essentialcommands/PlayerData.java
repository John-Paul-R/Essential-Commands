package com.fibermc.essentialcommands;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
    private MinecraftLocation previousLocation;

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
    public void addHome(String homeName, MinecraftLocation minecraftLocation) {
        homes.put(homeName, minecraftLocation);
        this.markDirty();
    }

    public Set<String> getHomeNames() {
        return homes.keySet();
    }

    public MinecraftLocation getHomeLocation(String homeName) {
        return homes.get(homeName);
    }

    // IO
    @Override
    public void fromTag(CompoundTag tag) {
        this.pUuid = tag.getUuid("playerUuid");
        ListTag homesListTag = tag.getCompound("data").getList("homes", 10);
        HashMap<String, MinecraftLocation> homes = Maps.newHashMap();
        for (Tag t : homesListTag) {
            CompoundTag homeTag = (CompoundTag) t;
            MinecraftLocation location = new MinecraftLocation(homeTag);
            String homeName = homeTag.getString("homeName");
            homes.put(homeName, location);
        }
        this.homes = homes;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putUuid("playerUuid", pUuid);
        ListTag homesListTag = new ListTag();
        for (Entry<String, MinecraftLocation> entry : homes.entrySet()) {
            CompoundTag homeTag = entry.getValue().toTag(new CompoundTag());
            homeTag.putString("homeName", entry.getKey());
            homesListTag.add(homeTag);
        }
        tag.put("homes", homesListTag);

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

    //Just used default method
//     @Override
//     public void save(File file) {
//       if (this.isDirty()) {
//          CompoundTag compoundTag = new CompoundTag();
//          compoundTag.put("data", this.toTag(new CompoundTag()));
//          compoundTag.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());

//          try {
//             FileOutputStream fileOutputStream =new FileOutputStream(file);
//             DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
//             Throwable var4 = null;

//             try {
//                NbtIo.write(compoundTag, dataOutputStream);
//             } catch (Throwable var14) {
//                var4 = var14;
//                throw var14;
//             } finally {
//                if (dataOutputStream != null) {
//                   if (var4 != null) {
//                      try {
//                         dataOutputStream.close();
//                      } catch (Throwable var13) {
//                         var4.addSuppressed(var13);
//                      }
//                   } else {
//                     dataOutputStream.close();
//                     fileOutputStream.close();
//                   }
//                }

//             }
//          } catch (IOException var16) {
//              //todo handle exception
             
//          }

//          this.setDirty(false);
//       }
//    }
}
