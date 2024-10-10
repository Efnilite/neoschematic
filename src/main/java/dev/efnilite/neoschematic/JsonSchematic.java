package dev.efnilite.neoschematic;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default {@link Schematic} implementation.
 * Starting from the minimum location, the blocks are concatenated into a string, until the maximum location is reached.
 * Each block is represented by a character. This character refers to a specific index in the palette.
 */
public class JsonSchematic implements FileType {

    private static final int START = '#';
    private int currentChar = START - 1;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    private final Map<Short, String> chars = new HashMap<>();
    private final Map<Character, Short> controls = new HashMap<>();

    @Expose
    private int dataVersion;
    @Expose
    private String minecraftVersion;
    @Expose
    private List<Integer> dimensions;
    @Expose
    private List<String> palette;
    @Expose
    private String blocks;
    @Expose
    private Map<String, List<String>> waypoints;

    public JsonSchematic() {

    }

    public JsonSchematic(
            int dataVersion, String minecraftVersion,
            List<Integer> dimensions, List<String> palette,
            String blocks
    ) {
        this.dataVersion = dataVersion;
        this.minecraftVersion = minecraftVersion;
        this.dimensions = dimensions;
        this.palette = palette;
        this.blocks = blocks;
        this.waypoints = new HashMap<>();
    }

    public JsonSchematic(
            int dataVersion, String minecraftVersion,
            List<Integer> dimensions, List<String> palette,
            String blocks, Map<String, List<String>> waypoints
    ) {
        this.dataVersion = dataVersion;
        this.minecraftVersion = minecraftVersion;
        this.dimensions = dimensions;
        this.palette = palette;
        this.blocks = blocks;
        this.waypoints = waypoints;
    }

    @Override
    public boolean save(@NotNull Schematic schematic, @NotNull File file) {
        Preconditions.checkNotNull(schematic, "Schematic is null");
        Preconditions.checkNotNull(file, "File is null");

        var dimensions = List.of(schematic.dimensions().getBlockX(), schematic.dimensions().getBlockY(), schematic.dimensions().getBlockZ());
        var palette = schematic.palette().stream().map(it -> it.getAsString(true)).toList();
        var serializedBlocks = String.join("", schematic.blocks().stream().map(this::getChar).toList());
        var waypoints = schematic.waypoints().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(it -> it.getX() + "," + it.getY() + "," + it.getZ() + "," + it.getYaw() + "," + it.getPitch()).toList()));

        var jsonSchematic = new JsonSchematic(Schematic.DATA_VERSION,
                schematic.minecraftVersion(), dimensions, palette,
                serializedBlocks, waypoints);
        try (var writer = new BufferedWriter(new FileWriter(file))) {
            GSON.toJson(jsonSchematic, writer);

            writer.flush();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    @Override
    @Nullable
    public Schematic load(@NotNull File file) {
        Preconditions.checkNotNull(file, "File is null");
        Preconditions.checkArgument(file.exists(), "File does not exist");

        try (var reader = new BufferedReader(new FileReader(file))) {
            var serialized = GSON.fromJson(reader, JsonSchematic.class);

            var dataVersion = serialized.dataVersion;
            var mcVersion = serialized.minecraftVersion;
            var unparsedPalette = serialized.palette;

            var palette = unparsedPalette.stream().map(Bukkit::createBlockData).toList();
            var dimensions = new Vector(serialized.dimensions.get(0),
                    serialized.dimensions.get(1),
                    serialized.dimensions.get(2));
            var blocks = serialized.blocks.chars().mapToObj(c -> fromChar((char) c)).toList();

            var waypoints = new HashMap<String, List<Location>>();
            if (dataVersion >= 2) {
                serialized.waypoints.forEach((key, locations) -> {
                    waypoints.put(key, locations.stream()
                            .map(it -> it.split(","))
                            .map(it -> new Location(null, Double.parseDouble(it[0]),
                                    Double.parseDouble(it[1]),
                                    Double.parseDouble(it[2]),
                                    Float.parseFloat(it[3]),
                                    Float.parseFloat(it[4])))
                            .toList());
                });
            }

            return new Schematic(Schematic.DATA_VERSION, mcVersion, dimensions,
                    palette, blocks, waypoints);
        } catch (IOException e) {
            return null;
        }
    }

    // avoids control chars
    public String getChar(short id) {
        return chars.computeIfAbsent(id, it -> {
            do {
                currentChar++;
            } while (Character.isISOControl(currentChar));

            return Character.toString(currentChar);
        });
    }

    public short fromChar(char c) {
        return controls.computeIfAbsent(c, it -> {
            var controlSince = 0;

            for (var i = START; i < c; i++) {
                if (Character.isISOControl(i)) {
                    controlSince++;
                }
            }

            return (short) (c - START - controlSince);
        });
    }
}