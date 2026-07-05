package com.github.minemaniauk.MMBattlegrounds.drops;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DropManager {

    private final JavaPlugin plugin;
    public final DropParticleManager particleManager;
    public HashMap<Player, Drop> selectedDrop = new HashMap<>();

    public DropManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.particleManager = new DropParticleManager(this.plugin);

        getOrCreateDropsSection();
    }

    public boolean selectDrop(Player player, String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        ConfigurationSection section = getOrCreateDropsSection();

        if (!section.isConfigurationSection(name)) {
            return false;
        }

        selectedDrop.put(player, new Drop(name));
        return true;
    }

    public boolean createNew(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        ConfigurationSection section = getOrCreateDropsSection();

        if (section.isSet(name)) {
            return false;
        }

        section.createSection(name);
        getPlugin().saveData();

        return true;
    }

    public boolean remove(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        ConfigurationSection section = getDropsSection();

        if (section == null || !section.isSet(name)) {
            return false;
        }

        section.set(name, null);
        getPlugin().saveData();

        selectedDrop.entrySet().removeIf(entry ->
                entry.getValue() != null
                        && entry.getValue().name.equalsIgnoreCase(name)
        );

        return true;
    }

    public List<Drop> getExisting() {
        List<Drop> drops = new ArrayList<>();

        ConfigurationSection section = getDropsSection();

        if (section == null) {
            return drops;
        }

        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                drops.add(new Drop(key));
            }
        }

        return drops;
    }

    public Drop getDrop(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        ConfigurationSection section = getDropsSection();

        if (section == null || !section.isConfigurationSection(name)) {
            return null;
        }

        return new Drop(name);
    }

    public Drop getSelectedDrop(Player player) {
        return selectedDrop.get(player);
    }

    private ConfigurationSection getDropsSection() {
        return getPlugin().getData().getConfigurationSection("drops");
    }

    private ConfigurationSection getOrCreateDropsSection() {
        FileConfiguration data = getPlugin().getData();

        ConfigurationSection section = data.getConfigurationSection("drops");

        if (section != null) {
            return section;
        }

        if (data.isSet("drops")) {
            data.set("drops", null);
        }

        section = data.createSection("drops");
        getPlugin().saveData();

        return section;
    }

    private MMBattlegrounds getPlugin() {
        return MMBattlegrounds.getInstance();
    }
}