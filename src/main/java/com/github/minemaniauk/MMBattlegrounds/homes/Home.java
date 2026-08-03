package com.github.minemaniauk.MMBattlegrounds.homes;

import com.github.minemaniauk.MMBattlegrounds.TeleportHelper;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Home {
    public Player owner;
    public String name;
    public Location location;

    public Home(Player owner, String name, Location location) {
        this.owner = owner;
        this.name = name;
        this.location = location;
    }

    public void teleport() {
        teleport(owner);
    }

    public void teleport(Player player) {
        TeleportHelper.teleport(player, location, false);
    }
}
