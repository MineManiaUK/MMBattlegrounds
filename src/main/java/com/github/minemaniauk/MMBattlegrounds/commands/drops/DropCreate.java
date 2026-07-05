package com.github.minemaniauk.MMBattlegrounds.commands.drops;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import com.github.minemaniauk.MMBattlegrounds.drops.Drop;
import com.github.minemaniauk.MMBattlegrounds.drops.DropManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DropCreate implements CommandExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /dropcreate <name>");
            return true;
        }

        String name = args[0];

        DropManager manager = MMBattlegrounds.getInstance().getDropManager();
        List<String> dropNames = new ArrayList<>();
        for (Drop drop : manager.getExisting()) {
            dropNames.add(drop.name);
        }

        if (dropNames.contains(name)){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cThe drop &f" + name + "&calready exists"));
            return true;
        }

        if (manager.createNew(name)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l> &aCreated &7new drop &f" + name));
            return true;
        }
        else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cSomething went wrong creating new drop &f" + name));
            return false;
        }
    }
}
