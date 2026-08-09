package com.github.minemaniauk.MMBattlegrounds.homes.commands;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import com.github.minemaniauk.MMBattlegrounds.homes.Home;
import com.github.minemaniauk.MMBattlegrounds.homes.HomeData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SetHome implements TabExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (sender instanceof Player p) {
            if (args.length != 1) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cUsage: /sethome <name>"));
                return true;
            }

            HomeData data = HomeData.load(p);
            String name = args[0];

            if (p.getLocation().clone().subtract(0, 1, 0).getBlock().getType() == Material.AIR) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cYou can not create a home without a block below you"));
                return true;
            }

            if (data.getHome(name) != null) {
                Home oldHome = data.getHome(name);
                Home newHome = new Home(p, name, p.getLocation());
                data.removeHome(oldHome);
                data.addHome(newHome);

                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&l> &aMoved home &f" + newHome.name));
                return true;
            }

            if (!sender.hasPermission("mmbattlegrounds.homes.unlimited")) {
                int maxHomes = MMBattlegrounds.getInstance().getConfig().getInt("max-homes", 3);
                if (data.homes.size() >= maxHomes) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cYou already have the max amount of homes"));
                    return true;
                }
            }

            Home newHome = new Home(p, name, p.getLocation());
            data.addHome(newHome);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&l> &aCreated home &f" + newHome.name));
            return true;
        }
        sender.sendMessage("Only players can run this command");
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!(sender instanceof Player player) || args.length != 1) {
            return List.of();
        }

        String prefix = args[0].toLowerCase();

        return HomeData.load(player).homes.keySet().stream()
                .filter(home -> home.toLowerCase().startsWith(prefix))
                .sorted()
                .toList();
    }
}
