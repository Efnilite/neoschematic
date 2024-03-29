<div style="text-align: center !important">

### NeoSchematic

[Source](https://github.com/Efnilite/neoschematic) | by [Efnilite](https://github.com/Efnilite)

</div>

## Features
- Easily editable schematic files for small changes
- Scales linearly with the amount of blocks: 1 million blocks ~= 1 MB

## Usage

### Create and save a schematic

```java
Schematic schematic = Schematic.create(pos1, pos2);
boolean success = schematic.save("plugins/schematic.json");
```

### Create and save a schematic asynchronously (Recommended)

```java
Schematic.createAsync(pos1, pos2, plugin).thenAccept(schematic -> 
    schematic.saveAsync("plugins/schematic.json", plugin)
);
```

### Paste existing schematic

```java
Schematic schematic = Schematic.load("plugins/schematic.json");
List<Block> blocks = schematic.paste(location, true);
```

### Load existing schematic asynchronously and paste (Recommended)

```java
Schematic.loadAsync("plugins/schematic.json", plugin).thenAccept(schematic -> {
    Bukkit.getScheduler().runTask(plugin, () -> schematic.paste(location, true));
});
```

### Example plugin

```java
package dev.efnilite.neoschematic;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ExamplePlugin extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        getCommand("example").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                             @NotNull String label, String[] args) {
        if (args.length == 0 || !(sender instanceof Player player)) return true;

        switch (args[0]) {
            case "save" -> {
                var coords1 = Arrays.stream(args[1].split(",")).mapToInt(Integer::parseInt).toArray();
                var coords2 = Arrays.stream(args[2].split(",")).mapToInt(Integer::parseInt).toArray();

                var pos1 = new Location(player.getWorld(), coords1[0], coords1[1], coords1[2]);
                var pos2 = new Location(player.getWorld(), coords2[0], coords2[1], coords2[2]);

                sender.sendMessage("Getting blocks...");

                Schematic.createAsync(pos1, pos2, this).thenAccept(schematic -> {
                    sender.sendMessage("Saving file...");

                    schematic.saveAsync("plugins/schematic.json", this).thenRun(() -> {
                        sender.sendMessage("Schematic saved as schematic.json");
                    });
                });
            }

            case "paste" -> {
                sender.sendMessage("Loading file...");

                Schematic.loadAsync("plugins/schematic.json", this).thenAccept(schematic -> {
                    if (schematic == null) {
                        player.sendMessage("Schematic not found.");
                        return;
                    }

                    sender.sendMessage("Pasting blocks...");

                    getServer().getScheduler().runTask(this, () -> {
                        List<Block> blocks = schematic.paste(player.getLocation(), true);
                        player.sendMessage("Pasted %s blocks".formatted(blocks.size()));
                    });
                });
            }
        }

        return true;
    }
}
```