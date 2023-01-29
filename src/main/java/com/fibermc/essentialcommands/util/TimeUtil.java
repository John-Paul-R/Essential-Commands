package com.fibermc.essentialcommands.util;

import net.minecraft.server.MinecraftServer;

public final class TimeUtil {
    private static final double tps = 20;
    private static final double secondsPerTick = 1 / tps;
    private static final long msPerTick = (long) (secondsPerTick * 1000);
    private static MinecraftServer _server;

    public static void init(MinecraftServer server) {
        _server = server;
    }

    public static int getTicks() {
        return _server.getTicks();
    }

    public static long ticksToMs(int ticks) {
        return ticks * msPerTick;
    }

    public static int msToTicks(long ms) {
        return (int) (ms / msPerTick);
    }

    public static long tickTimeToEpochMs(int ticks) {
        return ticksToMs(ticks - _server.getTicks()) + net.minecraft.util.Util.getEpochTimeMs();
    }

    public static int epochTimeMsToTicks(long epochTimeMs) {
        var msFromNow = epochTimeMs - net.minecraft.util.Util.getEpochTimeMs();
        return _server.getTicks() + msToTicks(msFromNow);
    }
}
