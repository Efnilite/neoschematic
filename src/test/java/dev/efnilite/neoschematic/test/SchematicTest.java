package dev.efnilite.neoschematic.test;

import dev.efnilite.neoschematic.Schematic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SchematicTest extends TestRunner {

    @Test
    public void testLoad() {
        var schematic = getSchematic();

        assertNotNull(schematic);
        assertEquals(Schematic.DATA_VERSION, schematic.getDataVersion());
        assertEquals(Bukkit.getBukkitVersion().split("-")[0], schematic.getMinecraftVersion());
        assertEquals(2, schematic.getDimensions().getBlockX());
        assertEquals(1, schematic.getDimensions().getBlockY());
        assertEquals(2, schematic.getDimensions().getBlockZ());
    }

    @Test
    public void testPasteSkipAir() {
        getSchematic().paste(new Location(getWorld(), 0, 0, 0), true);

        assertEquals(Material.GRASS_BLOCK, getWorld().getBlockAt(0, 0, 0).getType());
        assertEquals(Material.STONE, getWorld().getBlockAt(1, 0, 0).getType());
        assertEquals(Material.CHEST, getWorld().getBlockAt(0, 0, 1).getType());
        assertEquals(Material.STONE_STAIRS, getWorld().getBlockAt(1, 0, 1).getType());

        resetBlocks();
    }

    @Test
    public void testPaste() {
        getSchematic().paste(new Location(getWorld(), 0, 0, 0), false);

        assertEquals(Material.GRASS_BLOCK, getWorld().getBlockAt(0, 0, 0).getType());
        assertEquals(Material.AIR, getWorld().getBlockAt(1, 0, 0).getType());
        assertEquals(Material.CHEST, getWorld().getBlockAt(0, 0, 1).getType());
        assertEquals(Material.STONE_STAIRS, getWorld().getBlockAt(1, 0, 1).getType());

        resetBlocks();
    }

    @Test
    public void testWaypoint() {
        var waypoint = getSchematic().getWaypoint(new Location(getWorld(), -3, 0, 5), "waypoint");

        assertNotNull(waypoint);
        assertEquals(new Location(getWorld(), 10, 5, -10).add(-3, 0, 5), waypoint);
    }

    @Test
    public void testOffsetWaypoint() {
        var waypoint = getSchematic().getWaypoint(new Location(getWorld(), -3.5, 0.5, 5.5), "waypoint");

        assertNotNull(waypoint);
        assertEquals(new Location(getWorld(), 10, 5, -10).add(-4, 0, 5), waypoint);
    }
}
