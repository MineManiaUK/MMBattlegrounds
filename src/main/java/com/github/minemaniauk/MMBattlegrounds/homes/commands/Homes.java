package com.github.minemaniauk.MMBattlegrounds.homes.commands;

import com.github.minemaniauk.MMBattlegrounds.homes.HomeData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class Homes implements CommandExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (sender instanceof Player p) {
            List<String> homeNames = HomeData.load(p).homes.keySet().stream()
                    .sorted()
                    .toList();

            if (homeNames.isEmpty()) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cYou do not have any homes set"));
                return true;
            }

            StringBuilder output = new StringBuilder("&7&l> &7List of your homes:\n");
            for (String s : homeNames) {
                output.append("&f").append(s).append("\n");
            }
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', output.toString()));
            return true;
        }
        sender.sendMessage("Only players can run this command");
        return false;
    }
}
