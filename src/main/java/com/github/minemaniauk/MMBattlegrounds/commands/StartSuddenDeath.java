package com.github.minemaniauk.MMBattlegrounds.commands;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartSuddenDeath implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mmbattlegrounds.suddendeath.start")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have permission to use this command"));
            return true;
        }

        MMBattlegrounds.getInstance().startSuddenDeath();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l> &aStarting &7sudden death"));
        return true;
    }
}
