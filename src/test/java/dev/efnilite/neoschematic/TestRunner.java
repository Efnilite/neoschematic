package dev.efnilite.neoschematic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public abstract class TestRunner {

    private static Schematic schematic;
    private static final Path SCHEMATIC_PATH = Path.of("test-schematic.json");

    private static Schematic createSchematic() {
        var world = getWorld();

        world.getBlockAt(0, 0, 0).setType(Material.GRASS_BLOCK);
        world.getBlockAt(1, 0, 0).setType(Material.AIR);
        world.getBlockAt(0, 0, 1).setType(Material.CHEST);
        world.getBlockAt(1, 0, 1).setType(Material.STONE_STAIRS);

        var schematic = Schematic.create(
                new Location(world, 0, 0, 0),
                new Location(world, 1, 0, 1),
                Map.of("waypoint", List.of(new Location(world, 10, 5, -10))));

        schematic.save(SCHEMATIC_PATH.toFile());

        resetBlocks();

        return schematic;
    }

    public static Schematic getSchematic() {
        if (schematic == null || !Files.exists(SCHEMATIC_PATH)) {
            schematic = createSchematic();
        }

        return schematic;
    }

    public static World getWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public static void resetBlocks() {
        var world = getWorld();

        world.getBlockAt(0, 0, 0).setType(Material.STONE);
        world.getBlockAt(1, 0, 0).setType(Material.STONE);
        world.getBlockAt(0, 0, 1).setType(Material.STONE);
        world.getBlockAt(1, 0, 1).setType(Material.STONE);
    }

}
