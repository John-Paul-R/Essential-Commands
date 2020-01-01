package net.fabricmc.example;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerData implements Serializable{

    
    //ServerPlayerEntity
    private ServerPlayerEntity player;

    //TELEPORT
    //Remaining time before current tp Ask (sent by this player) expires.
    private int tpTimer;
    //Target of tpAsk
    private ServerPlayerEntity tpTarget;

    //players that have Asked to tp to this player
    //This list exists for autofilling the 'tpaccept' command
    private LinkedList<ServerPlayerEntity> tpAskers;
    

    //HOMES
    HashMap<String, MinecraftLocation> homes;

    public PlayerData(ServerPlayerEntity player) {
        this.player = player;
        tpTimer = -1;
        tpTarget = null;
        tpAskers = new LinkedList<ServerPlayerEntity>();
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

    public ServerPlayerEntity getTpTarget() {
        return tpTarget;
    }
    public void setTpTarget(ServerPlayerEntity tpTarget) {
        this.tpTarget = tpTarget;
    }

    public LinkedList<ServerPlayerEntity> getTpAskers() {
        return tpAskers;
    }
    public boolean hasBeenAskedByPlayer(ServerPlayerEntity tpAsker) {
        return tpAskers.contains(tpAsker);
    }
    public void addTpAsker(ServerPlayerEntity tpAsker) {
        this.tpAskers.add(tpAsker);
    }
    public void removeTpAsker(ServerPlayerEntity tpAsker) {
        this.tpAskers.remove(tpAsker);
    }


    private static final long serialVersionUID = 6565450996035389353L;

    public ServerPlayerEntity getPlayer() {
        return player;
    }


	public void addHome(String homeName, MinecraftLocation minecraftLocation) {
        homes.put(homeName, minecraftLocation);
    }
    public Set<String> getHomeNames() {
        return homes.keySet();
    }
	public MinecraftLocation getHomeLocation(String homeName) {
        return homes.get(homeName);
	}


}
