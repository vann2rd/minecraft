package dev.vann2rd.vso;

import java.util.Arrays;

public final class VsoMetrics {
    private static final int WINDOW = 100;
    private static final double[] TICK_MS = new double[WINDOW];
    private static int cursor;
    private static int samples;
    private static long tickStartNanos;

    public static long totalItemsRemoved;
    public static long totalXpRemoved;
    public static long totalMobsRemoved;
    public static long chunkLoads;
    public static long chunkUnloads;
    public static long loadedChunkEstimate;
    public static long blockEntityCountOnLastLoadedChunk;
    public static double lastTickMs;
    public static double averageTickMs;
    public static double maxTickMs;

    private VsoMetrics() {}

    public static void beginTick() {
        tickStartNanos = System.nanoTime();
    }

    public static void endTick() {
        if (tickStartNanos == 0) return;
        lastTickMs = (System.nanoTime() - tickStartNanos) / 1_000_000.0;
        TICK_MS[cursor] = lastTickMs;
        cursor = (cursor + 1) % WINDOW;
        samples = Math.min(samples + 1, WINDOW);
        double sum = 0;
        maxTickMs = 0;
        for (int i = 0; i < samples; i++) {
            sum += TICK_MS[i];
            maxTickMs = Math.max(maxTickMs, TICK_MS[i]);
        }
        averageTickMs = samples == 0 ? 0 : sum / samples;
    }

    public static long usedHeapMiB() {
        Runtime r = Runtime.getRuntime();
        return (r.totalMemory() - r.freeMemory()) / (1024 * 1024);
    }

    public static long maxHeapMiB() {
        return Runtime.getRuntime().maxMemory() / (1024 * 1024);
    }
}
