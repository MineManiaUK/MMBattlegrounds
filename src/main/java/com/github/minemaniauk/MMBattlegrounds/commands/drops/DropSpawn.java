package com.github.minemaniauk.MMBattlegrounds.commands.drops;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import com.github.minemaniauk.MMBattlegrounds.drops.DropManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class DropSpawn implements CommandExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (sender instanceof Player player) {
            DropManager manager = MMBattlegrounds.getInstance().getDropManager();

            if (manager.getSelectedDrop(player) == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&l> &cA drop is not selected"));
                return true;
            }

            manager.getSelectedDrop(player).spawn(MMBattlegrounds.getInstance());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l> &aSpawned &7the drop " + manager.getSelectedDrop(player).name));
            return true;
        }
        else {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player");
            return true;
        }
    }
}
