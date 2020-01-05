package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class PlayerData extends PersistentState {

    // Managers
    private TeleportRequestManager tpManager;

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
    private int tpDelay;

    public PlayerData(String keyUUID, ServerPlayerEntity player, ManagerLocator managers) {
        super(keyUUID);
        this.player = player;
        this.pUuid = player.getUuid();
        tpTimer = -1;
        tpTarget = null;
        tpAskers = new LinkedList<PlayerData>();
        homes = new HashMap<String, MinecraftLocation>();

        this.tpManager = managers.getTpManager();
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

    public void updatePlayer(ServerPlayerEntity serverPlayerEntity) {
        this.player = serverPlayerEntity;
    }

    public void tickTpCooldown() {
        this.tpCooldown--;
    }

    public int getTpCooldown() {
        return tpCooldown;
    }

    public void tickTpDelay() {
        this.tpDelay--;
    }

    public int getTpDelay() {
        return this.tpDelay;
    }

    public void setTpCooldown(int cooldown) {
        this.tpCooldown = cooldown;
    }

    public void setTpDelay(int delay) {
        this.tpDelay = delay;
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
