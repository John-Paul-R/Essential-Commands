package dev.jpcode.eccore.util;

import java.time.Duration;

import net.minecraft.server.MinecraftServer;

public final class TimeUtil {
    private TimeUtil() {}

    public static final int TPS = 20;
    public static final double SECONDS_PER_TICK = 1 / (double) TPS;
    public static final long MS_PER_TICK = (long) (SECONDS_PER_TICK * 1000);
    private static MinecraftServer server;

    public static void init(MinecraftServer server) {
        TimeUtil.server = server;
    }

    public static int getTicks() {
        return server.getTicks();
    }

    public static long ticksToMs(int ticks) {
        return ticks * MS_PER_TICK;
    }

    public static int msToTicks(long ms) {
        return (int) (ms / MS_PER_TICK);
    }

    public static long tickTimeToEpochMs(int ticks) {
        return ticksToMs(ticks - server.getTicks()) + net.minecraft.util.Util.getEpochTimeMs();
    }

    public static int epochTimeMsToTicks(long epochTimeMs) {
        var msFromNow = epochTimeMs - net.minecraft.util.Util.getEpochTimeMs();
        return server.getTicks() + msToTicks(msFromNow);
    }

    public static int durationToTicks(Duration duration) {
        return TimeUtil.msToTicks(duration.toMillis());
    }
}
