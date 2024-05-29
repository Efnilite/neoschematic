package dev.efnilite.neoschematic;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonSchematicTest {

    private final JsonSchematic jsonSchematic = new JsonSchematic();

    @Test
    public void testCharacters() {
        var chars = IntStream.range(0, 128).mapToObj(i -> jsonSchematic.getChar((short) i)).toList();

        assertEquals(chars.get(0), "#");
        assertEquals(chars.get(1), "$");
        assertEquals(chars.get(91), "~");
        assertEquals(chars.get(92), " ");
        assertEquals(chars.get(93), "¡"); // start of non-ascii
    }
}
