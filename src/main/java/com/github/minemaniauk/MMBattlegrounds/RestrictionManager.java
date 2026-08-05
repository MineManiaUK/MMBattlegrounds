package com.github.minemaniauk.MMBattlegrounds;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class RestrictionManager implements Listener {

    private static final long DEFAULT_DROP_KEEP_INVENTORY_DURATION_TICKS = 20L * 60L * 10L;
    private static final long DEFAULT_KEEP_INVENTORY_WARNING_TICKS = 20L * 60L;

    private final MMBattlegrounds plugin;
    private boolean keepInventoryForceEnable;
    private BukkitTask keepInventoryForceResetTask;
    private BukkitTask keepInventoryForceWarningTask;

    private boolean elytraForceDisable;
    private boolean riptideForceDisable;

    public RestrictionManager(MMBattlegrounds plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGlideEvent(EntityToggleGlideEvent event) {
        if (!elytraForceDisable) return;

        if (event.isGliding()) {
            event.setCancelled(true);

            if (event.getEntity() instanceof Player player) {
                player.sendMessage(ChatColor.translateAlternateColorCodes(
                        '&',
                        "&c&l> &cGliding is currently disabled"
                ));
            }
        }
    }

    @EventHandler
    public void onRiptideUse(PlayerInteractEvent event) {
        if (!riptideForceDisable) return;

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.TRIDENT) {
            return;
        }

        if (!item.containsEnchantment(Enchantment.RIPTIDE)) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes(
                '&',
                "&c&l> &cRiptide is currently disabled"
        ));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isKeepInventoryManagementEnabled()) {
            return;
        }

        boolean keepInventory = shouldKeepInventory(event.getEntity());

        event.setKeepInventory(keepInventory);

        if (keepInventory) {
            event.getDrops().clear();
        }
    }

    public boolean isKeepInventoryManagementEnabled() {
        return plugin.getConfiguration().getBoolean("keep-inventory-management");
    }

    public boolean isRiptideForceDisabled() {
        return riptideForceDisable;
    }

    public boolean isElytraForceDisabled() {
        return elytraForceDisable;
    }

    public void enableElytraForceDisable() {
        elytraForceDisable = true;
    }

    public void disableElytraForceDisable() {
        elytraForceDisable = false;
    }

    public void enableRiptideForceDisable() {
        riptideForceDisable = true;
    }

    public void disableRiptideForceDisable() {
        riptideForceDisable = false;
    }

    public void enableMovementForceDisable() {
        enableElytraForceDisable();
        enableRiptideForceDisable();
    }

    public void disableMovementForceDisable() {
        disableElytraForceDisable();
        disableRiptideForceDisable();
    }

    public boolean enableAll() {
        boolean keepInventoryChanged = enableKeepInventoryForce();
        boolean movementChanged = !isElytraForceDisabled() || !isRiptideForceDisabled();

        enableMovementForceDisable();
        return keepInventoryChanged || movementChanged;
    }

    public boolean enableAllSilently() {
        boolean keepInventoryChanged = enableKeepInventoryForceSilently();
        boolean movementChanged = !isElytraForceDisabled() || !isRiptideForceDisabled();

        enableMovementForceDisable();
        return keepInventoryChanged || movementChanged;
    }

    public boolean isKeepInventoryForceEnabled() {
        return keepInventoryForceEnable;
    }

    public long getDropKeepInventoryDurationTicks() {
        return Math.max(
                0L,
                plugin.getConfiguration().getLong(
                        "drop-keep-inventory-force-duration-ticks",
                        DEFAULT_DROP_KEEP_INVENTORY_DURATION_TICKS
                )
        );
    }

    public boolean enableKeepInventoryForce() {
        if (!isKeepInventoryManagementEnabled()) {
            cancelScheduledKeepInventoryTasks();
            keepInventoryForceEnable = false;
            return false;
        }

        cancelScheduledKeepInventoryTasks();
        boolean stateChanged = setKeepInventoryForceEnabled(true, true);
        broadcastKeepInventoryManualEnable();
        return stateChanged;
    }

    public boolean enableKeepInventoryForceSilently() {
        if (!isKeepInventoryManagementEnabled()) {
            cancelScheduledKeepInventoryTasks();
            keepInventoryForceEnable = false;
            return false;
        }

        cancelScheduledKeepInventoryTasks();
        return setKeepInventoryForceEnabled(true, false);
    }

    public boolean enableAllForDuration(long durationTicks) {
        if (durationTicks <= 0L) {
            disableAll();
            return false;
        }

        boolean movementChanged = !isElytraForceDisabled() || !isRiptideForceDisabled();
        enableMovementForceDisable();

        if (!isKeepInventoryManagementEnabled()) {
            cancelScheduledKeepInventoryTasks();
            keepInventoryForceEnable = false;
            keepInventoryForceResetTask = plugin.getServer().getScheduler().runTaskLater(
                    plugin,
                    this::disableAll,
                    durationTicks
            );
            return movementChanged;
        }

        boolean keepInventoryChanged = enableKeepInventoryForDuration(durationTicks, this::disableAll);
        return keepInventoryChanged || movementChanged;
    }

    public boolean disableAll() {
        boolean keepInventoryChanged = disableKeepInventoryForceEnable();
        boolean movementChanged = isElytraForceDisabled() || isRiptideForceDisabled();

        disableMovementForceDisable();
        return keepInventoryChanged || movementChanged;
    }

    public boolean disableAllSilently() {
        boolean keepInventoryChanged = disableKeepInventoryForceEnableSilently();
        boolean movementChanged = isElytraForceDisabled() || isRiptideForceDisabled();

        disableMovementForceDisable();
        return keepInventoryChanged || movementChanged;
    }

    public boolean enableKeepInventoryForDuration(long durationTicks) {
        return enableKeepInventoryForDuration(durationTicks, this::disableKeepInventoryForceEnable);
    }

    public boolean disableKeepInventoryForceEnable() {
        return disableKeepInventoryForceEnable(true);
    }

    public boolean disableKeepInventoryForceEnableSilently() {
        return disableKeepInventoryForceEnable(false);
    }

    private boolean disableKeepInventoryForceEnable(boolean announce) {
        cancelScheduledKeepInventoryTasks();
        return setKeepInventoryForceEnabled(false, announce);
    }

    private boolean enableKeepInventoryForDuration(long durationTicks, Runnable disableAction) {
        if (!isKeepInventoryManagementEnabled()) {
            cancelScheduledKeepInventoryTasks();
            keepInventoryForceEnable = false;
            return false;
        }

        if (durationTicks <= 0L) {
            disableAction.run();
            return false;
        }

        boolean stateChanged = setKeepInventoryForceEnabled(true, true);

        cancelScheduledKeepInventoryTasks();
        if (stateChanged) {
            broadcastKeepInventoryDuration(durationTicks);
        }
        else {
            broadcastKeepInventoryRefresh(durationTicks);
        }
        scheduleUpcomingDisableAlert(durationTicks);

        keepInventoryForceResetTask = plugin.getServer().getScheduler().runTaskLater(
                plugin,
                disableAction,
                durationTicks
        );

        return stateChanged;
    }

    private void cancelScheduledKeepInventoryTasks() {
        if (keepInventoryForceResetTask != null) {
            keepInventoryForceResetTask.cancel();
            keepInventoryForceResetTask = null;
        }

        if (keepInventoryForceWarningTask != null) {
            keepInventoryForceWarningTask.cancel();
            keepInventoryForceWarningTask = null;
        }
    }

    private boolean shouldKeepInventory(Player player) {
        if (keepInventoryForceEnable) {
            return true;
        }

        return !plugin.isTagged(player);
    }

    private boolean setKeepInventoryForceEnabled(boolean enabled, boolean announce) {
        boolean stateChanged = keepInventoryForceEnable != enabled;
        keepInventoryForceEnable = enabled;

        if (announce && stateChanged) {
            broadcastKeepInventoryState(enabled);
        }

        return stateChanged;
    }

    private void scheduleUpcomingDisableAlert(long durationTicks) {
        long warningTicks = Math.max(
                0L,
                plugin.getConfiguration().getLong(
                        "keep-inventory-force-warning-ticks"
                )
        );

        if (!areKeepInventoryAlertsEnabled() || warningTicks <= 0L || warningTicks >= durationTicks) {
            return;
        }

        keepInventoryForceWarningTask = plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> broadcastUpcomingKeepInventoryChange(warningTicks),
                durationTicks - warningTicks
        );
    }

    private boolean areKeepInventoryAlertsEnabled() {
        return plugin.getConfiguration().getBoolean("keep-inventory-force-alerts", true);
    }

    private void broadcastKeepInventoryState(boolean enabled) {
        if (!areKeepInventoryAlertsEnabled()) {
            return;
        }

        String title = enabled ? ChatColor.GREEN + "KEEP INVENTORY ON" : ChatColor.RED + "KEEP INVENTORY OFF";
        String subtitle = enabled
                ? ChatColor.YELLOW + "All deaths keep items"
                : ChatColor.YELLOW + "Normal death rules restored";
        String message = enabled
                ? "&7&l> &aKeep inventory is now force-enabled for everyone"
                : "&7&l> &cKeep inventory force-enable has ended. Normal rules now apply";

        broadcast(title, subtitle, message);
    }

    private void broadcastKeepInventoryManualEnable() {
        if (!areKeepInventoryAlertsEnabled()) {
            return;
        }

        broadcastMessage(
                "&7&l> &aKeep inventory will stay forced on until it is manually disabled"
        );
    }

    private void broadcastKeepInventoryDuration(long durationTicks) {
        if (!areKeepInventoryAlertsEnabled()) {
            return;
        }

        broadcastMessage("&7&l> &aKeep inventory will stay forced on for &f" + formatDuration(durationTicks));
    }

    private void broadcastKeepInventoryRefresh(long durationTicks) {
        if (!areKeepInventoryAlertsEnabled()) {
            return;
        }

        broadcastMessage(
                "&7&l> &aKeep inventory force-enable was refreshed. New remaining time: &f"
                        + formatDuration(durationTicks)
        );
    }

    private void broadcastUpcomingKeepInventoryChange(long remainingTicks) {
        if (!keepInventoryForceEnable || !areKeepInventoryAlertsEnabled()) {
            return;
        }

        broadcast(
                ChatColor.GOLD + "KEEP INVENTORY ENDING",
                ChatColor.YELLOW + "Turns off in " + formatDuration(remainingTicks),
                "&7&l> &eKeep inventory force-enable will end in &f" + formatDuration(remainingTicks)
                        + "&e. Normal death rules will return after that"
        );
    }

    private void broadcast(String title, String subtitle, String message) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendTitle(title, subtitle, 10, 50, 20);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    private void broadcastMessage(String message) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    private String formatDuration(long ticks) {
        long totalSeconds = Math.max(1L, ticks / 20L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;

        if (minutes > 0L && seconds > 0L) {
            return minutes + "m " + seconds + "s";
        }

        if (minutes > 0L) {
            return minutes + "m";
        }

        return seconds + "s";
    }

    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent event) {
        if (event.getEntity().getType() == EntityType.END_CRYSTAL) {
            if (!MMBattlegrounds.getInstance().getConfig().getBoolean("end-crystals")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        if (event.getExplodedBlockState().getType() == Material.RESPAWN_ANCHOR){
            if (!MMBattlegrounds.getInstance().getConfig().getBoolean("respawn-anchors")) {
                event.setCancelled(true);
            }
        }
    }
}
