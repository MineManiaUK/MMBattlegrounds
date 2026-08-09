package com.github.minemaniauk.MMBattlegrounds.homes;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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
        File playerFile = getPlayerFile(player);

        try {
            File parent = playerFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Failed to create home data folder at " + parent.getAbsolutePath());
            }

            FileConfiguration dataFile = new YamlConfiguration();

            for (Map.Entry<String, Home> entry : homes.entrySet()) {
                dataFile.set(entry.getKey() + ".location", entry.getValue().location);
            }

            dataFile.save(playerFile);
        } catch (IOException e) {
            MMBattlegrounds.getInstance().getLogger().log(Level.SEVERE, "Something went wrong saving home data", e);
        }
    }

    public static HomeData load(Player player) {
        File playerFile = getPlayerFile(player);

        FileConfiguration dataFile = YamlConfiguration.loadConfiguration(playerFile);

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

    private static File getPlayerFile(Player player) {
        return new File(getHomesFolder(), player.getUniqueId() + ".yml");
    }

    private static File getHomesFolder() {
        return new File(MMBattlegrounds.getInstance().getDataFolder(), "player-data");
    }
}
