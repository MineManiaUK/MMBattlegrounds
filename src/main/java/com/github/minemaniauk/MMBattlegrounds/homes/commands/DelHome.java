package com.github.minemaniauk.MMBattlegrounds.homes.commands;

import com.github.minemaniauk.MMBattlegrounds.homes.Home;
import com.github.minemaniauk.MMBattlegrounds.homes.HomeData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class DelHome implements TabExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (sender instanceof Player p) {
            if (args.length != 1) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cUsage: /delhome <name>"));
                return true;
            }

            String name = args[0];
            HomeData data = HomeData.load(p);
            Home home = data.getHome(name);
            if (home == null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cHome &f%s &cdoes not exist").formatted(name));
                return true;
            }

            data.removeHome(home);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&l> &aDeleted home &f%s").formatted(name));
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
