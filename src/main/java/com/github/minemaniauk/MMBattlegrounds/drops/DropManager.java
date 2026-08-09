package com.github.minemaniauk.MMBattlegrounds.drops;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DropManager {

    private static final class DropEditorSession {
        private final Drop drop;
        private final Inventory inventory;

        private DropEditorSession(Drop drop, Inventory inventory) {
            this.drop = drop;
            this.inventory = inventory;
        }
    }

    public enum SpawnResult {
        STARTED,
        NO_SELECTION,
        ALREADY_ACTIVE,
        INVALID_DROP
    }

    public enum CancelResult {
        CANCELLED,
        NO_SELECTION,
        NOT_ACTIVE
    }

    private final JavaPlugin plugin;
    public final DropParticleManager particleManager;
    public HashMap<Player, Drop> selectedDrop = new HashMap<>();
    private final Map<String, DropParticleManager.ActiveArc> activeDrops = new HashMap<>();
    private final Map<UUID, DropEditorSession> activeEditorSessions = new HashMap<>();

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

        cancel(name);
        section.set(name, null);
        getPlugin().saveData();

        activeEditorSessions.entrySet().removeIf(entry ->
                entry.getValue() != null
                        && entry.getValue().drop != null
                        && entry.getValue().drop.name.equalsIgnoreCase(name)
        );

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

    public boolean openInventoryEditor(Player player) {
        Drop drop = getSelectedDrop(player);

        if (drop == null) {
            return false;
        }

        Inventory editor = drop.createEditorInventory();
        player.openInventory(editor);
        activeEditorSessions.put(player.getUniqueId(), new DropEditorSession(drop, editor));
        return true;
    }

    public void handleInventoryClose(Player player, Inventory inventory) {
        if (player == null || inventory == null) {
            return;
        }

        DropEditorSession session = activeEditorSessions.get(player.getUniqueId());

        if (session == null || session.inventory != inventory) {
            return;
        }

        activeEditorSessions.remove(player.getUniqueId());

        if (getDrop(session.drop.name) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes(
                    '&',
                    "&c&l> &cThe selected drop no longer exists"
            ));
            return;
        }

        session.drop.setInventory(inventory);
        player.sendMessage(ChatColor.translateAlternateColorCodes(
                '&',
                "&7&l> &aSaved &7the drop inventory"
        ));
    }

    public SpawnResult spawnSelectedDrop(Player player) {
        Drop drop = getSelectedDrop(player);

        if (drop == null) {
            return SpawnResult.NO_SELECTION;
        }

        return spawn(drop);
    }

    public CancelResult cancelSelectedDrop(Player player) {
        Drop drop = getSelectedDrop(player);

        if (drop == null) {
            return CancelResult.NO_SELECTION;
        }

        return cancel(drop.name);
    }

    public SpawnResult spawn(Drop drop) {
        if (drop == null) {
            return SpawnResult.NO_SELECTION;
        }

        String key = activeDropKey(drop.name);

        if (activeDrops.containsKey(key)) {
            return SpawnResult.ALREADY_ACTIVE;
        }

        DropParticleManager.ActiveArc activeArc = drop.spawn(plugin);

        if (activeArc == null) {
            return SpawnResult.INVALID_DROP;
        }

        activeDrops.put(key, activeArc);
        activeArc.future().whenComplete((location, throwable) -> activeDrops.remove(key, activeArc));
        getPlugin().getRestictionManager().enableAllForDuration(
                getPlugin().getRestictionManager().getDropKeepInventoryDurationTicks()
        );
        return SpawnResult.STARTED;
    }

    public CancelResult cancel(String name) {
        if (name == null || name.isBlank()) {
            return CancelResult.NOT_ACTIVE;
        }

        String key = activeDropKey(name);
        DropParticleManager.ActiveArc activeArc = activeDrops.remove(key);

        if (activeArc == null || !activeArc.cancel()) {
            return CancelResult.NOT_ACTIVE;
        }

        if (activeDrops.isEmpty()) {
            getPlugin().getRestictionManager().disableAll();
        }

        notifyDropCancelled(name);
        return CancelResult.CANCELLED;
    }

    private void notifyDropCancelled(String name) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(
                    ChatColor.RED + "DROP CANCELLED",
                    ChatColor.YELLOW + "The drop was called off",
                    10,
                    50,
                    20
            );
            player.sendMessage(ChatColor.translateAlternateColorCodes(
                    '&',
                    "&7&l> &cThe supply drop was cancelled"
            ));
        }
    }

    private String activeDropKey(String name) {
        return name.toLowerCase(Locale.ROOT);
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
