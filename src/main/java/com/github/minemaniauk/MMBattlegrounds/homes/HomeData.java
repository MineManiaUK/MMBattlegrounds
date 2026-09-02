package com.github.minemaniauk.MMBattlegrounds.homes;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class HomeData {
    public Player player;
    public HashMap<String, Home> homes = new HashMap<>();

    public HomeData(Player player, HashMap<String, Home> homes) {
        this.player = player;
        this.homes = homes;
    }

    public void addHome(Home home) {
        homes.put(home.name, home);
        save();
    }

    public Home getHome(String name) {
        return homes.getOrDefault(name, null);
    }

    public void removeHome(Home home) {
        homes.remove(home.name);
        save();
    }

    public void save() {
        FileConfiguration dataFile = new YamlConfiguration();

        for (Map.Entry<String, Home> entry : homes.entrySet()) {
            dataFile.set(entry.getKey() + ".location", entry.getValue().location);
        }

        MMBattlegrounds.getInstance().savePlayerData(player, dataFile);
    }

    public static HomeData load(Player player) {
        FileConfiguration dataFile = MMBattlegrounds.getInstance().loadPlayerData(player);

        HashMap<String, Home> homeMap = new HashMap<>();
        for (String key : dataFile.getKeys(false)) {
            ConfigurationSection section = dataFile.getConfigurationSection(key);
            if (section == null || !section.isLocation("location")) {
                continue;
            }

            homeMap.put(key, new Home(player, key, section.getLocation("location")));
        }

        return new HomeData(player, homeMap);
    }
}
