package com.github.minemaniauk.MMBattlegrounds;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BalanceSystemManager implements Listener {

    public BalanceSystemManager(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public int getPermanentBalanceLevel(Player player) {
        FileConfiguration playerConfig =
                MMBattlegrounds.getInstance().loadPlayerData(player);

        int zeroRange =
                getConfig().getInt("damage-balance-effects-zero-range");

        int effectMultiple =
                getConfig().getInt("damage-balance-effects-multiplier");

        int maxLevel =
                getConfig().getInt("damage-balance-effects-max-level");

        int kills = playerConfig.getInt("kills");
        int deaths = playerConfig.getInt("deaths");

        int netKills = kills - deaths;
        int distance = Math.abs(netKills);

        if (distance < zeroRange + effectMultiple) {
            return 0;
        }

        int level = (distance - zeroRange) / effectMultiple;

        level = Math.min(level, maxLevel);

        return netKills > 0 ? -level : level;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (!getConfig().getBoolean("damage-balance-enabled")) {
            return;
        }

        int balanceLevel = getPermanentBalanceLevel(player);

        if (balanceLevel == 0) {
            return;
        }

        double damagePerLevel =
                getConfig().getDouble("damage-balance-damage-per-level", 0.10);

        double multiplier;

        if (balanceLevel > 0) {
            // Increased damage
            multiplier = 1.0 + (balanceLevel * damagePerLevel);
        } else {
            // Reduced damage
            multiplier = 1.0 - (Math.abs(balanceLevel) * damagePerLevel);
        }

        multiplier = Math.max(multiplier, 0.10);

        event.setDamage(event.getDamage() * multiplier);
    }

    private FileConfiguration getConfig() {
        return MMBattlegrounds.getInstance().getConfig();
    }
}