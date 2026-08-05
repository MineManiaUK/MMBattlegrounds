package com.github.minemaniauk.MMBattlegrounds.commands.spawn;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class SetSpawn implements CommandExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (sender.hasPermission("mmbattlegrounds.setspawn")) {
            if (sender instanceof Player p) {
                Location location = p.getLocation();

                MMBattlegrounds.getInstance().getData().set("spawn-location", location);
                p.sendMessage("&7&l> &7Set spawn");
                return true;
            }
            sender.sendMessage("Only players can run this command");
            return false;
        }
        else {
            sender.sendMessage("No permission");
            return false;
        }
    }
}
