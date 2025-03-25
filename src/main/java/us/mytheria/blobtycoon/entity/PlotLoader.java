package us.mytheria.blobtycoon.entity;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobtycoon.director.manager.PlotManager;

import java.util.HashSet;
import java.util.Set;

public class PlotLoader {
    private final BoundingBox boundingBox;
    private final Plot plot;
    private final String worldName;
    private final PlotManager plotManager;
    private final Set<ChunkCoordinates> chunks;

    private int chunkSize;

    public PlotLoader(@NotNull Plot plot,
                      @NotNull PlotManager plotManager) {
        this.plot = plot;
        this.plotManager = plotManager;
        boundingBox = plot.getData().getBoundingBox();
        worldName = plot.getData().getWorldName();
        chunks = new HashSet<>();
        loadChunks();
    }

    private void loadChunks() {
        Vector min = boundingBox.getMin();
        Vector max = boundingBox.getMax();
        int x1 = min.getBlockX() & -16;
        int x2 = max.getBlockX() & -16;
        int z1 = min.getBlockZ() & -16;
        int z2 = max.getBlockZ() & -16;

        for (int x = x1; x <= x2; x += 16) {
            for (int z = z1; z <= z2; z += 16) {
                chunks.add(new ChunkCoordinates(x >> 4, z >> 4));
            }
        }
        chunkSize = chunks.size();
    }

    public Set<ChunkCoordinates> getChunks() {
        return chunks;
    }

    public void loadChunks(World world) {
        for (ChunkCoordinates coordinates : chunks) {
            Chunk chunk = world.getChunkAt(coordinates.getX(), coordinates.getZ());
            chunk.setForceLoaded(true);
            chunk.getEntities();
        }
    }

    public String getIndex() {
        return plot.getData().getIndex();
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean containsChunk(Chunk chunk) {
        for (ChunkCoordinates coordinates : chunks) {
            if (coordinates.getX() == chunk.getX() && coordinates.getZ() == chunk.getZ()) {
                return true;
            }
        }
        return false;
    }

    public boolean isDone() {
        return chunkSize == 0;
    }

    public void removeChunk(Chunk chunk) {
        chunkSize--;
        if (!isDone())
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean hasInitialized = DefaultStructuresInitializer.hasInitialized();
                if (!hasInitialized)
                    return;
                plot.pasteStockStructure(null);
                plotManager.removeLoader(PlotLoader.this);
                cancel();
            }
        }.runTaskTimer(plotManager.getPlugin(), 0, 1);
    }
}
