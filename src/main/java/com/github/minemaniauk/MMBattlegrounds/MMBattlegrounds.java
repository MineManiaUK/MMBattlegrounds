package com.github.minemaniauk.MMBattlegrounds;

import com.booksaw.betterTeams.Team;
import com.github.minemaniauk.MMBattlegrounds.commands.DropTimeTable;
import com.github.minemaniauk.MMBattlegrounds.commands.ResetAllBorders;
import com.github.minemaniauk.MMBattlegrounds.commands.StartSuddenDeath;
import com.github.minemaniauk.MMBattlegrounds.commands.drops.*;
import com.github.minemaniauk.MMBattlegrounds.drops.DropManager;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class MMBattlegrounds extends JavaPlugin implements Listener {

    private static MMBattlegrounds instance;
    private ScoreboardManager scoreboardManager;
    private DropManager dropManager;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration data;
    private File dataFile;
    public long suddenDeathScheduledStartTime; // The time at which sudden death is scheduled to start unix time
    public long suddenDeathStartTime; // The time at which sudden death actually started
    public long teamDisbandTime; // Remaining time in millis in sudden death for disband
    public long suddenDeathLength; // Amount of millis which sudden death border should shrink over
    public GamePhase gamePhase;
    public List<Player> alivePlayers = new ArrayList<>(); // Used in sudden death
    private final Map<UUID, Long> combatTags = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        dataFile = new File(getDataFolder(), "data.yml");
        data = YamlConfiguration.loadConfiguration(dataFile);
        suddenDeathScheduledStartTime = config.getLong("sudden-death-start");
        teamDisbandTime = config.getLong("sudden-death-team-disband-time");
        suddenDeathLength = config.getLong("sudden-death-length");
        gamePhase = GamePhase.NORMAL;

        Bukkit.getScheduler().runTaskTimer(
                this,
                this::cleanupExpiredCombatTags,
                20L,
                20L
        );

        scoreboardManager = new ScoreboardManager();
        dropManager = new DropManager(this);

        Bukkit.getScheduler().runTaskTimer(this, this::update, 0L, 20L);
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("startsuddendeath").setExecutor(new StartSuddenDeath());
        getCommand("dropcreate").setExecutor(new DropCreate());
        DropRemove dropRemove = new DropRemove();
        getCommand("dropremove").setExecutor(dropRemove);
        getCommand("dropremove").setTabCompleter(dropRemove);
        DropSelect dropSelect = new DropSelect();
        getCommand("dropselect").setExecutor(dropSelect);
        getCommand("dropselect").setTabCompleter(dropSelect);
        getCommand("dropsetitems").setExecutor(new DropSetItems());
        getCommand("dropsetlocation").setExecutor(new DropSetLocation());
        getCommand("dropspawn").setExecutor(new DropSpawn());
        getCommand("resetallborders").setExecutor(new ResetAllBorders());
        if (config.getBoolean("show-drop-timetable")) {
            getCommand("droptimetable").setExecutor(new DropTimeTable());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        scoreboardManager.AddPlayerScoreBoard(event.getPlayer());

        if (config.getBoolean("show-drop-timetable")){
            String timeTableLink = config.getString("drop-timetable-link");
            if (timeTableLink != null && !timeTableLink.isEmpty()) {
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&l> &a&lCheck the drop time table to know when valuable supply drops are: &f" + timeTableLink));
            }
        }

        if (gamePhase != GamePhase.NORMAL) {
            if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                alivePlayers.add(event.getPlayer());
            }
            spreadNonOverworldPlayersInsideBorder();
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (isTagged(event.getPlayer())) {
            event.getPlayer().setHealth(0);
        }

        combatTags.remove(event.getPlayer().getUniqueId());
        if (gamePhase != GamePhase.NORMAL){
            if (event.getPlayer().getGameMode() == GameMode.SURVIVAL){
                alivePlayers.remove(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerKicked(PlayerKickEvent event) {
        combatTags.remove(event.getPlayer().getUniqueId());
        if (gamePhase != GamePhase.NORMAL){
            if (event.getPlayer().getGameMode() == GameMode.SURVIVAL){
                alivePlayers.remove(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (config.getBoolean("keep-inventory-management")) {
            if (isTagged(player)) {
                event.setKeepInventory(false);
            }
            else {
                event.setKeepInventory(true);
                event.getDrops().clear();
            }
        }

        if (event.getEntity().getKiller() != null){
            combatTags.remove(event.getEntity().getKiller().getUniqueId());
            event.getEntity().getKiller().sendMessage(ChatColor.GREEN + "You are no longer in combat");
        }

        if (isTagged(player)) {
            combatTags.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "You are no longer in combat");
        }



        if (gamePhase != GamePhase.NORMAL) {

            Location deathLocation = player.getLocation().clone();

            getServer().getScheduler().runTask(this, () -> {
                player.spigot().respawn();
                player.teleport(deathLocation);
                player.setGameMode(GameMode.SPECTATOR);
            });

            alivePlayers.remove(player);

            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            if (alivePlayers.size() <= 1) {
                gamePhase = GamePhase.GAME_OVER;

                if (alivePlayers.isEmpty()) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(
                                ChatColor.RED + "GAME OVER",
                                ChatColor.GRAY + "Nobody survived.",
                                10,
                                60,
                                20
                        );
                    }
                    return;
                }

                Player winningPlayer = alivePlayers.get(0);
                winningPlayer.sendTitle(
                        ChatColor.GREEN + "YOU WIN",
                        ChatColor.DARK_GREEN + "Well done GG WP",
                        10,
                        60,
                        20
                );

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p != winningPlayer) {
                        p.sendTitle(
                                ChatColor.RED + "GAME OVER",
                                ChatColor.GREEN + "The last person standing was " + ChatColor.WHITE + winningPlayer.getName() + ChatColor.GREEN + " GG WP",
                                10,
                                60,
                                20
                        );

                    }

                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }                }

                winningPlayer.getInventory().clear();

                spawnFireworksForSeconds(winningPlayer, 5);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!isTagged(event.getPlayer())) return;

        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.PLUGIN || cause == PlayerTeleportEvent.TeleportCause.COMMAND) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can not do this while in combat");
            event.setCancelled(true);
        }
    }

    private void tag(Player player) {
        boolean wasTagged = isTagged(player);

        combatTags.put(
                player.getUniqueId(),
                System.currentTimeMillis() + config.getLong("combat-tag-time")
        );

        if (!wasTagged) {
            player.sendMessage(
                    ChatColor.RED + "You are now in combat for 15 seconds. DO NOT LOG OUT"
            );
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerAttackPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = getPlayerAttacker(event.getDamager());
        if (attacker == null) return;
        if (attacker.equals(victim)) return;

        tag(attacker);
        tag(victim);
    }

    private Player getPlayerAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }

        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof Player player) {
                return player;
            }
        }

        return null;
    }

    private boolean isTagged(Player player) {
        Long expiry = combatTags.get(player.getUniqueId());

        if (expiry == null) {
            return false;
        }

        if (expiry <= System.currentTimeMillis()) {
            combatTags.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    private void cleanupExpiredCombatTags() {
        long now = System.currentTimeMillis();

        combatTags.entrySet().removeIf(entry -> {
            if (entry.getValue() > now) {
                return false;
            }

            Player player = Bukkit.getPlayer(entry.getKey());

            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.GREEN + "You are no longer in combat");
            }

            return true;
        });
    }

    public void spawnFireworksForSeconds(Player player, int seconds) {
        new BukkitRunnable() {
            int runs = 0;
            final int maxRuns = seconds * 2;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                spawnFireworksAroundPlayer(player);

                runs++;

                if (runs >= maxRuns) {
                    cancel();
                }
            }
        }.runTaskTimer(this, 0L, 10L);
    }

    public void spawnFireworksAroundPlayer(Player player) {
        World world = player.getWorld();
        Location center = player.getLocation();
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            double xOffset = (random.nextDouble() * 6.0) - 3.0;
            double zOffset = (random.nextDouble() * 6.0) - 3.0;

            Location fireworkLocation = center.clone().add(xOffset, 1.0, zOffset);

            Firework firework = world.spawn(fireworkLocation, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();

            Color mainColor = Color.fromRGB(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
            );

            Color fadeColor = Color.fromRGB(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
            );

            meta.addEffect(FireworkEffect.builder()
                    .withColor(mainColor)
                    .withFade(fadeColor)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .trail(true)
                    .flicker(true)
                    .build());

            meta.setPower(1);
            firework.setFireworkMeta(meta);
        }
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (gamePhase != GamePhase.GAME_OVER) {
            return;
        }

        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (gamePhase != GamePhase.NORMAL) {
            if (event.getCause() == PlayerPortalEvent.TeleportCause.NETHER_PORTAL || event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l> &cportals are disabled in sudden death."));
            }
        }
    }

    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent event) {
        if (event.getEntity().getType() == EntityType.END_CRYSTAL) {
            if (!config.getBoolean("end-crystals")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        if (event.getExplodedBlockState().getType() == Material.RESPAWN_ANCHOR){
            if (!config.getBoolean("respawn-anchors")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        
        if (gamePhase == GamePhase.NORMAL){
            return;
        }

        if (event.getPlayer().hasPermission("mmbattlegrounds.bypass.commanddisablement")) {
            return;
        }

        String message = event.getMessage().toLowerCase();
        List<String> disabledCommands = config.getStringList("sudden-death-disabled-commands");

        for (String command : disabledCommands) {
            if (message.equals(command) || message.startsWith(command + " ")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis command is disabled in sudden death"));
            }
        }
        
        if (gamePhase != GamePhase.SUDDEN_DEATH_NO_TEAMS) {
            return;
        }

        List<String> disabledTeamCommands = config.getStringList("no-teams-disabled-commands");

        for (String command : disabledTeamCommands) {
            if (message.equals(command) || message.startsWith(command + " ")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTeams are disabled."));
            }
        }
    }

    public void update() {
        long now = System.currentTimeMillis();

        switch (gamePhase) {
            case NORMAL:
                long normRemainingTimeMillis = Math.max(0L, suddenDeathScheduledStartTime - now);

                scoreboardManager.update(normRemainingTimeMillis, GamePhase.NORMAL);
                break;

            case SUDDEN_DEATH:

                long teamDisbandAt = suddenDeathStartTime + teamDisbandTime;
                long teamDisbandRemainingMillis = Math.max(0L, teamDisbandAt - now);

                if (teamDisbandRemainingMillis <= 0L) {
                    startSuddenDeathNoTeams();
                    return;
                }

                scoreboardManager.update(teamDisbandRemainingMillis, GamePhase.SUDDEN_DEATH);
                break;

            case SUDDEN_DEATH_NO_TEAMS:
                scoreboardManager.update(0L, GamePhase.SUDDEN_DEATH_NO_TEAMS);
                break;

            case GAME_OVER:
                scoreboardManager.update(0L, GamePhase.GAME_OVER);
                break;
        }
    }

    public void startSuddenDeath() {
        gamePhase = GamePhase.SUDDEN_DEATH;

        suddenDeathStartTime = System.currentTimeMillis();

        alivePlayers.clear();
        alivePlayers.addAll(getServer().getOnlinePlayers());

        long suddenDeathEndTime = suddenDeathStartTime + suddenDeathLength;
        long suddenDeathRemainingMillis = Math.max(0L, suddenDeathEndTime - System.currentTimeMillis());

        double finalBorderSize = config.getDouble("border-size");

        for (World world : Bukkit.getWorlds()) {
            WorldBorder border = world.getWorldBorder();

            border.setCenter(0, 0);

            border.setSize(finalBorderSize, TimeUnit.MILLISECONDS, suddenDeathRemainingMillis);

            border.setDamageBuffer(3.0);
            border.setDamageAmount(1.0);
            border.setWarningDistance(10);
            border.setWarningTime(15);
        }

        long delayTicks = suddenDeathLength / 50L;

        Bukkit.getScheduler().runTaskLater(this, () -> {
            OnWorldBorderFinished();
        }, delayTicks);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(
                    ChatColor.RED + "SUDDEN DEATH",
                    ChatColor.YELLOW + "The border is closing! Respawning is disabled",
                    10,
                    60,
                    20
            );
        }

        spreadNonOverworldPlayersInsideBorder();
    }

    public void startSuddenDeathNoTeams() {
        gamePhase = GamePhase.SUDDEN_DEATH_NO_TEAMS;

        List<String> teamNames = new ArrayList<>();

        for (Team team : Team.getTeamManager().getLoadedTeamListClone().values()) {
            teamNames.add(team.getName());
        }

        for (String teamName : teamNames) {
            String command = "teama disband " + teamName;

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(
                    ChatColor.RED + "TEAMS DISABLED",
                    ChatColor.YELLOW + "Everyone for themselves",
                    10,   // fade in ticks
                    60,   // stay ticks
                    20    // fade out ticks
            );
        }
    }

    @SuppressWarnings("deprecation")
    public void spreadNonOverworldPlayersInsideBorder() {
        World overworld = Bukkit.getWorld("world");

        if (overworld == null) {
            Bukkit.getLogger().warning("Overworld 'world' was not found.");
            return;
        }

        WorldBorder border = overworld.getWorldBorder();

        double centerX = border.getCenter().getX();
        double centerZ = border.getCenter().getZ();

        double radius = (border.getSize() / 2.0) - 10.0; // 10 block safety margin
        double minDistance = 50.0;

        for (Player p : getServer().getOnlinePlayers()) {
            if (p.getWorld().getEnvironment() != World.Environment.NORMAL) {
                if (p.getRespawnLocation() != null && p.getRespawnLocation().getWorld() == overworld) {
                    Location respawnLocation = p.getRespawnLocation();
                    p.teleport(respawnLocation);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l> &fYou have been teleported to your spawn the overworld for sudden death"));
                }
                else {
                    String command = String.format(
                            "execute as %s in minecraft:overworld run spreadplayers %.2f %.2f %.2f %.2f false @s",
                            p.getName(),
                            centerX,
                            centerZ,
                            minDistance,
                            radius
                    );

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l> &fYou have been randomly teleported to the overworld for sudden death"));
                }
            }
        }
    }

    public void OnWorldBorderFinished() {
        for (Player p : alivePlayers) {
            p.addPotionEffect(new PotionEffect(
                    PotionEffectType.GLOWING,
                    PotionEffect.INFINITE_DURATION,
                    0,
                    false,
                    false
            ));
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l> &fAll alive players now have &6glowing"));
        }
    }

    public void saveData() {
        try {
            data.save(dataFile);
        }
        catch (IOException e) {
            getLogger().severe("Failed to save data");
            getLogger().severe(e.getMessage());
            getLogger().severe(e.getStackTrace().toString());
        }
    }

    public DropManager getDropManager() {
        return this.dropManager;
    }

    public FileConfiguration getData() {
        return this.data;
    }

    public FileConfiguration getConfiguration() {
        return this.config;
    }

    public static MMBattlegrounds getInstance() {
        return instance;
    }


}
