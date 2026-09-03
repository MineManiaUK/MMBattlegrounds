package com.github.minemaniauk.MMBattlegrounds;

import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.team.TeamManager;
import com.github.minemaniauk.MMBattlegrounds.drops.Drop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ScoreboardManager {

    public ScoreboardLibrary library;
    public Map<UUID, Sidebar> sidebars = new HashMap<>();

    public ScoreboardManager() {
        library = getPlugin().getScoreboardLibrary();
    }

    public void AddPlayerScoreBoard(Player player) {
        Sidebar sidebar = library.createSidebar();
        sidebar.title(
                Component.text("Breakneck ", NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD)
                        .append(
                                Component.text("BG", NamedTextColor.GOLD)
                                        .decoration(TextDecoration.BOLD, false)
                        )
        );

        sidebar.line(0, Component.empty());
        sidebar.line(1, Component.text(player.getName()).color(NamedTextColor.YELLOW));
        sidebar.line(
                2,
                Component.text("Kills: ", NamedTextColor.RED)
                        .append(Component.text(
                                getPlugin().loadPlayerData(player).getInt("kills", 0),
                                NamedTextColor.DARK_RED
                        ))
        );

        if (getPlugin().getConfig().getBoolean("damage-balance-enabled")) {
            int balanceLevel = getPlugin()
                    .getBalanceSystemManager()
                    .getPermanentBalanceLevel(player);

            Component damageLevel;

            if (balanceLevel > 0) {
                damageLevel = Component.text("Strength " + balanceLevel)
                        .color(NamedTextColor.GREEN);

            } else if (balanceLevel < 0) {
                damageLevel = Component.text("Weakness " + Math.abs(balanceLevel))
                        .color(NamedTextColor.DARK_RED);

            } else {
                damageLevel = Component.text("Neutral")
                        .color(NamedTextColor.GRAY);
            }

            sidebar.line(
                    3,
                    Component.text("Damage Level: ")
                            .color(NamedTextColor.RED)
                            .append(damageLevel)
            );
        }

        sidebar.line(4, getTeamComponent(player));

        sidebar.line(4, getTeamComponent(player));

        sidebar.line(5, Component.empty());

        sidebar.line(6, getDropStatus());
        sidebar.line(7, getDropLocation());

        sidebar.line(8, Component.empty());

        sidebar.line(9, Component.text("Loading...").color(NamedTextColor.RED));
        sidebar.line(10, Component.text("Loading...").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));

        sidebars.put(player.getUniqueId(), sidebar);
        sidebar.addPlayer(player);
    }

    public void removePlayerScoreBoard(Player player) {
        Sidebar sidebar = sidebars.get(player.getUniqueId());
        sidebar.close();
        sidebars.remove(player.getUniqueId());
    }

    public void updateTime(long remainingTime, GamePhase phase) {
        long remainingTimeSeconds = Math.max(0L, remainingTime / 1000L);

        switch (phase) {
            case NORMAL:
                if (remainingTimeSeconds == 0L) {
                    updatePhaseLines(
                            Component.text("Waiting for sudden death...").color(NamedTextColor.RED),
                            Component.text("0d 0h 0m").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                    );
                    return;
                }

                long normDays = remainingTimeSeconds / 86400;
                long normHours = (remainingTimeSeconds % 86400) / 3600;
                long normMinutes = (remainingTimeSeconds % 3600) / 60;
                long normSeconds = remainingTimeSeconds % 60;

                updatePhaseLines(
                        Component.text("Sudden Death in:").color(NamedTextColor.RED),
                        Component.text(normDays + "d " + normHours + "h " + normMinutes + "m " + normSeconds + "s")
                                .color(NamedTextColor.GREEN)
                                .decorate(TextDecoration.BOLD)
                );
                break;

            case SUDDEN_DEATH:
                long sdMinutes = remainingTimeSeconds / 60;
                long sdSeconds = remainingTimeSeconds % 60;

                updatePhaseLines(
                        Component.text("Teams disband in:").color(NamedTextColor.RED),
                        Component.text(sdMinutes + "m " + sdSeconds + "s")
                                .color(NamedTextColor.GREEN)
                                .decorate(TextDecoration.BOLD)
                );
                break;

            case SUDDEN_DEATH_NO_TEAMS:
                updatePhaseLines(
                        Component.text("Last stand").color(NamedTextColor.RED).decorate(TextDecoration.BOLD),
                        Component.text("Everyone for themselves").color(NamedTextColor.DARK_RED)
                );
                break;

            case GAME_OVER:
                updatePhaseLines(
                        Component.text("GAME OVER").color(NamedTextColor.RED).decorate(TextDecoration.BOLD),
                        Component.text("Thanks for playing").color(NamedTextColor.GRAY)
                );
                break;
        }
    }

    private void updatePhaseLines(Component phaseLine, Component timeLine) {
        for (Sidebar sidebar : sidebars.values()) {
            sidebar.line(9, phaseLine);
            sidebar.line(10, timeLine);
        }
    }

    public void updateDropStatuses() {
        for (Sidebar sidebar : sidebars.values()) {
            sidebar.line(6, getDropStatus());
            sidebar.line(7, getDropLocation());
        }
    }

    public void updatePlayerStatuses() {
        for (Map.Entry<UUID, Sidebar> entry : sidebars.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            Sidebar sidebar = entry.getValue();

            sidebar.line(1, Component.text(p.getName()).color(NamedTextColor.YELLOW));
            sidebar.line(
                    2,
                    Component.text("Kills: ", NamedTextColor.RED)
                            .append(Component.text(
                                    getPlugin().loadPlayerData(p).getInt("kills", 0),
                                    NamedTextColor.DARK_RED
                            ))
            );

            if (getPlugin().getConfig().getBoolean("damage-balance-enabled")) {
                int balanceLevel = getPlugin()
                        .getBalanceSystemManager()
                        .getPermanentBalanceLevel(p);

                Component damageLevel;

                if (balanceLevel > 0) {
                    damageLevel = Component.text("Strength " + balanceLevel)
                            .color(NamedTextColor.GREEN);

                } else if (balanceLevel < 0) {
                    damageLevel = Component.text("Weakness " + Math.abs(balanceLevel))
                            .color(NamedTextColor.DARK_RED);

                } else {
                    damageLevel = Component.text("Neutral")
                            .color(NamedTextColor.GRAY);
                }

                sidebar.line(
                        3,
                        Component.text("Damage Level: ")
                                .color(NamedTextColor.RED)
                                .append(damageLevel)
                );
            }

            sidebar.line(4, getTeamComponent(p));
        }
    }

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component getTeamComponent(Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("BetterTeams")) {
            return Component.text("No Team", NamedTextColor.GRAY);
        }

        Team team = Team.getTeam(player);

        if (team == null) {
            return Component.text("No Team", NamedTextColor.GRAY);
        }

        return MINI_MESSAGE.deserialize(team.getTag());
    }

    private Component getDropStatus() {
        String lastDropName = getPlugin().getData().getString("last-drop-name");

        if (getPlugin().getDropManager().isDropActive()) {
            return Component.text("Drop Inbound...").color(NamedTextColor.RED);
        }

        if (getPlugin().getDropManager().wasLastDropCancelled()) {
            return Component.text("Drop Cancelled...").color(NamedTextColor.DARK_RED);
        }

        if (lastDropName == null || lastDropName.isEmpty()) {
            return Component.text("No Drop").color(NamedTextColor.RED);
        }

        return Component.text("Drop Landed...").color(NamedTextColor.RED);
    }

    private Component getDropLocation() {
        String lastDropName = getPlugin().getData().getString("last-drop-name");

        if (lastDropName == null || lastDropName.isEmpty() || getPlugin().getDropManager().wasLastDropCancelled()) {
            return Component.text("X:- Z:-").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
        }

        Drop drop = getPlugin().getDropManager().getDrop(lastDropName);
        if (drop == null) {
            return Component.text("X:- Z:-").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
        }

        Location dropLocation = drop.location;
        return Component.text("X:" + dropLocation.getBlockX() + " Z:" + dropLocation.getBlockZ()).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
    }

    private MMBattlegrounds getPlugin() {
        return MMBattlegrounds.getInstance();
    }
}
