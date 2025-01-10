package dev.efnilite.neoschematic;

import org.bukkit.Location;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class ZipSchematicTest extends TestRunner {

    @Override
    protected Schematic getSchematic() {
        placeBlocks();

        var saved = Schematic.create(
                new Location(getWorld(), 0, 0, 0),
                new Location(getWorld(), 1, 0, 1),
                Map.of("waypoint", List.of(new Location(getWorld(), 10, 5, -10))));

        UUID uuid = UUID.randomUUID();
        saved.save(uuid + ".zip", new ZipSchematic());

        assertTrue(Files.exists(Path.of(uuid + ".zip")));

        resetBlocks();

        return Schematic.load(uuid + ".zip", new ZipSchematic());
    }

}
