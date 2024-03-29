package dev.efnilite.neoschematic;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface FileType {

   boolean save(@NotNull Schematic schematic, @NotNull File file);

   Schematic load(@NotNull File file);

}
