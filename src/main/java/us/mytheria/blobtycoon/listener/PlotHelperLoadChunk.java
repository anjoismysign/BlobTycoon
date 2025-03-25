package us.mytheria.blobtycoon.listener;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.EntitiesLoadEvent;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.ChunkCoordinates;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.configuration.PlotHelperConfiguration;
import us.mytheria.blobtycoon.entity.plothelper.PlotHelper;

import java.util.List;

public class PlotHelperLoadChunk extends BlobTycoonListener {
    public PlotHelperLoadChunk(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onLoad(EntitiesLoadEvent event) {
        Chunk chunk = event.getChunk();
        List<PlotHelper> plotHelpers = getListenerManager().getManagerDirector().getPlotProfileManager().getAll().stream()
                .map(PlotProfile::plotHelper)
                .filter(plotHelper -> {
                    ChunkCoordinates chunkCoordinates = plotHelper.getChunkCoordinates();
                    if (chunkCoordinates == null)
                        return false;
                    return chunkCoordinates.isChunk(chunk);
                })
                .toList();
        plotHelpers.forEach(PlotHelper::reload);
    }

    public boolean checkIfShouldRegister() {
        return PlotHelperConfiguration.getInstance().isEnabled();
    }
}
