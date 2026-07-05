package com.github.minemaniauk.MMBattlegrounds.commands.drops;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import com.github.minemaniauk.MMBattlegrounds.drops.DropManager;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NonNull;

public class DropSetItems implements CommandExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (sender instanceof Player player) {
            Block block = player.getTargetBlockExact(5);
            if (block.getState() instanceof Chest chest) {
                DropManager manager = MMBattlegrounds.getInstance().getDropManager();
                Inventory targetInventory = chest.getInventory();

                if (manager.getSelectedDrop(player) == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cA drop is not selected"));
                    return true;
                }

                manager.getSelectedDrop(player).setInventory(targetInventory);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l> &aSet &7the drops inventory"));
                return true;

            }
            else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cYou are not looking at a chest"));
                return true;
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player");
            return true;
        }
    }
}
