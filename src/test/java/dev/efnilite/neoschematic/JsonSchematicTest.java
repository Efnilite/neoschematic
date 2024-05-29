package dev.efnilite.neoschematic;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonSchematicTest {

    private final JsonSchematic jsonSchematic = new JsonSchematic();

    @Test
    public void testToChar() {
        var chars = IntStream.range(0, 100).mapToObj(i -> jsonSchematic.getChar((short) i)).toList();
        var str = String.join("", chars);

        assertEquals(str.charAt(0), '#');
        assertEquals(str.charAt(1), '$');
        assertEquals(str.charAt(91), '~');
        assertEquals(str.charAt(92), ' ');
        assertEquals(str.charAt(93), '¡'); // start of non-ascii
    }

    @Test
    public void testFromChar() {
        var idxs = "#$~ ¡".chars().map(c -> jsonSchematic.fromChar((char) c)).boxed().toList();

        assertEquals(idxs.get(0), 0);
        assertEquals(idxs.get(1), 1);
        assertEquals(idxs.get(2), 91);
        assertEquals(idxs.get(3), 92);
        assertEquals(idxs.get(4), 93);
    }
}
