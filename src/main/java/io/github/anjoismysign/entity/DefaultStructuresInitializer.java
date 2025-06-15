package io.github.anjoismysign.entity;

import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.utilities.Structrador;
import io.github.anjoismysign.BlobTycoon;
import io.github.anjoismysign.director.TycoonManagerDirector;
import io.github.anjoismysign.util.TycoonStructrador;

import java.io.File;
import java.util.Objects;

public class DefaultStructuresInitializer {
    private static boolean hasInitialized = false;
    private static Structrador CLEAN;
    private static Structrador STOCK;

    public static void load(TycoonManagerDirector director, File structuresDirectory) {
        try {
            boolean tinyDebug = director.getConfigManager().tinyDebug();
            director.detachAsset("clean.nbt", tinyDebug, structuresDirectory);
            director.detachAsset("stock.nbt", tinyDebug, structuresDirectory);
            BlobTycoon plugin = director.getPlugin();
            File cleanFile = new File(structuresDirectory, "clean.nbt");
            File stockFile = new File(structuresDirectory, "stock.nbt");
            StructureManager structureManager = plugin.getServer().getStructureManager();
            Structure cleanStructure = structureManager.loadStructure(cleanFile);
            CLEAN = new TycoonStructrador(cleanStructure, plugin);
            Structure stockStructure = structureManager.loadStructure(stockFile);
            STOCK = new TycoonStructrador(stockStructure, plugin);
            hasInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    protected static Structrador getClean() {
        return Objects.requireNonNull(CLEAN, "structure is null");
    }

    @NotNull
    protected static Structrador getStock() {
        return Objects.requireNonNull(STOCK, "structure is null");
    }

    public static boolean hasInitialized() {
        return hasInitialized;
    }
}
