package io.github.anjoismysign.blobtycoon.entity;

import io.github.anjoismysign.bloblib.api.BlobLibDisguiseAPI;
import io.github.anjoismysign.bloblib.disguises.DisguiseManager;
import io.github.anjoismysign.bloblib.disguises.Disguiser;
import io.github.anjoismysign.bloblib.middleman.LibsDisguises;
import io.github.anjoismysign.blobtycoon.util.BTDisguiseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.utilities.Structrador;
import io.github.anjoismysign.blobtycoon.BlobTycoonInternalAPI;
import io.github.anjoismysign.blobtycoon.entity.configuration.PlotPlacingConfiguration;
import io.github.anjoismysign.blobtycoon.entity.plotdata.PlotData;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlotPlaceProgress {
    private final Queue<PlotPlaceRequest> queue;
    private CompletableFuture<Void> future;
    private boolean isPlacing;

    public static PlotPlaceProgress empty() {
        return new PlotPlaceProgress(null, false);
    }

    private PlotPlaceProgress(CompletableFuture<Void> future, boolean isPlacing) {
        this.future = future;
        this.isPlacing = isPlacing;
        this.queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Attempts to paste a structure to a Plot.
     * If there is a structure waiting to be pasted,
     * it won't be pasted and will return false.
     * If there is a structure being pasted but
     * none waiting, it will be queued.
     *
     * @param request       the request to paste
     * @param plotExpansion the expansion to restructure
     * @param skipQueue     whether to skip the queue
     * @return the result of the paste
     */
    public PlaceResult paste(PlotPlaceRequest request,
                             @Nullable PlotExpansion plotExpansion,
                             boolean skipQueue) {
        if (isPlacing) {
            if (isWaiting() && !skipQueue)
                return PlaceResult.NOT_QUEUEABLE;
            else {
                queue.offer(request);
                return PlaceResult.QUEUED;
            }
        } else {
            isPlacing = true;
            Structrador structrador = request.getStructrador();
            PlotData plotData = request.getPlotData();
            Runnable whenComplete = request.getWhenComplete();
            //From now on, will proceed placing the structure
            if (plotExpansion != null)
                plotExpansion.restructure(request.getPlotData());
            request.getPlotData().removeEntities();
            if (request.isChained()) {
                StructureRotation rotation = StructureRotation.NONE;
                if (request.operation() != null)
                    rotation = request.operation().perform();
                future = structrador.chainedPlace(plotData.getMinPoint(),
                        true,
                        rotation,
                        Mirror.NONE,
                        0,
                        1,
                        new Random(),
                        BlobTycoonInternalAPI.getInstance().getMaxPlacedPerSecond(),
                        1,
                        block -> {
                            Location blockLocation = block.getLocation();
                            PlotPlacingConfiguration.getInstance().getBlockParticleConfiguration()
                                    .spawn(blockLocation);
                        }, entity -> {
                            Location entityLocation = entity.getLocation();
                            PlotPlacingConfiguration.getInstance().getEntityParticleConfiguration()
                                    .spawn(entityLocation);
                            TycoonKey.transientize(entity);
                        }).getFuture();
            } else {
                future = new CompletableFuture<>();
                structrador.simultaneousPlace(plotData.getMinPoint(), true,
                        StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
                future.complete(null);
            }
            future.thenRun(() -> {
                isPlacing = false;
                if (whenComplete != null) {
                    whenComplete.run();
                }
                Disguiser disguiser = BlobLibDisguiseAPI.getInstance().getDisguiser();
                if (disguiser.hasEngine()){
                    BTDisguiseAPI api = BTDisguiseAPI.INSTANCE;
                    request.getPlotData().getAllEntities().forEach(entity -> {
                        PersistentDataContainer container = entity.getPersistentDataContainer();
                        boolean has = api.has(container);
                        if (!has){
                            return;
                        }
                        @Nullable String raw = api.raw(container);
                        if (raw == null){
                            return;
                        }
                        disguiser.disguiseEntity(raw, entity);
                    });
                }
                PlotPlaceRequest poll = queue.poll();
                if (poll != null) {
                    paste(poll, plotExpansion, false);
                }
            });
            return PlaceResult.SUCCESS;
        }
    }

    /**
     * Clears the queue.
     */
    public void clear() {
        queue.clear();
    }

    /**
     * Will return true if there is a structure waiting to be pasted.
     *
     * @return true if there is a structure waiting to be pasted
     */
    public boolean isWaiting() {
        return !queue.isEmpty();
    }

    /**
     * Will return true if there is a structure being pasted.
     *
     * @return true if there is a structure being pasted
     */
    public boolean isPlacing() {
        return isPlacing;
    }

    public enum PlaceResult {
        SUCCESS,
        QUEUED,
        NOT_QUEUEABLE,
        FAILED,
        PLOT_LOADING
    }
}
