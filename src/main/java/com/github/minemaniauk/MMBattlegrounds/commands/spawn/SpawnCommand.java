package com.github.minemaniauk.MMBattlegrounds.commands.spawn;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import com.github.minemaniauk.MMBattlegrounds.TeleportHelper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class SpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (sender instanceof Player p) {
            Location location = MMBattlegrounds.getInstance().getData().getLocation("spawn-location");
            if (location == null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cNo spawn is set"));
                return true;
            }

            TeleportHelper.teleport(p, location, false);
            return true;
        }
        sender.sendMessage("Only players can run this command");
        return false;
    }
}
