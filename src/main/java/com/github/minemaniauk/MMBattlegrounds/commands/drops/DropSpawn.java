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

            switch (manager.spawnSelectedDrop(player)) {
                case NO_SELECTION -> sender.sendMessage(ChatColor.translateAlternateColorCodes(
                        '&',
                        "&c&l> &cA drop is not selected"
                ));
                case ALREADY_ACTIVE -> sender.sendMessage(ChatColor.translateAlternateColorCodes(
                        '&',
                        "&c&l> &cThat drop is already inbound"
                ));
                case INVALID_DROP -> sender.sendMessage(ChatColor.translateAlternateColorCodes(
                        '&',
                        "&c&l> &cThe selected drop could not be spawned"
                ));
                case STARTED -> sender.sendMessage(ChatColor.translateAlternateColorCodes(
                        '&',
                        "&7&l> &aSpawned &7the drop " + manager.getSelectedDrop(player).name
                ));
            }
            return true;
        }
        else {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player");
            return true;
        }
    }
}
