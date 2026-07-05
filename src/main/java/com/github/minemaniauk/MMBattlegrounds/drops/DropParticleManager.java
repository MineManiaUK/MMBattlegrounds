package com.github.minemaniauk.MMBattlegrounds.drops;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class DropParticleManager {

    private final JavaPlugin plugin;

    public DropParticleManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Location> spawnArc(Location target) {
        CompletableFuture<Location> future = new CompletableFuture<>();

        if (plugin == null || target == null || target.getWorld() == null) {
            future.completeExceptionally(new IllegalArgumentException("Invalid plugin or target location"));
            return future;
        }

        World world = target.getWorld();

        Location end = target.clone();
        Location start = randomStart(end);
        Location control = randomControl(start, end);

        int totalTicks = ThreadLocalRandom.current().nextInt(2400, 3601);

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                double t = (double) tick / totalTicks;

                if (t >= 1.0) {
                    impact(world, end);

                    future.complete(end.clone());

                    cancel();
                    return;
                }

                Location point = quadraticBezier(start, control, end, t);

                world.spawnParticle(
                        Particle.FLAME,
                        point,
                        4,
                        0.18, 0.18, 0.18,
                        0.015
                );

                world.spawnParticle(
                        Particle.LAVA,
                        point,
                        1,
                        0.08, 0.08, 0.08,
                        0.0
                );

                spawnTrail(world, start, control, end, t);

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return future;
    }

    private static Location randomStart(Location end) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Random direction around the target
        double angle = random.nextDouble(0, Math.PI * 2);

        // Far enough away for a 2–3 minute 
        double distance = random.nextDouble(250, 500);

        // Height above the target
        double height = random.nextDouble(120, 220);

        double x = Math.cos(angle) * distance;
        double z = Math.sin(angle) * distance;

        return end.clone().add(x, height, z);
    }

    private static Location randomControl(Location start, Location end) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Midpoint between start and impact point
        Location midpoint = start.clone().add(end).multiply(0.5);

        // Raise the arc
        double extraHeight = random.nextDouble(80, 160);
        midpoint.add(0, extraHeight, 0);

        // Sideways variation so every  arc feels different
        double sideOffsetX = random.nextDouble(-80, 80);
        double sideOffsetZ = random.nextDouble(-80, 80);
        midpoint.add(sideOffsetX, 0, sideOffsetZ);

        return midpoint;
    }

    private static Location quadraticBezier(Location start, Location control, Location end, double t) {
        double oneMinusT = 1.0 - t;

        double x =
                oneMinusT * oneMinusT * start.getX()
                        + 2 * oneMinusT * t * control.getX()
                        + t * t * end.getX();

        double y =
                oneMinusT * oneMinusT * start.getY()
                        + 2 * oneMinusT * t * control.getY()
                        + t * t * end.getY();

        double z =
                oneMinusT * oneMinusT * start.getZ()
                        + 2 * oneMinusT * t * control.getZ()
                        + t * t * end.getZ();

        return new Location(start.getWorld(), x, y, z);
    }

    private static void spawnTrail(
            World world,
            Location start,
            Location control,
            Location end,
            double t
    ) {
        // Number of points behind the 
        int trailPoints = 10;

        // Bigger value = longer trail
        double trailSpacing = 0.0025;

        for (int i = 0; i < trailPoints; i++) {
            double trailT = t - (i * trailSpacing);

            if (trailT < 0) continue;

            Location trailPoint = quadraticBezier(start, control, end, trailT);

            world.spawnParticle(
                    Particle.FLAME,
                    trailPoint,
                    2,
                    0.15, 0.15, 0.15,
                    0.01
            );

            world.spawnParticle(
                    Particle.SMOKE,
                    trailPoint,
                    1,
                    0.25, 0.25, 0.25,
                    0.01
            );
        }
    }

    private static void impact(World world, Location location) {
        world.spawnParticle(
                Particle.EXPLOSION_EMITTER,
                location,
                1,
                0, 0, 0,
                0
        );

        world.spawnParticle(
                Particle.FLAME,
                location,
                100,
                2.0, 0.8, 2.0,
                0.08
        );

        world.spawnParticle(
                Particle.SMOKE,
                location,
                80,
                2.2, 1.0, 2.2,
                0.05
        );

        world.spawnParticle(
                Particle.LAVA,
                location,
                25,
                1.2, 0.4, 1.2,
                0.02
        );

        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.75f);
    }
}
