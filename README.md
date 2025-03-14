# Back me Up

This is an addon mod for Better than Wolves. Join BTW discord to check out other mods as well: [BTW Forum](https://wiki.btwce.com/index.php?title=Main_Page)

## Features:
- Backups and loads your world without leaving the game
- All configurable:
  - Run the game once so the .properties file gets created (in config folder)
  - You can backup your world hourly or daily (in game time, 24h)
  - Configure where all backups go (leave null, for game_folder/backups)
  - Enable cleanup policy, and configure it to your liking based on your free disk space
- A new command is added: /backmeup list AND /backmeup load <index>
  - list command lists backups that are available to load
  - load command deletes the old world, and moves the one you wanted to the saves folder. And lastly, loads the world. You just need to wait.

### Changelog (v2.0.0):

- Quick save & quick load shortcuts added (F6 for save, F7 for load) (Customizable in game controls menu)
- Quick load is basically the same thing as running the command: "/backmeup load 0"
- Day start offset is added to the config. This is useful if you're using some mods which alter the initial day start time like Nightmare mode. (18000 for midnight start)

### Changelog (v2.1.0)
- This version ensures that whenever the quick save key is pressed, all current state (world, player data) gets saved in that exact moment. Previously, it was just backing up the autosaved world folder which is about every 45 seconds.
