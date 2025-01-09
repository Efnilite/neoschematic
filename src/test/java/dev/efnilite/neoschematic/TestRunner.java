package dev.efnilite.neoschematic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class TestRunner {

    protected abstract Schematic getSchematic();

    private final Schematic schematic = getSchematic();

    public World getWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public void placeBlocks() {
        var world = getWorld();

        world.getBlockAt(0, 0, 0).setType(Material.GRASS_BLOCK);
        world.getBlockAt(1, 0, 0).setType(Material.AIR);
        world.getBlockAt(0, 0, 1).setType(Material.CHEST);
        world.getBlockAt(1, 0, 1).setType(Material.STONE_STAIRS);
    }

    public void resetBlocks() {
        var world = getWorld();

        world.getBlockAt(0, 0, 0).setType(Material.STONE);
        world.getBlockAt(1, 0, 0).setType(Material.STONE);
        world.getBlockAt(0, 0, 1).setType(Material.STONE);
        world.getBlockAt(1, 0, 1).setType(Material.STONE);
    }

    @Test
    public void testLoad() {
        assertNotNull(schematic);
        assertEquals(Schematic.DATA_VERSION, schematic.getDataVersion());
        assertEquals(Bukkit.getBukkitVersion().split("-")[0], schematic.getMinecraftVersion());
        assertEquals(2, schematic.getDimensions().getBlockX());
        assertEquals(1, schematic.getDimensions().getBlockY());
        assertEquals(2, schematic.getDimensions().getBlockZ());
    }

    @Test
    public void testPasteSkipAir() {
        schematic.paste(new Location(getWorld(), 0, 0, 0), true);

        assertEquals(Material.GRASS_BLOCK, getWorld().getBlockAt(0, 0, 0).getType());
        assertEquals(Material.STONE, getWorld().getBlockAt(1, 0, 0).getType());
        assertEquals(Material.CHEST, getWorld().getBlockAt(0, 0, 1).getType());
        assertEquals(Material.STONE_STAIRS, getWorld().getBlockAt(1, 0, 1).getType());

        resetBlocks();
    }

    @Test
    public void testPaste() {
        schematic.paste(new Location(getWorld(), 0, 0, 0), false);

        assertEquals(Material.GRASS_BLOCK, getWorld().getBlockAt(0, 0, 0).getType());
        assertEquals(Material.AIR, getWorld().getBlockAt(1, 0, 0).getType());
        assertEquals(Material.CHEST, getWorld().getBlockAt(0, 0, 1).getType());
        assertEquals(Material.STONE_STAIRS, getWorld().getBlockAt(1, 0, 1).getType());

        resetBlocks();
    }

    @Test
    public void testWaypoint() {
        var waypoint = schematic.getWaypoint(new Location(getWorld(), -3, 0, 5), "waypoint");

        assertNotNull(waypoint);
        assertEquals(new Location(getWorld(), 10, 5, -10).add(-3, 0, 5), waypoint);
    }

    @Test
    public void testOffsetWaypoint() {
        var waypoint = schematic.getWaypoint(new Location(getWorld(), -3.5, 0.5, 5.5), "waypoint");

        assertNotNull(waypoint);
        assertEquals(new Location(getWorld(), 10, 5, -10).add(-4, 0, 5), waypoint);
    }

}
