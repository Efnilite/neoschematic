package dev.efnilite.neoschematic;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Creates a new {@link Schematic} instance. This should only be used for custom {@link FileType} implementations.
 * @see #create(Location, Location)
 *
 * @param dataVersion The data version.
 * @param minecraftVersion The Minecraft NMS version.
 * @param dimensions The dimensions of the schematic.
 * @param palette The palette of block data.
 * @param blocks The block data.
 */
public record Schematic(int dataVersion, String minecraftVersion, Vector dimensions,
                        List<BlockData> palette, List<Short> blocks) {

    /**
     * Synchronously gets and stores all blocks between the positions in a new {@link Schematic} instance.
     * For large schematics, use {@link #createAsync(Location, Location, Plugin)}.
     *
     * @param pos1 The first position.
     * @param pos2 The second position.
     * @return A new {@link Schematic} instance.
     */
    @NotNull
    public static Schematic create(@NotNull Location pos1, @NotNull Location pos2) {
        var world = pos1.getWorld() == null ? pos2.getWorld() : pos1.getWorld();

        var data = getBlocks(pos1.toVector(), pos2.toVector(), world);

        return new Schematic(1, Bukkit.getBukkitVersion().split("-")[0], data.dimensions, data.palette, data.blocks);
    }

    /**
     * Asynchronously gets and stores all blocks between the positions in a new {@link Schematic} instance.
     * This method avoids blocking the main thread during block fetching.
     *
     * @param pos1 The first position.
     * @param pos2 The second position.
     * @return A {@link CompletableFuture}. When completed, the new {@link Schematic} instance is returned.
     */
    @NotNull
    public static CompletableFuture<Schematic> createAsync(@NotNull Location pos1, @NotNull Location pos2, @NotNull Plugin plugin) {
        var future = new CompletableFuture<Schematic>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> future.complete(create(pos1, pos2)));

        return future;
    }

    /**
     * Reads a schematic from a file with the specified {@link FileType}.
     * For large schematics, use {@link #loadAsync(File, FileType, Plugin)}.
     *
     * @param file The file to read.
     * @param type The {@link FileType} instance.
     * @return A new {@link Schematic} instance, or null if reading fails or if the file doesn't exist.
     * @throws IllegalArgumentException If file does not exist or is null.
     */
    @Nullable
    public static Schematic load(@NotNull File file, @NotNull FileType type) {
        Preconditions.checkNotNull(file, "File is null");
        Preconditions.checkArgument(file.exists(), "File does not exist");

        return type.load(file);
    }

    /**
     * Loads a schematic from a file with the specified {@link FileType}.
     * For large schematics, use {@link #loadAsync(File, FileType, Plugin)}.
     *
     * @param file The file to read.
     * @param type The {@link FileType} instance.
     * @return A new {@link Schematic} instance, or null if reading fails or if the file doesn't exist.
     */
    @Nullable
    public static Schematic load(@NotNull String file, @NotNull FileType type) {
        return type.load(new File(file));
    }

    /**
     * Reads a schematic from a file with the default {@link JsonSchematic}.
     * For large schematics, use {@link #loadAsync(File, Plugin)}.
     *
     * @param file The file to read.
     * @return A new {@link Schematic} instance, or null if reading fails or if the file doesn't exist.
     * @throws IllegalArgumentException If file does not exist or is null.
     */
    @Nullable
    public static Schematic load(@NotNull File file) {
        return load(file, new JsonSchematic());
    }

    /**
     * Reads a schematic from a file with the default {@link JsonSchematic}.
     * For large schematics, use {@link #loadAsync(File, Plugin)}.
     *
     * @param file The file to read.
     * @return A new {@link Schematic} instance, or null if reading fails or if the file doesn't exist.
     * @throws IllegalArgumentException If file does not exist or is null.
     */
    @Nullable
    public static Schematic load(@NotNull String file) {
        return load(file, new JsonSchematic());
    }

    /**
     * Asynchronously reads a schematic from a file with the specified {@link FileType}.
     *
     * @param file The file to read.
     * @param type The {@link FileType} instance.
     * @param plugin The plugin instance.
     * @return A {@link CompletableFuture}. When completed, the new {@link Schematic} instance is returned,
     * or null if reading fails or if the file doesn't exist.
     * @throws IllegalArgumentException If file does not exist or is null.
     */
    public static CompletableFuture<Schematic> loadAsync(@NotNull File file, @NotNull FileType type, @NotNull Plugin plugin) {
        var future = new CompletableFuture<Schematic>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> future.complete(load(file, type)));

        return future;
    }

    /**
     * Asynchronously reads a schematic from a file with the specified {@link FileType}.
     *
     * @param file The file to read.
     * @param type The {@link FileType} instance.
     * @param plugin The plugin instance.
     * @return A {@link CompletableFuture}. When completed, the new {@link Schematic} instance is returned,
     * or null if reading fails or if the file doesn't exist.
     * @throws IllegalArgumentException If file does not exist or is null.
     */
    public static CompletableFuture<Schematic> loadAsync(@NotNull String file, @NotNull FileType type, @NotNull Plugin plugin) {
        return loadAsync(new File(file), type, plugin);
    }

    /**
     * Asynchronously reads a schematic from a file with the default {@link JsonSchematic}.
     *
     * @param file The file to read.
     * @param plugin The plugin instance.
     * @return A {@link CompletableFuture}. When completed, the new {@link Schematic} instance is returned,
     * or null if reading fails or if the file doesn't exist.
     * @throws IllegalArgumentException If file does not exist or is null.
     */
    public static CompletableFuture<Schematic> loadAsync(@NotNull File file, @NotNull Plugin plugin) {
        return loadAsync(file, new JsonSchematic(), plugin);
    }

    /**
     * Asynchronously reads a schematic from a file with the default {@link JsonSchematic}.
     *
     * @param file The file to read.
     * @param plugin The plugin instance.
     * @return A {@link CompletableFuture}. When completed, the new {@link Schematic} instance is returned,
     * or null if reading fails or if the file doesn't exist.
     * @throws IllegalArgumentException If file does not exist or is null.
     */
    public static CompletableFuture<Schematic> loadAsync(@NotNull String file, @NotNull Plugin plugin) {
        return loadAsync(new File(file), plugin);
    }

    private static BlocksData getBlocks(Vector pos1, Vector pos2, World world) {
        Preconditions.checkNotNull(pos1, "First position is null");
        Preconditions.checkNotNull(pos2, "Second position is null");
        Preconditions.checkNotNull(world, "Locations must have at least one world");

        var pos = pos1.clone().toLocation(world);
        var min = Vector.getMinimum(pos1, pos2);
        var max = Vector.getMaximum(pos1, pos2);
        var dimensions = max.clone().subtract(min).toBlockVector();

        var paletteMap = new LinkedHashMap<BlockData, Short>();
        var blocks = new LinkedList<Short>();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            pos.setX(x);

            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                pos.setY(y);

                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    pos.setZ(z);

                    var block = pos.getBlock();
                    var type = block.getBlockData();

                    var size = paletteMap.size();
                    if (!paletteMap.containsKey(type)) { // ensure O(n)
                        paletteMap.put(type, (short) size);
                    }

                    blocks.add(paletteMap.get(type));
                }
            }
        }

        var palette = new ArrayList<>(paletteMap.keySet());

        return new BlocksData(dimensions, palette, blocks);
    }

    /**
     * Saves the schematic to a file with the specified {@link FileType}.
     * For large schematics, use {@link #saveAsync(File, FileType, Plugin)}.
     *
     * @param file The file to save to.
     * @param type The {@link FileType} instance.
     * @return {@code true} if the schematic was saved successfully, false if an error was returned.
     * @throws IllegalArgumentException If file or filetype is null.
     */
    public boolean save(@NotNull File file, @NotNull FileType type) {
        Preconditions.checkNotNull(file, "File is null");
        Preconditions.checkNotNull(type, "File type is null");

        return type.save(this, file);
    }

    /**
     * Saves the schematic to a file with the specified {@link FileType}.
     * For large schematics, use {@link #saveAsync(File, FileType, Plugin)}.
     *
     * @param file The file to save to.
     * @param type The {@link FileType} instance.
     * @return {@code true} if the schematic was saved successfully, false if an error was returned.
     * @throws IllegalArgumentException If file or filetype is null.
     */
    public boolean save(@NotNull String file, @NotNull FileType type) {
        return save(new File(file), type);
    }

    /**
     * Saves the schematic to a file with the default {@link JsonSchematic}.
     * For large schematics, use {@link #saveAsync(File, Plugin)}.
     *
     * @param file The file to save to.
     * @return {@code true} if the schematic was saved successfully, false if an error was returned.
     * @throws IllegalArgumentException If file is null.
     */
    public boolean save(@NotNull File file) {
        return save(file, new JsonSchematic());
    }

    /**
     * Saves the schematic to a file with the default {@link JsonSchematic}.
     * For large schematics, use {@link #saveAsync(File, Plugin)}.
     *
     * @param file The file to save to.
     * @return {@code true} if the schematic was saved successfully, false if an error was returned.
     * @throws IllegalArgumentException If file is null.
     */
    public boolean save(@NotNull String file) {
        return save(new File(file));
    }

    /**
     * Asynchronously saves the schematic to a file with the specified {@link FileType}.
     *
     * @param file The file to save to.
     * @param type The {@link FileType} instance.
     * @param plugin The plugin instance.
     * @return A {@link CompletableFuture}.
     * When completed, {@code true} if the schematic was saved successfully, false if an error was returned.
     */
    @NotNull
    public CompletableFuture<Boolean> saveAsync(@NotNull File file, @NotNull FileType type, @NotNull Plugin plugin) {
        var future = new CompletableFuture<Boolean>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> future.complete(save(file, type)));

        return future;
    }

    /**
     * Asynchronously saves the schematic to a file with the specified {@link FileType}.
     *
     * @param file The file to save to.
     * @param type The {@link FileType} instance.
     * @param plugin The plugin instance.
     * @return A {@link CompletableFuture}.
     * When completed, {@code true} if the schematic was saved successfully, false if an error was returned.
     */
    @NotNull
    public CompletableFuture<Boolean> saveAsync(@NotNull String file, @NotNull FileType type, @NotNull Plugin plugin) {
        return saveAsync(new File(file), type, plugin);
    }

    /**
     * Asynchronously saves the schematic to a file with the default {@link JsonSchematic}.
     *
     * @param file The file to save to.
     * @param plugin The plugin instance.
     * @return A {@link CompletableFuture}.
     * When completed, {@code true} if the schematic was saved successfully, false if an error was returned.
     */
    @NotNull
    public CompletableFuture<Boolean> saveAsync(@NotNull File file, @NotNull Plugin plugin) {
        return saveAsync(file, new JsonSchematic(), plugin);
    }

    /**
     * Asynchronously saves the schematic to a file with the default {@link JsonSchematic}.
     *
     * @param file The file to save to.
     * @param plugin The plugin instance.
     * @return A {@link CompletableFuture}.
     * When completed, {@code true} if the schematic was saved successfully, false if an error was returned.
     */
    @NotNull
    public CompletableFuture<Boolean> saveAsync(@NotNull String file, @NotNull Plugin plugin) {
        return saveAsync(new File(file), plugin);
    }

    /**
     * Pastes the schematic at the specified location.
     *
     * @param location The location to paste the schematic at.
     * @return A list of all blocks which have been altered.
     */
    public List<Block> paste(@NotNull Location location, boolean skipAir) {
        Preconditions.checkNotNull(location, "Location is null");

        var pos = location.clone();
        var max = pos.clone().add(dimensions);
        var bs = new ArrayList<Block>();

        var idx = 0;
        for (int x = location.getBlockX(); x <= max.getBlockX(); x++) {
            pos.setX(x);

            for (int y = location.getBlockY(); y <= max.getBlockY(); y++) {
                pos.setY(y);

                for (int z = location.getBlockZ(); z <= max.getBlockZ(); z++) {
                    pos.setZ(z);

                    var data = palette.get(blocks.get(idx));

                    if (skipAir && data.getMaterial().isAir()) {
                        idx++;
                        continue;
                    }

                    var block = pos.getBlock();
                    block.setBlockData(data);
                    bs.add(block);

                    idx++;
                }
            }
        }

        return bs;
    }

    private record BlocksData(Vector dimensions, List<BlockData> palette, List<Short> blocks) {

    }
}