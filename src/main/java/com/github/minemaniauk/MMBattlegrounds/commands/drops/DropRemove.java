package com.github.minemaniauk.MMBattlegrounds.commands.drops;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import com.github.minemaniauk.MMBattlegrounds.drops.Drop;
import com.github.minemaniauk.MMBattlegrounds.drops.DropManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DropRemove implements TabExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        String name = args[0];

        DropManager manager = MMBattlegrounds.getInstance().getDropManager();
        List<String> dropNames = new ArrayList<>();
        for (Drop drop : manager.getExisting()) {
            dropNames.add(drop.name);
        }

        if (!dropNames.contains(name)){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cThe drop &f" + name + "&cdoes not exist"));
            return true;
        }

        if (manager.remove(name)){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l> &cRemoved &7drop &f" + name));
            return true;
        }
        else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cSomething went wrong removing drop &f" + name));
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        DropManager manager = MMBattlegrounds.getInstance().getDropManager();

        List<String> completions = new ArrayList<>();
        for (Drop drop : manager.getExisting()) {
            completions.add(drop.name);
        }

        if (args.length == 0) {
            return completions;
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return completions.stream()
                    .filter(s -> s.startsWith(prefix))
                    .toList();
        }

        return List.of();
    }
}
