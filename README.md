# MMBattlegrounds

MMBattlegrounds is the plugin used in the server Breakneck Battlegrounds it is NOT a plugin used on the battlegrounds smp

> **Versioning**
> 
> The first number represents the season, the second represents major changes, and the third represents minor changes or patches.
> 
### Sudden death

Sudden death occurs before a server reset and functions like a battle royale last player standing is crowned the winner of that season

### Drops
These are supply drops which can be activated by staff.
You can manage them with the drop admin commands below

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

# Damage effect balancing systems
damage-balance-enabled: false
damage-balance-effects-zero-range: 3
damage-balance-effects-multiplier: 2
damage-balance-effects-max-level: 5
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
