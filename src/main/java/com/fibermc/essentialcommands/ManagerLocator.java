package com.fibermc.essentialcommands;

public class ManagerLocator {

    private PlayerDataManager dataManager;
    private TeleportRequestManager tpManager;

    public ManagerLocator(PlayerDataManager dataManager, TeleportRequestManager tpManager) {
        this.dataManager = dataManager;
        this.tpManager = tpManager;
    }

    public PlayerDataManager getDataManager() {
        return dataManager;
    }

    public TeleportRequestManager getTpManager() {
        return tpManager;
    }
}
