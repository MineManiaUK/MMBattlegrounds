# MMBattlegrounds

MMBattlegrounds is the plugin used in the server Breakneck Battlegrounds it is NOT a plugin used on the battlegrounds smp

> **Versioning**
> 
> The first number represents the season, the second represents major changes, and the third represents minor changes or patches.
> 
> Last Updated for version `3.0.0`
> 
## Sudden death

Sudden death occurs before a server reset and functions like a battle royale last player standing is crowned the winner of that season

## Drops
These are supply drops which can be activated by staff.
You can manage them with the drop admin commands below

## Damage balancing

This is a system which increases or decreases a player's outgoing damage based on their net number of kills.

The balance value is calculated from:

`netKills = (kills - deaths) + tune`

Players with more kills than deaths receive a damage reduction, while players with more deaths than kills receive a damage increase.

The system uses player data keys:
- `kills` - Total number of player kills.
- `deaths` - Total number of player deaths.
- `tune` - Acts as a manual adjustment to the player's effective net-kill value

And config keys:
- `damage-balance-enabled` - Enables or disables the damage balancing system.
- `damage-balance-effects-zero-range` - The neutral net-kill range before a balance level begins to apply.
- `damage-balance-effects-multiplier` - The number of additional net kills or deaths required to increase the balance level.
- `damage-balance-effects-max-level` - The maximum balance level that can be applied, preventing the damage modifier from scaling indefinitely.
- `damage-balance-damage-per-level` - The percentage increase or decrease in outgoing damage applied for each balance level. For example, `0.10` means 10% per level.

## Homes
The plugin includes a home and spawn function which can be used with these commands

### Commands
| Command    | Description                    | Permission                  |
|------------|--------------------------------|-----------------------------|
| /home      |                                |                             |
| /homes     |                                |                             |
| /sethome   |                                |                             |
| /delhome   |                                |                             |
| /spawn     |                                |                             |
| /homeadmin | Access and manage player homes | mmbattlegrounds.homes.admin |
| /setspawn  | Set the spawn location         | mmbattlegrounds.setspawn    |

## Admin Commands

| Command           | Description                                                                             | Permission                         |
|-------------------|-----------------------------------------------------------------------------------------|------------------------------------|
| /startsuddendeath | Activates the sudden death phase                                                        | mmbattlegrounds.suddendeath.start  |
| /dropcreate       | Create a drop                                                                           | mmbattlegrounds.drop.create        |
| /dropremove       | Delete a drop                                                                           | mmbattlegrounds.drop.remove        |
| /dropselect       | Select a drop to edit/spawn                                                             | mmbattlegrounds.drop.select        |
| /dropsetitems     | Sets the selected drops contents (Uses a single chest which the executor is looking at) | mmbattlegrounds.drop.set.items     |
| /dropsetlocation  | Sets the selected drops location to the executors current location                      | mmbattlegrounds.drop.set.location  |
| /dropspawn        | Spawns in the selected drop with its set location and contents                          | mmbattlegrounds.drop.spawn         |
| /dropcancel       | Cancel the active drop                                                                  | mmbattlegrounds.drop.cancel        |
| /resetallborders  | Sets the world border in all worlds to the default config value or the given value      | minecraft.command.worldborder      |
| /battplayerdata   | Edit a users player data                                                                | mmbattlegrounds.command.playerdata |
| /homeadmin        | Access and manage player homes                                                          | mmbattlegrounds.homes.admin        |

## Config

```yaml
# The timestamp in Unix time until sudden death is scheduled to start (visual only sudden death must be started manually with /startsuddendeath) 
sudden-death-start:

# The amount of time remaining in secs in the sudden death length before teams are disbanded
sudden-death-team-disband-time: 900

# Amount of time in secs which sudden death border should shrink over
sudden-death-length: 1800

# Is spawn command enabled  
spawn-enabled: true

# The default size of the worldborder
default-border-size: 5000

# Final world border size
border-size: 10

# Show drop timetable link on join the server
show-drop-timetable: false

# The drop timetable link
drop-timetable-link:

# Min amount of ticks which a drop takes to fall
drop-mix-ticks: 4800

# Max amount of ticks which a drop takes to fall
drop-max-ticks: 7201

# Is sudden death lava rising enabled?
rising-lava-enabled: false

# How long the lava takes to rise 1 block in ticks (40 = 2secs)
rising-lava-level-time: 40

# Damage effect balancing systems
# Enables or disables the entire damage balancing system.
damage-balance-enabled: true

# The neutral K/D difference range before any balance modifier starts.
damage-balance-effects-zero-range: 5

# How many additional net kills/deaths are required for each balance level.
damage-balance-effects-multiplier: 2

# Maximum balance level that can be applied.
# Prevents the damage bonus/penalty from increasing indefinitely.
damage-balance-effects-max-level: 5

# Percentage damage change applied per balance level.
# 0.10 = 10% per level
damage-balance-damage-per-level: 0.10

# Enable end crystal explosions
end-crystal-entity-damage: false
end-crystal-block-damage: false

# Enable respawn anchors explosions
respawn-anchors-explode: false

# Should the logic death by environment/mobs keep inventory and not if killed by another player (Turning this of means the server will use the vanilla gamerule IF TURNED ON MAKE SURE THE GAMERULE IS SET TO FALSE)
keep-inventory-management: true

# Broadcast keep inventory force-enable state changes and warnings
keep-inventory-force-alerts: true

# Warn this many ticks before keep inventory force-enable turns off
keep-inventory-force-warning-ticks: 1200

# How long a drop force-enables keep inventory for
drop-keep-inventory-force-duration-ticks: 12000

# Amount of time after being attacked by a player in which a death all be counted as a death by a player (secs) Used by the keep inventory system
combat-tag-time: 15

# Commands which are disabled in sudden death (Players which have "mmbattlegrounds.bypass.commanddisablement" are not affected)
sudden-death-disabled-commands:
  - "/sethome"
  - "/home"
  - "/delhome"
  - "/listhome"
  - "/renamehome"
  - "/relocatehome"
  - "/spawn"
  - "/tpa"
  - "/tpaccept"
  - "/tpdeny"
  - "/team sethome"
  - "/team setwarp"
  - "/team home"
  - "/team warp"
  - "/team warps"

# Commands which are disabled when teams are disabled (Players which have "mmbattlegrounds.bypass.commanddisablement" are not affected)
no-teams-disabled-commands:
  - "/team"

# Commands disabled when a drop is active (Players which have "mmbattlegrounds.bypass.commanddisablement" are not affected)
drop-disabled-commands:
  - "/command1"
  - "/command2"
```
