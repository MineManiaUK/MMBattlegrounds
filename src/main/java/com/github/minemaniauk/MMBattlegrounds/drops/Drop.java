package com.github.minemaniauk.MMBattlegrounds.drops;

import com.github.minemaniauk.MMBattlegrounds.MMBattlegrounds;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class Drop {

    public final String name;
    public Location location;
    public Inventory inventory;

    private final MMBattlegrounds plugin;

    public Drop(String name) {
        this.name = name;
        this.plugin = MMBattlegrounds.getInstance();

        FileConfiguration data = plugin.getData();

        this.location = loadLocation(data, getLocationPath());
        this.inventory = loadInventory(data, getInventoryPath(), name + " Supply Drop");
    }

    private String getBasePath() {
        return "drops." + name;
    }

    private String getLocationPath() {
        return getBasePath() + ".location";
    }

    private String getInventoryPath() {
        return getBasePath() + ".inventory";
    }

    public void setLocation(Location location) {
        this.location = location;

        saveLocation(plugin.getData(), getLocationPath(), location);

        plugin.saveData();
    }

    public void setInventory(Inventory sourceInventory) {
        if (sourceInventory == null) {
            this.inventory = null;
            saveInventory(plugin.getData(), getInventoryPath(), null);
            plugin.saveData();
            return;
        }

        Inventory copy = Bukkit.createInventory(
                null,
                sourceInventory.getSize(),
                name + " Supply Drop"
        );

        copy.setContents(sourceInventory.getContents());

        this.inventory = copy;

        saveInventory(plugin.getData(), getInventoryPath(), copy);

        plugin.saveData();

        plugin.getLogger().info("Saved drop '" + name + "' inventory with "
                + countItems(copy) + " item stacks.");
    }

    public static int countItems(Inventory inventory) {
        if (inventory == null) return 0;

        int count = 0;

        for (ItemStack item : inventory.getContents()) {
            if (item != null && !item.getType().isAir()) {
                count++;
            }
        }

        return count;
    }

    public void spawn(JavaPlugin javaPlugin) {
        if (location == null || location.getWorld() == null) {
            javaPlugin.getLogger().warning("Cannot spawn drop '" + name + "': location is not set.");
            return;
        }

        if (inventory == null) {
            javaPlugin.getLogger().warning("Cannot spawn drop '" + name + "': inventory is not loaded.");
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(
                    ChatColor.RED + "DROP INBOUND",
                    ChatColor.GREEN + "X: " + location.getBlockX() + " Z: " + location.getBlockZ() ,
                    10,
                    60,
                    20
            );
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l> &6&lA supply drop is inbound to &a&l" + "X: " + location.getBlockX() + " Z: " + location.getBlockZ() ));
        }

        plugin.getDropManager().particleManager.spawnArc(location).thenAccept(impactLocation -> {
            if (impactLocation == null || impactLocation.getWorld() == null) {
                javaPlugin.getLogger().warning("Drop '" + name + "' finished particles, but impact location was invalid.");
                return;
            }

            spawnSchem(javaPlugin, impactLocation);

            Bukkit.getScheduler().runTaskLater(javaPlugin, () -> {
                Chest chest = findNearbyChest(impactLocation, 10);

                if (chest == null) {
                    javaPlugin.getLogger().warning("Schematic pasted, but no chest was found nearby.");
                    return;
                }

                Inventory chestInventory = chest.getInventory();

                copyInventory(inventory, chestInventory);

            }, 1L);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle(
                        ChatColor.RED + "DROP AVAILABLE",
                        ChatColor.GREEN + "The drop has hit the ground" ,
                        10,
                        60,
                        20
                );
            }
        });
    }



    private void spawnSchem(JavaPlugin plugin, Location pasteLocation) {
        if (pasteLocation == null || pasteLocation.getWorld() == null) {
            plugin.getLogger().warning("Cannot paste schematic: paste location is invalid.");
            return;
        }

        File schemFile = new File(plugin.getDataFolder(), "drop.schem");

        if (!schemFile.exists()) {
            plugin.getLogger().warning("Schematic not found: " + schemFile.getPath());
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schemFile);

        if (format == null) {
            plugin.getLogger().warning("Unknown schematic format: " + schemFile.getName());
            return;
        }

        try (
                ClipboardReader reader = format.getReader(new FileInputStream(schemFile));
                EditSession editSession = WorldEdit.getInstance()
                        .newEditSession(BukkitAdapter.adapt(pasteLocation.getWorld()))
        ) {
            Clipboard clipboard = reader.read();

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(
                            pasteLocation.getBlockX(),
                            pasteLocation.getBlockY(),
                            pasteLocation.getBlockZ()
                    ))
                    .ignoreAirBlocks(true)
                    .build();

            Operations.complete(operation);
            editSession.flushSession();

        } catch (Exception exception) {
            plugin.getLogger().severe("Failed to paste schematic");
            exception.printStackTrace();
        }
    }

    private static Chest findNearbyChest(Location center, int radius) {
        if (center == null || center.getWorld() == null) return null;

        World world = center.getWorld();

        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    Location check = new Location(world, x, y, z);

                    if (check.getBlock().getState() instanceof Chest chest) {
                        return chest;
                    }
                }
            }
        }

        return null;
    }

    public static void copyInventory(Inventory from, Inventory to) {
        if (from == null || to == null) return;

        to.clear();

        ItemStack[] contents = from.getContents();

        for (int i = 0; i < Math.min(contents.length, to.getSize()); i++) {
            ItemStack item = contents[i];

            if (item != null && !item.getType().isAir()) {
                to.setItem(i, item.clone());
            }
        }
    }

    public static void saveInventory(FileConfiguration config, String path, Inventory inventory) {
        if (config == null || path == null || path.isBlank()) return;

        if (inventory == null) {
            config.set(path, null);
            return;
        }

        List<ItemStack> contents = new ArrayList<>();

        for (ItemStack item : inventory.getContents()) {
            contents.add(item);
        }

        config.set(path + ".size", inventory.getSize());
        config.set(path + ".contents", contents);
    }

    public static Inventory loadInventory(FileConfiguration config, String path, String title) {
        if (config == null || path == null || path.isBlank()) {
            return Bukkit.createInventory(null, 27, title);
        }

        ConfigurationSection section = config.getConfigurationSection(path);

        if (section == null) {
            return Bukkit.createInventory(null, 27, title);
        }

        int size = section.getInt("size", 27);

        size = Math.max(9, Math.min(54, size));
        size = ((size + 8) / 9) * 9;

        Inventory inventory = Bukkit.createInventory(null, size, title);

        List<?> rawContents = section.getList("contents");

        if (rawContents == null) {
            return inventory;
        }

        for (int i = 0; i < Math.min(rawContents.size(), inventory.getSize()); i++) {
            Object object = rawContents.get(i);

            if (object instanceof ItemStack item) {
                inventory.setItem(i, item);
            }
        }

        return inventory;
    }

    public static void saveLocation(FileConfiguration config, String path, Location location) {
        if (config == null || path == null || path.isBlank()) return;

        if (location == null || location.getWorld() == null) {
            config.set(path, null);
            return;
        }

        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }

    public static Location loadLocation(FileConfiguration config, String path) {
        if (config == null || path == null || path.isBlank()) {
            return null;
        }

        ConfigurationSection section = config.getConfigurationSection(path);

        if (section == null) {
            return null;
        }

        String worldName = section.getString("world");

        if (worldName == null || worldName.isBlank()) {
            return null;
        }

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            return null;
        }

        if (!section.isDouble("x") && !section.isInt("x")) return null;
        if (!section.isDouble("y") && !section.isInt("y")) return null;
        if (!section.isDouble("z") && !section.isInt("z")) return null;

        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");

        float yaw = section.contains("yaw") ? (float) section.getDouble("yaw") : 0.0f;
        float pitch = section.contains("pitch") ? (float) section.getDouble("pitch") : 0.0f;

        return new Location(world, x, y, z, yaw, pitch);
    }
}