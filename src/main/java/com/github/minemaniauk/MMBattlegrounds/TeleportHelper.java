package com.github.minemaniauk.MMBattlegrounds;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TeleportHelper implements Listener {

    private static final Map<UUID, BukkitTask> countdowns = new HashMap<>();

    private TeleportHelper() {
    }

    public static void register(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new TeleportHelper(), plugin);
    }

    public static void teleport(Player player, Location location, boolean skipCooldown) {
        cancelCountdown(player);

        if (location == null || location.getWorld() == null) {
            sendMessage(player, "&c&l> &cThat destination is unavailable");
            return;
        }

        if (MMBattlegrounds.getInstance().isTagged(player)) {
            sendMessage(player, "&c&l> &cYou are in combat");
            return;
        }

        if (location.clone().subtract(0, 1, 0).getBlock().getType() == Material.AIR) {
            location.setY(location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 1);
            sendMessage(player, "&c&lWARNING> &cThe destination could be unsafe. &c&lMove now to cancel the teleport");
        }

        if (skipCooldown || player.hasPermission("mmbattlegrounds.bypass.teleportdelay")) {
            completeTeleport(player, location);
            return;
        }

        JavaPlugin plugin = MMBattlegrounds.getInstance();
        int teleportDelay = Math.max(0, plugin.getConfig().getInt("teleport-delay", 5));

        if (teleportDelay == 0) {
            completeTeleport(player, location);
            return;
        }

        BukkitTask task = new BukkitRunnable() {
            private int remaining = teleportDelay;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    countdowns.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                if (remaining <= 0) {
                    completeTeleport(player, location);
                    return;
                }

                sendMessage(player, "&6&l> &6Teleporting in: " + remaining + "...");
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        countdowns.put(player.getUniqueId(), task);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }

        // Ignore head rotation; cancel only when X, Y, or Z changes.
        boolean moved =
                from.getX() != to.getX()
                        || from.getY() != to.getY()
                        || from.getZ() != to.getZ();

        if (!moved) {
            return;
        }

        Player player = event.getPlayer();

        if (cancelCountdown(player)) {
            sendMessage(player, "&c&l> &cTeleport cancelled because you moved");
        }
    }

    private static boolean cancelCountdown(Player player) {
        BukkitTask task = countdowns.remove(player.getUniqueId());

        if (task == null) {
            return false;
        }

        task.cancel();
        return true;
    }

    private static void completeTeleport(Player player, Location location) {
        player.teleport(location);
        sendMessage(player, "&a&l> &aTeleported");

        BukkitTask task = countdowns.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private static void sendMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
