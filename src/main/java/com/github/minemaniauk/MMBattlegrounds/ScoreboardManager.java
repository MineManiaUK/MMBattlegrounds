package com.github.minemaniauk.MMBattlegrounds;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {

    public Scoreboard scoreboard;
    public Objective phaseObjective;
    public Objective timeObjective;

    public ScoreboardManager() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        phaseObjective = scoreboard.registerNewObjective("phase", Criteria.DUMMY, ChatColor.translateAlternateColorCodes('&', "&c&lPhase: &4None"));
       timeObjective = scoreboard.registerNewObjective("time", Criteria.DUMMY, ChatColor.translateAlternateColorCodes('&', "&a&lTime remaining: &4None"));
    }

    public void AddPlayerScoreBoard(Player player) {
        player.setScoreboard(scoreboard);
    }

    private String lastTimeLine = null;

    public void update(long remainingTime, GamePhase phase) {
        long remainingTimeSeconds = Math.max(0L, remainingTime / 1000L);

        if (lastTimeLine != null) {
            phaseObjective.getScoreboard().resetScores(lastTimeLine);
            lastTimeLine = null;
        }

        switch (phase) {
            case NORMAL:
                if (remainingTimeSeconds == 0L) {
                    phaseObjective.setDisplayName(ChatColor.RED + "Waiting for sudden death...");

                    lastTimeLine = ChatColor.GREEN.toString() + ChatColor.BOLD + "0d 0h 0m";
                    phaseObjective.getScore(lastTimeLine).setScore(1);

                    phaseObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
                    return;
                }

                long normDays = remainingTimeSeconds / 86400;
                long normHours = (remainingTimeSeconds % 86400) / 3600;
                long normMinutes = (remainingTimeSeconds % 3600) / 60;
                long normSeconds = remainingTimeSeconds % 60;

                phaseObjective.setDisplayName(ChatColor.RED + "Sudden Death in:");

                lastTimeLine = ChatColor.GREEN.toString() + ChatColor.BOLD
                        + normDays + "d " + normHours + "h " + normMinutes + "m " + normSeconds + "s";

                phaseObjective.getScore(lastTimeLine).setScore(1);
                phaseObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
                break;

            case SUDDEN_DEATH:
                long sdMinutes = remainingTimeSeconds / 60;
                long sdSeconds = remainingTimeSeconds % 60;

                phaseObjective.setDisplayName(ChatColor.RED + "Teams disband in:");

                lastTimeLine = ChatColor.GREEN.toString() + ChatColor.BOLD
                        + sdMinutes + "m " + sdSeconds + "s";

                phaseObjective.getScore(lastTimeLine).setScore(1);
                phaseObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
                break;

            case SUDDEN_DEATH_NO_TEAMS:
                phaseObjective.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Last stand");

                lastTimeLine = ChatColor.DARK_RED + "Everyone for themselves";
                phaseObjective.getScore(lastTimeLine).setScore(1);

                phaseObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
                break;

            case GAME_OVER:
                phaseObjective.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "GAME OVER");

                lastTimeLine = ChatColor.GRAY + "Thanks for playing";
                phaseObjective.getScore(lastTimeLine).setScore(1);

                phaseObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
                break;
        }
    }

}
