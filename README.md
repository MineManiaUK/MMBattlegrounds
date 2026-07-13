# MMBattlegrounds

MMBattlegrounds is the plugin used in the server internally known as batt2 it is NOT a plugin used on the battlegrounds smp

### Sudden death

Sudden death occurs before a server reset and functions like a battle royale last player standing is crowned the winner of that season

### Drops
These are supply drops which can be activated by staff.

## Commands

| Command           | Description                                                                             | Permission                        |
|-------------------|-----------------------------------------------------------------------------------------|-----------------------------------|
| /startsuddendeath | Activates the sudden death phase                                                        | mmbattlegrounds.suddendeath.start |
| /dropcreate       | Create a drop                                                                           | mmbattlegrounds.drop.create       |
| /dropremove       | Delete a drop                                                                           | mmbattlegrounds.drop.remove       |
| /dropselect       | Select a drop to edit/spawn                                                             | mmbattlegrounds.drop.select       |
| /dropsetitems     | Sets the selected drops contents (Uses a single chest which the executor is looking at) | mmbattlegrounds.drop.set.items    |
| /dropsetlocation  | Sets the selected drops location to the executors current location                      | mmbattlegrounds.drop.set.location |
| /dropspawn        | Spawns in the selected drop with its set location and contents                          | mmbattlegrounds.drop.spawn        |
| /resetallborders  | Sets the world border in all worlds to the default config value or the given value      | minecraft.command.worldborder     |


## Config

```yaml
# The timestamp in Unix millis until sudden death is scheduled to start (visual only sudden death must be started manually with /startsuddendeath) 
sudden-death-start:

# The amount of time remaining in millis in the sudden death length before teams are disbanded
sudden-death-team-disband-time: 900000

# Amount of time in millis which sudden death border should shrink over
sudden-death-length: 1800000

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

# Enable end crystal explosions
end-crystals: false

# Enable respawn anchors explosions
respawn-anchors: false

# Should the logic death by environment/mobs keep inventory and not if killed by another player (Turning this of means the server will use the vanilla gamerule IF TURNED ON MAKE SURE THE GAMERULE IS SET TO FALSE)
keep-inventory-management: true

# Amount of time after being attacked by a player in which a death all be counted as a death by a player (Millis) Used by the keep inventory system
combat-tag-time: 15000

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
```
