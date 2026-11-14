package dev.vann2rd.vso;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.monster.Monster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class VannServerOptimizer implements ModInitializer {
    private long ticks;

    @Override
    public void onInitialize() {
        VsoConfig.load();

        ServerTickEvents.START_SERVER_TICK.register(server -> VsoMetrics.beginTick());
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            VsoMetrics.endTick();
            onEndTick(server);
        });

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            VsoMetrics.chunkLoads++;
            VsoMetrics.loadedChunkEstimate++;
            VsoMetrics.blockEntityCountOnLastLoadedChunk = chunk.getBlockEntities().size();
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            VsoMetrics.chunkUnloads++;
            VsoMetrics.loadedChunkEstimate = Math.max(0, VsoMetrics.loadedChunkEstimate - 1);
        });

        System.out.println("[VSO] Ultimate 3.2.0 initialized.");
        System.out.println("[VSO] Item drops and XP orbs are configured for automatic deletion.");
    }

    private void onEndTick(MinecraftServer server) {
        ticks++;
        if (ticks % VsoConfig.cleanupEveryTicks == 0) cleanupDrops(server);
        if (VsoConfig.limitMobs && ticks % VsoConfig.mobScanEveryTicks == 0) limitMobs(server);
        if (VsoConfig.logStats && ticks % VsoConfig.statsEveryTicks == 0) logStats();
    }

    private void cleanupDrops(MinecraftServer server) {
        long itemCount = 0;
        long xpCount = 0;

        for (ServerLevel level : server.getAllLevels()) {
            List<Entity> remove = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (VsoConfig.clearItemDrops && entity instanceof ItemEntity) {
                    remove.add(entity);
                    itemCount++;
                } else if (VsoConfig.clearXpOrbs && entity instanceof ExperienceOrb) {
                    remove.add(entity);
                    xpCount++;
                }
            }
            remove.forEach(Entity::discard);
        }

        VsoMetrics.totalItemsRemoved += itemCount;
        VsoMetrics.totalXpRemoved += xpCount;
        if (itemCount + xpCount > 0) {
            System.out.println("[VSO] Cleanup: items=" + itemCount + ", xp=" + xpCount);
        }
    }

    private void limitMobs(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            List<Mob> hostile = new ArrayList<>();
            List<Mob> passive = new ArrayList<>();

            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof Mob mob) || protectedMob(mob)) continue;
                if (mob instanceof Monster) hostile.add(mob);
                else if (mob instanceof Animal) passive.add(mob);
            }

            VsoMetrics.totalMobsRemoved += trim(level, hostile, VsoConfig.hostileLimitPerDimension);
            VsoMetrics.totalMobsRemoved += trim(level, passive, VsoConfig.passiveLimitPerDimension);
        }
    }

    private boolean protectedMob(Mob mob) {
        if (VsoConfig.protectNamedMobs && mob.hasCustomName()) return true;
        if (VsoConfig.protectPersistentMobs && mob.isPersistenceRequired()) return true;
        return VsoConfig.protectLeashedMobs && mob.isLeashed();
    }

    private int trim(ServerLevel level, List<Mob> mobs, int limit) {
        if (mobs.size() <= limit) return 0;
        var spawn = level.getSharedSpawnPos();
        mobs.sort(Comparator.comparingDouble(m -> m.distanceToSqr(
                spawn.getX(), spawn.getY(), spawn.getZ())));
        int removed = 0;
        for (int i = limit; i < mobs.size(); i++) {
            mobs.get(i).discard();
            removed++;
        }
        return removed;
    }

    private void logStats() {
        System.out.printf(
            "[VSO] MSPT last=%.2f avg100=%.2f max100=%.2f | heap=%d/%d MiB | loadedChunks~=%d | chunkLoad=%d unload=%d | blockEntities(last loaded chunk)=%d | removed items=%d xp=%d mobs=%d%n",
            VsoMetrics.lastTickMs, VsoMetrics.averageTickMs, VsoMetrics.maxTickMs,
            VsoMetrics.usedHeapMiB(), VsoMetrics.maxHeapMiB(),
            VsoMetrics.loadedChunkEstimate, VsoMetrics.chunkLoads, VsoMetrics.chunkUnloads,
            VsoMetrics.blockEntityCountOnLastLoadedChunk,
            VsoMetrics.totalItemsRemoved, VsoMetrics.totalXpRemoved, VsoMetrics.totalMobsRemoved
        );
    }
}
