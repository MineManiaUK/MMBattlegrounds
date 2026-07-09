package com.github.minemaniauk.MMBattlegrounds.commands;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

public class ResetAllBorders implements CommandExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        double borderSize = MMBattlegrounds.getInstance().getConfiguration().getDouble("default-border-size", 5000);

        if (args.length > 0){
            try {
                borderSize = Double.parseDouble(args[0]);
            }
            catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cThe value &f" + args[0] + " &cis not a valid number input"));
                return true;
            }
        }

        for(World world : Bukkit.getWorlds()) {
            world.getWorldBorder().setSize(borderSize);
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l> &aSuccessfully &areset &7all world borders to &f" + borderSize));
        return true;
    }
}
