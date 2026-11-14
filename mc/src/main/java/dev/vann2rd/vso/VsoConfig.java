package dev.vann2rd.vso;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class VsoConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("vannserver-optimizer.properties");

    public static boolean clearItemDrops = true;
    public static boolean clearXpOrbs = true;
    public static boolean limitMobs = true;
    public static boolean protectNamedMobs = true;
    public static boolean protectPersistentMobs = true;
    public static boolean protectLeashedMobs = true;
    public static boolean logStats = true;
    public static int cleanupEveryTicks = 100;
    public static int mobScanEveryTicks = 1200;
    public static int statsEveryTicks = 1200;
    public static int hostileLimitPerDimension = 20;
    public static int passiveLimitPerDimension = 12;

    private VsoConfig() {}

    public static void load() {
        Properties p = new Properties();
        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) { p.load(in); }
            catch (IOException e) { System.err.println("[VSO] Config read failed: " + e); }
        } else {
            p.setProperty("clearItemDrops", "true");
            p.setProperty("clearXpOrbs", "true");
            p.setProperty("limitMobs", "true");
            p.setProperty("protectNamedMobs", "true");
            p.setProperty("protectPersistentMobs", "true");
            p.setProperty("protectLeashedMobs", "true");
            p.setProperty("logStats", "true");
            p.setProperty("cleanupEveryTicks", "100");
            p.setProperty("mobScanEveryTicks", "1200");
            p.setProperty("statsEveryTicks", "1200");
            p.setProperty("hostileLimitPerDimension", "20");
            p.setProperty("passiveLimitPerDimension", "12");
            try {
                Files.createDirectories(FILE.getParent());
                try (OutputStream out = Files.newOutputStream(FILE)) {
                    p.store(out, "VannServer Optimizer Ultimate 3.2.0");
                }
            } catch (IOException e) { System.err.println("[VSO] Config create failed: " + e); }
        }

        clearItemDrops = bool(p, "clearItemDrops", true);
        clearXpOrbs = bool(p, "clearXpOrbs", true);
        limitMobs = bool(p, "limitMobs", true);
        protectNamedMobs = bool(p, "protectNamedMobs", true);
        protectPersistentMobs = bool(p, "protectPersistentMobs", true);
        protectLeashedMobs = bool(p, "protectLeashedMobs", true);
        logStats = bool(p, "logStats", true);
        cleanupEveryTicks = integer(p, "cleanupEveryTicks", 100, 1, 72000);
        mobScanEveryTicks = integer(p, "mobScanEveryTicks", 1200, 20, 72000);
        statsEveryTicks = integer(p, "statsEveryTicks", 1200, 20, 72000);
        hostileLimitPerDimension = integer(p, "hostileLimitPerDimension", 20, 0, 10000);
        passiveLimitPerDimension = integer(p, "passiveLimitPerDimension", 12, 0, 10000);
    }

    private static boolean bool(Properties p, String k, boolean d) {
        return Boolean.parseBoolean(p.getProperty(k, Boolean.toString(d)).trim());
    }
    private static int integer(Properties p, String k, int d, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(p.getProperty(k, Integer.toString(d)).trim()))); }
        catch (NumberFormatException e) { return d; }
    }
}
