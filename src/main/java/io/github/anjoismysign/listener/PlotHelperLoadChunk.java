package io.github.anjoismysign.listener;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.EntitiesLoadEvent;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.ChunkCoordinates;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.configuration.PlotHelperConfiguration;
import io.github.anjoismysign.entity.plothelper.PlotHelper;

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
