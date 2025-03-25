package us.mytheria.blobtycoon.entity;

import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.utilities.Structrador;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.entity.configuration.PlotPlacingConfiguration;
import us.mytheria.blobtycoon.entity.plotdata.PlotData;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlotPlaceProgress {
    private CompletableFuture<Void> future;
    private boolean isPlacing;
    private final Queue<PlotPlaceRequest> queue;

    private PlotPlaceProgress(CompletableFuture<Void> future, boolean isPlacing) {
        this.future = future;
        this.isPlacing = isPlacing;
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public static PlotPlaceProgress empty() {
        return new PlotPlaceProgress(null, false);
    }

    public static enum PlaceResult {
        SUCCESS,
        QUEUED,
        NOT_QUEUEABLE,
        FAILED,
        PLOT_LOADING
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
}
