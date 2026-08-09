package com.github.minemaniauk.MMBattlegrounds.homes.commands;

import com.github.minemaniauk.MMBattlegrounds.TeleportHelper;
import com.github.minemaniauk.MMBattlegrounds.homes.Home;
import com.github.minemaniauk.MMBattlegrounds.homes.HomeData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class HomeAdmin implements TabExecutor {
    private static final List<String> ACTIONS = List.of(
            "list",
            "tp",
            "set",
            "delete"
    );

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return true;
        }

        String action = normaliseAction(args[0]);
        if (action == null) {
            sendUsage(sender);
            return true;
        }

        Player owner = Bukkit.getPlayerExact(args[1]);
        if (owner == null) {
            sender.sendMessage(colour("&c&l> &cPlayer &f%s &cis not online").formatted(args[1]));
            return true;
        }

        return switch (action) {
            case "list" -> handleList(sender, owner, args);
            case "tp" -> handleTeleport(sender, owner, args);
            case "set" -> handleSet(sender, owner, args);
            case "delete" -> handleDelete(sender, owner, args);
            default -> {
                sendUsage(sender);
                yield true;
            }
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return ACTIONS.stream()
                    .filter(action -> action.startsWith(prefix))
                    .toList();
        }

        if (args.length == 2) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .sorted()
                    .toList();
        }

        String action = normaliseAction(args[0]);
        if (action == null) {
            return List.of();
        }

        if (args.length == 3) {
            if ("list".equals(action)) {
                return List.of();
            }

            Player owner = Bukkit.getPlayerExact(args[1]);
            if (owner == null) {
                return List.of();
            }

            String prefix = args[2].toLowerCase(Locale.ROOT);
            return HomeData.load(owner).homes.keySet().stream()
                    .filter(home -> home.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .sorted()
                    .toList();
        }

        if (args.length == 4 && ("tp".equals(action) || "set".equals(action))) {
            String prefix = args[3].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .sorted()
                    .toList();
        }

        return List.of();
    }

    private boolean handleList(CommandSender sender, Player owner, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(colour("&c&l> &cUsage: /homeadmin list <player>"));
            return true;
        }

        List<String> homeNames = HomeData.load(owner).homes.keySet().stream()
                .sorted()
                .toList();

        if (homeNames.isEmpty()) {
            sender.sendMessage(colour("&c&l> &cPlayer &f%s &cdoes not have any homes").formatted(owner.getName()));
            return true;
        }

        StringBuilder output = new StringBuilder(colour("&7&l> &7Homes for &f" + owner.getName() + "&7:\n"));
        for (String homeName : homeNames) {
            output.append(colour("&f")).append(homeName).append("\n");
        }

        sender.sendMessage(output.toString().trim());
        return true;
    }

    private boolean handleTeleport(CommandSender sender, Player owner, String[] args) {
        if (args.length < 3 || args.length > 4) {
            sender.sendMessage(colour("&c&l> &cUsage: /homeadmin tp <player> <home> [targetPlayer]"));
            return true;
        }

        Home home = HomeData.load(owner).getHome(args[2]);
        if (home == null) {
            sender.sendMessage(colour("&c&l> &cHome &f%s &cdoes not exist for &f%s").formatted(args[2], owner.getName()));
            return true;
        }

        Player target = resolveActionPlayer(sender, args, 3, "tp");
        if (target == null) {
            return true;
        }

        TeleportHelper.teleport(target, home.location, true);
        sender.sendMessage(colour("&a&l> &aTeleporting &f%s &ato home &f%s &aowned by &f%s")
                .formatted(target.getName(), home.name, owner.getName()));
        return true;
    }

    private boolean handleSet(CommandSender sender, Player owner, String[] args) {
        if (args.length < 3 || args.length > 4) {
            sender.sendMessage(colour("&c&l> &cUsage: /homeadmin set <player> <home> [locationPlayer]"));
            return true;
        }

        Player locationPlayer = resolveActionPlayer(sender, args, 3, "set");
        if (locationPlayer == null) {
            return true;
        }

        HomeData data = HomeData.load(owner);
        Home existing = data.getHome(args[2]);
        if (existing != null) {
            data.removeHome(existing);
        }

        Location location = locationPlayer.getLocation().clone();
        data.addHome(new Home(owner, args[2], location));

        sender.sendMessage(colour("&a&l> &aSet home &f%s &afor &f%s &aat &f%s")
                .formatted(args[2], owner.getName(), locationPlayer.getName()));
        return true;
    }

    private boolean handleDelete(CommandSender sender, Player owner, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(colour("&c&l> &cUsage: /homeadmin delete <player> <home>"));
            return true;
        }

        HomeData data = HomeData.load(owner);
        Home home = data.getHome(args[2]);
        if (home == null) {
            sender.sendMessage(colour("&c&l> &cHome &f%s &cdoes not exist for &f%s").formatted(args[2], owner.getName()));
            return true;
        }

        data.removeHome(home);
        sender.sendMessage(colour("&a&l> &aDeleted home &f%s &afor &f%s").formatted(args[2], owner.getName()));
        return true;
    }

    private @Nullable Player resolveActionPlayer(CommandSender sender, String[] args, int playerIndex, String action) {
        if (args.length > playerIndex) {
            Player target = Bukkit.getPlayerExact(args[playerIndex]);
            if (target == null) {
                sender.sendMessage(colour("&c&l> &cPlayer &f%s &cis not online").formatted(args[playerIndex]));
                return null;
            }
            return target;
        }

        if (sender instanceof Player player) {
            return player;
        }

        sender.sendMessage(colour("&c&l> &cConsole must provide the extra player argument for &f%s").formatted(action));
        return null;
    }

    private @Nullable String normaliseAction(String action) {
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "list" -> "list";
            case "tp" -> "tp";
            case "set" -> "set";
            case "delete" -> "delete";
            default -> null;
        };
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(colour("&c&l> &cUsage:"));
        sender.sendMessage(colour("&f/homeadmin list <player>"));
        sender.sendMessage(colour("&f/homeadmin tp <player> <home> [targetPlayer]"));
        sender.sendMessage(colour("&f/homeadmin set <player> <home> [locationPlayer]"));
        sender.sendMessage(colour("&f/homeadmin delete <player> <home>"));
    }

    private String colour(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
