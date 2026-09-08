package com.github.minemaniauk.MMBattlegrounds.commands;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class PlayerDataCommand implements TabExecutor {

    List<String> arg2Completions = List.of("get", "set");
    List<String> arg3Completions = List.of("deaths", "kills", "tune");

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {

        if (args.length < 2) {
            sendUsage(sender);
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find player " + args[0]);
            return true;
        }

        if (!arg2Completions.contains(args[1])) {
            sendUsage(sender);
            return true;
        }

        switch (args[1]) {
            case "get" -> {
                if (args.length < 3) {
                    sendUsage(sender);
                    return true;
                }

                int value = getPlayerDataFile(player).getInt(args[2], 0);

                sender.sendMessage(
                        ChatColor.GREEN + player.getName()
                                + ChatColor.WHITE + " " + args[2]
                                + ChatColor.GREEN + " value is: "
                                + ChatColor.WHITE + value
                );

                return true;
            }

            case "set" -> {
                if (args.length < 4) {
                    sendUsage(sender);
                    return true;
                }

                try {
                    int newValue = Integer.parseInt(args[3]);

                    FileConfiguration data = getPlayerDataFile(player);
                    data.set(args[2], newValue);
                    savePlayerDataFile(player, data);
                    MMBattlegrounds.getInstance().getScoreboardManager().updatePlayerStatuses();

                    sender.sendMessage(
                            ChatColor.GREEN + "Successfully set "
                                    + ChatColor.WHITE + player.getName()
                                    + " " + args[2]
                                    + ChatColor.GREEN + " to "
                                    + ChatColor.WHITE + newValue
                    );

                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid number input.");
                }

                return true;
            }

            default -> {
                sendUsage(sender);
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        List<String> arg2Completions = List.of("get", "set");
        List<String> arg3Completions = List.of("deaths", "kills", "tune");

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();

            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.startsWith(prefix))
                    .toList();
        }

        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            return arg2Completions.stream()
                    .filter(s -> s.startsWith(prefix))
                    .toList();
        }

        if (args.length == 3) {
            String prefix = args[2].toLowerCase();
            return arg3Completions.stream()
                    .filter(s -> s.startsWith(prefix))
                    .toList();
        }

        return List.of();
    }

    private FileConfiguration getPlayerDataFile(Player player) {
        return MMBattlegrounds.getInstance().loadPlayerData(player);
    }

    private void savePlayerDataFile(Player player, FileConfiguration data) {
        MMBattlegrounds.getInstance().savePlayerData(player, data);
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("Usage: /mmbattplayerdata <Player> <get|set> <deaths|kills|tune> [value]");
    }
}
