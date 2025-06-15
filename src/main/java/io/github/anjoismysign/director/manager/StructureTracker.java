package io.github.anjoismysign.director.manager;

import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.aesthetic.DirectoryAssistant;
import io.github.anjoismysign.director.TycoonManager;
import io.github.anjoismysign.director.TycoonManagerDirector;
import org.bukkit.Bukkit;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class StructureTracker extends TycoonManager {
    private final StructureManager structureManager = getPlugin().getServer().getStructureManager();
    private Map<String, Structure> structures;
    private List<Runnable> whenDoneLoading;

    public StructureTracker(TycoonManagerDirector managerDirector) {
        super(managerDirector);
        whenDoneLoading = new ArrayList<>();
        Bukkit.getScheduler().runTask(getPlugin(), this::reload);
    }

    @Override
    public void reload() {
        try {
            File dataFolder = getPlugin().getDataFolder();
            File structuresFolder = new File(dataFolder, "Structures");
            if (!structuresFolder.exists())
                structuresFolder.mkdirs();
            BlobTycoonInternalAPI.getInstance().initializeStructures(structuresFolder);
            String[] extensions = {"nbt"};
            Collection<File> files = DirectoryAssistant.of(structuresFolder).listRecursively(extensions);
            structures = new HashMap<>();
            files.forEach(this::load);
            whenDoneLoading.forEach(Runnable::run);
            whenDoneLoading = new ArrayList<>();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Load a structure from a file.
     *
     * @param file The file to load the structure from.
     */
    public void load(@NotNull File file) {
        Objects.requireNonNull(file, "'file' cannot be null");
        try {
            boolean tinyDebug = getManagerDirector().getConfigManager().tinyDebug();
            String path = file.getPath();
            String[] split = path.split(Pattern.quote(File.separator));
            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 3; i < split.length; i++) {
                String s = split[i];
                keyBuilder.append(s).append("/");
            }
            keyBuilder = keyBuilder.deleteCharAt(keyBuilder.length() - 1);
            String key = keyBuilder.toString().replace("output/Structures/", "");
            Structure structure;
            try {
                structure = structureManager.loadStructure(file);
            } catch (IOException exception) {
                exception.printStackTrace();
                return;
            }
            structures.put(key, structure);
            if (tinyDebug)
                getPlugin().getLogger().warning("Loaded structure " + keyBuilder);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Get a structure by key.
     *
     * @param key The key of the structure.
     * @return The structure, or null if it does not exist.
     */
    @Nullable
    public Structure getStructure(String key) {
        return structures.get(key);
    }

    /**
     * Get all structures.
     *
     * @return All structures.
     */
    public Collection<Structure> getTracked() {
        return Collections.unmodifiableCollection(structures.values());
    }

    /**
     * Run a task when all structures are done loading.
     *
     * @param runnable The task to run.
     */
    public void whenDoneLoading(Runnable runnable) {
        whenDoneLoading.add(runnable);
    }
}