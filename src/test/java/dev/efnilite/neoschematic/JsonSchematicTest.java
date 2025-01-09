package dev.efnilite.neoschematic;

import org.bukkit.Location;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonSchematicTest extends TestRunner {

    @Override
    protected Schematic getSchematic() {
        placeBlocks();

        var saved = Schematic.create(
                new Location(getWorld(), 0, 0, 0),
                new Location(getWorld(), 1, 0, 1),
                Map.of("waypoint", List.of(new Location(getWorld(), 10, 5, -10))));

        UUID uuid = UUID.randomUUID();
        saved.save(uuid + ".json");

        assertTrue(Files.exists(Path.of(uuid + ".json")));

        resetBlocks();

        return Schematic.load(uuid + ".json");
    }

    private static final JsonSchematic JSON_SCHEMATIC = new JsonSchematic();

    @Test
    public void testToChar() {
        var chars = IntStream.range(0, 100).mapToObj(i -> JSON_SCHEMATIC.getChar((short) i)).toList();
        var str = String.join("", chars);

        assertEquals('#', str.charAt(0));
        assertEquals('$', str.charAt(1));
        assertEquals('~', str.charAt(91));
        assertEquals(' ', str.charAt(92));
        assertEquals('¡', str.charAt(93)); // start of non-ascii
    }

    @Test
    public void testFromChar() {
        var idxs = "#$~ ¡".chars().map(c -> JSON_SCHEMATIC.fromChar((char) c)).boxed().toList();

        assertEquals(0, (int) idxs.get(0));
        assertEquals(1, (int) idxs.get(1));
        assertEquals(91, (int) idxs.get(2));
        assertEquals(92, (int) idxs.get(3));
        assertEquals(93, (int) idxs.get(4));
    }

}
