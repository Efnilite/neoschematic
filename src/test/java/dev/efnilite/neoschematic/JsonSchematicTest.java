package dev.efnilite.neoschematic;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonSchematicTest {

    private final JsonSchematic jsonSchematic = new JsonSchematic();

    @Test
    public void testCharacters() {
        var chars = IntStream.range(0, 100).mapToObj(i -> jsonSchematic.getChar((short) i)).toList();
        var str = String.join("", chars);

        assertEquals(str.charAt(0), '#');
        assertEquals(str.charAt(1), '$');
        assertEquals(str.charAt(91), '~');
        assertEquals(str.charAt(92), ' ');
        assertEquals(str.charAt(93), '¡'); // start of non-ascii
    }
}
