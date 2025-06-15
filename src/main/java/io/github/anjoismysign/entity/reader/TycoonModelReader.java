package io.github.anjoismysign.entity.reader;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.entity.StructureData;
import io.github.anjoismysign.entity.structure.ObjectModel;
import io.github.anjoismysign.entity.structure.StorageModel;
import io.github.anjoismysign.entity.structure.StructureModel;

import java.util.Objects;
import java.util.function.Consumer;

public class TycoonModelReader {
    public static ObjectModel OBJECT_MODEL(@NotNull ConfigurationSection section,
                                           @NotNull JavaPlugin plugin,
                                           @NotNull String type,
                                           @NotNull String key,
                                           @NotNull Consumer<Player> whenRemoved,
                                           @NotNull Consumer<Player> whenPlaced) {
        if (!section.isString("Structure"))
            throw new ConfigurationFieldException("'Structure' is not valid or set");
        String structurePath = section.getString("Structure");
        String placeSoundKey = section.getString("Place-Sound", null);
        String removeSoundKey = section.getString("Remove-Sound", null);
        String translatableItemKey = section.getString("TranslatableItem", key);
        TranslatableItem translatableItem = TranslatableItem.by(translatableItemKey);
        Objects.requireNonNull(translatableItem,
                "'TranslatableItem' is not valid or set");
        Structure structure = BlobTycoonInternalAPI.getInstance().getStructure(structurePath);
        if (structure == null)
            throw new ConfigurationFieldException("'Structure' doesn't point to a valid structure");
        return new ObjectModel() {
            @Override
            public @NotNull String getType() {
                return type;
            }

            @Override
            public @NotNull String getKey() {
                return key;
            }

            @Override
            public @NotNull TranslatableItem getTranslatableItem() {
                return translatableItem;
            }

            @Override
            public @NotNull JavaPlugin getPlugin() {
                return plugin;
            }

            @Override
            public @NotNull Structure getStructure() {
                return structure;
            }

            @Override
            public @NotNull String getStructurePath() {
                return structurePath;
            }

            @Override
            public @Nullable String getPlaceSoundKey() {
                return placeSoundKey;
            }

            @Override
            public @Nullable String getRemoveSoundKey() {
                return removeSoundKey;
            }

            @Override
            public @NotNull Consumer<Player> getWhenPlaced() {
                return whenPlaced;
            }

            @Override
            public @NotNull Consumer<Player> getWhenRemoved() {
                return whenRemoved;
            }
        };
    }

    public static StorageModel STORAGE_MODEL(@NotNull ConfigurationSection section,
                                             @NotNull JavaPlugin plugin,
                                             @NotNull String type,
                                             @NotNull String key,
                                             @NotNull Consumer<Player> whenRemoved,
                                             @NotNull Consumer<Player> whenPlaced,
                                             int maxStorage) {
        if (!section.isString("Structure"))
            throw new ConfigurationFieldException("'Structure' is not valid or set");
        if (!section.isConfigurationSection("Structure-Data"))
            throw new ConfigurationFieldException("'Structure-Data' is not valid or set");
        String structurePath = section.getString("Structure");
        if (structurePath.equals("null"))
            throw new ConfigurationFieldException("'Structure' is defaulted to 'null', you need to manually set it to a valid structure path");

        String placeSoundKey = section.getString("Place-Sound", null);
        String removeSoundKey = section.getString("Remove-Sound", null);
        String translatableItemKey = section.getString("TranslatableItem", key);
        TranslatableItem translatableItem = TranslatableItem.by(translatableItemKey);
        Objects.requireNonNull(translatableItem,
                "'TranslatableItem' is not valid or set");
        Structure structure = BlobTycoonInternalAPI.getInstance().getStructure(structurePath);
        if (structure == null)
            throw new ConfigurationFieldException("'Structure' doesn't point to a valid structure");
        StructureData structureData = StructureData.READ(section, "Structure-Data");
        return new StorageModel() {

            @Override
            public int getMaxStorage() {
                return maxStorage;
            }

            @Override
            public @NotNull TranslatableItem getTranslatableItem() {
                return translatableItem;
            }

            @Override
            public @NotNull JavaPlugin getPlugin() {
                return plugin;
            }

            @Override
            public @NotNull Structure getStructure() {
                return structure;
            }

            @Override
            public @NotNull String getStructurePath() {
                return structurePath;
            }

            @Override
            public @Nullable String getPlaceSoundKey() {
                return placeSoundKey;
            }

            @Override
            public @Nullable String getRemoveSoundKey() {
                return removeSoundKey;
            }

            @Override
            public @NotNull Consumer<Player> getWhenPlaced() {
                return whenPlaced;
            }

            @Override
            public @NotNull Consumer<Player> getWhenRemoved() {
                return whenRemoved;
            }

            @Override
            public @NotNull StructureData getStructureData() {
                return structureData;
            }

            @Override
            public String getType() {
                return type;
            }

            @Override
            public @NotNull String getKey() {
                return key;
            }
        };
    }

    public static StructureModel STRUCTURE_MODEL(@NotNull ConfigurationSection section,
                                                 @NotNull JavaPlugin plugin,
                                                 @NotNull String type,
                                                 @NotNull String key,
                                                 @NotNull Consumer<Player> whenRemoved,
                                                 @NotNull Consumer<Player> whenPlaced) {
        if (!section.isString("Structure"))
            throw new ConfigurationFieldException("'Structure' is not valid or set");
        if (!section.isConfigurationSection("Structure-Data"))
            throw new ConfigurationFieldException("'Structure-Data' is not valid or set");
        String structurePath = section.getString("Structure");
        String placeSoundKey = section.getString("Place-Sound", null);
        String removeSoundKey = section.getString("Remove-Sound", null);
        String translatableItemKey = section.getString("TranslatableItem", key);
        TranslatableItem translatableItem = TranslatableItem.by(translatableItemKey);
        Objects.requireNonNull(translatableItem,
                "'TranslatableItem' is not valid or set");
        Structure structure = BlobTycoonInternalAPI.getInstance().getStructure(structurePath);
        if (structure == null)
            throw new ConfigurationFieldException("'Structure' doesn't point to a valid structure");
        StructureData structureData = StructureData.READ(section, "Structure-Data");
        return new StructureModel() {

            @Override
            public @NotNull TranslatableItem getTranslatableItem() {
                return translatableItem;
            }

            @Override
            public @NotNull JavaPlugin getPlugin() {
                return plugin;
            }

            @Override
            public @NotNull Structure getStructure() {
                return structure;
            }

            @Override
            public @NotNull String getStructurePath() {
                return structurePath;
            }

            @Override
            public @Nullable String getPlaceSoundKey() {
                return placeSoundKey;
            }

            @Override
            public @Nullable String getRemoveSoundKey() {
                return removeSoundKey;
            }

            @Override
            public @NotNull Consumer<Player> getWhenPlaced() {
                return whenPlaced;
            }

            @Override
            public @NotNull Consumer<Player> getWhenRemoved() {
                return whenRemoved;
            }

            @Override
            public @NotNull StructureData getStructureData() {
                return structureData;
            }

            @Override
            public String getType() {
                return type;
            }

            @Override
            public @NotNull String getKey() {
                return key;
            }
        };
    }
}
