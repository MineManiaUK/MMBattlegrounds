package com.github.minemaniauk.MMBattlegrounds.commands;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jspecify.annotations.NonNull;

public class DropTimeTable implements CommandExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        FileConfiguration config = MMBattlegrounds.getInstance().getConfig();
        if (config.getBoolean("show-drop-timetable")) {
            String timeTableLink = config.getString("drop-timetable-link");
            if (timeTableLink != null && !timeTableLink.isEmpty()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&l> &a&lCheck the drop time table to know when valuable supply drops are: &f" + timeTableLink));
            }
        }


        return true;
    }
}
