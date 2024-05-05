package dev.efnilite.neoschematic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface FileType {

   /**
    * Save the schematic to a file.
    * @param schematic The schematic to save
    * @param file The file to save to
    * @return True if the schematic was saved successfully, false if not
    */
   boolean save(@NotNull Schematic schematic, @NotNull File file);

   /**
    * Load a schematic from a file.
    * @param file The file.
    * @return The schematic, or null if the schematic could not be loaded.
    */
   @Nullable Schematic load(@NotNull File file);

}
