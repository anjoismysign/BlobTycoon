package us.mytheria.blobtycoon.director.manager;

import me.anjoismysign.aesthetic.DirectoryAssistant;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.util.BoundingBox;
import us.mytheria.bloblib.entities.IndependentBuilderManager;
import us.mytheria.blobtycoon.director.TycoonManager;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.entity.Plot;
import us.mytheria.blobtycoon.entity.PlotLoader;
import us.mytheria.blobtycoon.entity.TycoonKey;
import us.mytheria.blobtycoon.entity.plotdata.PlotData;
import us.mytheria.blobtycoon.entity.plotdata.PlotDataBuilder;
import us.mytheria.blobtycoon.exception.NoAvailablePlotsException;
import us.mytheria.blobtycoon.util.PlotDiscriminator;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PlotManager extends TycoonManager implements Listener {
    private final Map<String, PlotLoader> loaders;
    private final IndependentBuilderManager<PlotData> plotDataBuilderManager;

    private TreeMap<String, Plot> used;
    private TreeMap<String, Plot> available;

    public PlotManager(TycoonManagerDirector managerDirector) {
        super(managerDirector);
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        plotDataBuilderManager = new IndependentBuilderManager<>(managerDirector, "PlotData");
        plotDataBuilderManager.setBuilderFunction(uuid -> PlotDataBuilder
                .build(uuid, this));
        loaders = new HashMap<>();
        used = new TreeMap<>();
        available = new TreeMap<>();
        loadPlots();
    }

    @Override
    public void reload() {
        loadPlots();
    }

    public File getPlotsDirectory() {
        File dataFolder = getPlugin().getDataFolder();
        return new File(dataFolder, "plots");
    }

    private void loadPlots() {
        File plotsDirectory = getPlotsDirectory();
        if (!plotsDirectory.exists())
            plotsDirectory.mkdirs();
        String[] extensions = {"yml"};
        Collection<File> files = DirectoryAssistant.of(plotsDirectory).listRecursively(extensions);
        files.forEach(file -> {
            PlotData data = PlotData.fromFile(file);
            load(data);
        });
    }

    public void load(PlotData data) {
        String index = data.getIndex();
        Plot usedPlot = getUsed(index);
        if (usedPlot != null) {
            usedPlot.setData(data);
            return;
        }
        Plot availablePlot = getAvailable(index);
        if (availablePlot != null) {
            availablePlot.setData(data);
            return;
        }
        Plot plot = Plot.of(data);
        available.put(index, plot);
        loaders.put(index, new PlotLoader(plot, this));
    }

    public Plot getFirstAvailable() {
        Map.Entry<String, Plot> first = available.firstEntry();
        if (first == null)
            throw new NoAvailablePlotsException();
        String index = first.getKey();
        Plot plot = first.getValue();
        used.put(index, plot);
        available.remove(index);
        return plot;
    }

    public void freePlayerPlot(String index) {
        Plot plot = used.get(index);
        if (plot == null)
            throw new NullPointerException("Plot with getIndex " + index + " is not used.");
        available.put(index, plot);
        used.remove(index);
        plot.pasteStockStructure(null);
    }


    @Nullable
    private PlotLoader containsChunk(Chunk chunk) {
        return loaders.values().stream()
                .filter(loader -> loader.getWorldName().equals(chunk.getWorld().getName()))
                .filter(plotLoader -> plotLoader
                        .containsChunk(chunk)).findFirst().orElse(null);
    }

    /**
     * Let PlotLoaders know when a World is initialized,
     * so they can load their chunks and force-load them.
     *
     * @param event the event
     */
    @EventHandler
    public void onInitialization(WorldInitEvent event) {
        World world = event.getWorld();
        String worldName = world.getName();
        List<PlotLoader> list = loaders.values().stream()
                .filter(plotLoader -> plotLoader.getWorldName().equals(worldName))
                .toList();
        if (list.isEmpty())
            return;
        list.forEach(loader -> loader.loadChunks(world));
    }

    /**
     * Removes transient entities when a Chunk loads.
     *
     * @param event the event
     */
    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        List<Entity> list = event.getEntities().stream()
                .filter(TycoonKey::isTransient)
                .toList();
        if (list.isEmpty())
            return;
        Bukkit.getScheduler().runTask(getPlugin(), () -> {
            list.forEach(Entity::remove);
        });
    }

    @EventHandler
    public void onEntityLoad(EntitiesLoadEvent event) {
        Chunk chunk = event.getChunk();
        List<PlotLoader> list = loaders.values().stream()
                .filter(plotLoader -> plotLoader.containsChunk(chunk))
                .toList();
        if (list.isEmpty())
            return;
        list.forEach(plotLoader -> {
            BoundingBox boundingBox = plotLoader.getBoundingBox();
            for (Entity entity : event.getEntities()) {
                if (boundingBox.contains(entity.getLocation().toVector())) {
                    if (PlotDiscriminator.dontRemove(entity))
                        continue;
                    entity.remove();
                }
            }
            plotLoader.removeChunk(chunk);
        });
    }

    public void removeLoader(PlotLoader loader) {
        loaders.remove(loader.getIndex());
    }

    public PlotLoader getLoader(String index) {
        return loaders.get(index);
    }

    public IndependentBuilderManager<PlotData> getPlotDataBuilderManager() {
        return plotDataBuilderManager;
    }

    public Plot getAvailable(String index) {
        return available.get(index);
    }

    public Plot getUsed(String index) {
        return used.get(index);
    }

    public List<Plot> getPlots() {
        List<Plot> plots = new ArrayList<>();
        plots.addAll(available.values());
        plots.addAll(used.values());
        return plots;
    }
}
