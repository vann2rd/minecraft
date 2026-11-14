# VannServer Optimizer Ultimate 3.2.0

Fabric server-side project focused on reducing loaded-entity pressure and measuring server workload.

## Implemented in source
- Deletes all loaded dropped item entities every 100 ticks (~5 seconds at 20 TPS), including valuable items.
- Deletes all loaded XP orbs on the same schedule.
- Every 1200 ticks (~60 seconds), limits ordinary hostile mobs to 20 and passive animals to 12 per loaded dimension.
- Skips mobs with custom names, persistence-required mobs, and leashed mobs.
- Measures server tick duration (MSPT), rolling average/max, JVM heap usage, chunk load/unload event counts, and a loaded-chunk estimate.
- Prints a metrics line every 1200 ticks.
- Scans only already-loaded entity collections; it does not request chunk loads.

## Important limits — not falsely advertised as implemented
This source does **not** change Minecraft's internal chunk scheduler, throttle block-entity tickers, or rewrite redstone scheduling. Those operations require version-specific mixins and careful whitelists; indiscriminately skipping them can break hoppers, furnaces, farms, doors, redstone clocks, and world simulation.
The chunk callbacks here are telemetry only. For actual chunk workload reduction, use the included `server.properties` template:
- `view-distance=5`
- `simulation-distance=4`

This reduces the amount of world actively sent/simulated. It is not a custom chunk-engine optimization. Back up the world before changing it.

Memory figures are monitoring only. The mod does not force garbage collection or change JVM heap limits. Set Java memory arguments in the launcher rather than forcing GC every few seconds.

This is a server-side mod and does not directly improve client rendering FPS. Sodium and compatible client performance mods address rendering separately.

## Build on Arch Linux
Minecraft/Fabric 26.2 coordinates are version-sensitive. These are declared project inputs and are not a guarantee that all artifacts have been published or that the code compiles unchanged.

```bash
sudo pacman -S --needed jdk25-openjdk gradle
sudo archlinux-java set java-25-openjdk
gradle wrapper --gradle-version 9.5.1 --distribution-type bin
chmod +x gradlew
./gradlew build --stacktrace
```

Expected output if build succeeds:
`build/libs/vannserver-optimizer-3.2.0.jar`

Do not install the jar unless Gradle build succeeds. Back up the Minecraft world and test on a copy first.

## Config
Created at:
`config/vannserver-optimizer.properties`

```properties
clearItemDrops=true
clearXpOrbs=true
limitMobs=true
protectNamedMobs=true
protectPersistentMobs=true
protectLeashedMobs=true
logStats=true
cleanupEveryTicks=100
mobScanEveryTicks=1200
statsEveryTicks=1200
hostileLimitPerDimension=20
passiveLimitPerDimension=12
```

To disable item deletion, set `clearItemDrops=false`. This is destructive by default: valuable loot is deleted too.

## Build without installing Java or Gradle on your laptop

This project includes a GitHub Actions workflow at `.github/workflows/build.yml`.
It compiles on GitHub's hosted runner using Java 25 and Gradle 9.5.1. Your own laptop does not need Java or Gradle.

### One-time setup
1. Extract this ZIP.
2. Create a new empty GitHub repository.
3. Upload the *contents* of the `VannServerOptimizer-Ultimate-3.2.0` folder to the repository root, including the hidden `.github/workflows/build.yml` file.
4. Open the repository's **Actions** tab. Run **Build VannServer Optimizer** if it has not started automatically.
5. Open the completed run and download the `vannserver-optimizer-3.2.0` artifact. It contains the JAR only if compilation succeeds.

### Important
- This workflow avoids downloading Java/Gradle onto your laptop, but uploading source and downloading the resulting artifact still requires an internet connection. The source ZIP is small; the resulting JAR should also be small.
- The original Java source has not yet been verified against the 26.2 API. If compilation fails, open the failed Actions run and inspect the first `error:` lines; the source may need a compatibility fix.
- Back up the Minecraft world before testing. This mod deletes dropped items and XP orbs by design.
