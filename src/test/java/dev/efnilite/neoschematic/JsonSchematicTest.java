package dev.efnilite.neoschematic;

import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class JsonSchematicTest extends TestRunner {

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
