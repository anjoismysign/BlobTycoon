package io.github.anjoismysign.entity;

import org.bukkit.Location;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.utilities.Structrador;
import io.github.anjoismysign.entity.plotdata.PlotData;

import java.util.Objects;

public class Plot {
    @NotNull
    private final PlotPlaceProgress progress;
    @NotNull
    private PlotData data;

    /**
     * Creates a Plot using a PlotData
     *
     * @return the plot
     */
    public static Plot of(@NotNull PlotData data) {
        Objects.requireNonNull(data);
        return new Plot(data, PlotPlaceProgress.empty());
    }

    /**
     * Creates a Plot using the stock structure.
     * Should create getHomePosition at the center of the plot.
     *
     * @param index  the getIndex of the plot
     * @param point1 the first point of the plot
     * @param size   the getSize of the plot
     * @return the plot
     */
    public static Plot STOCK(String index, Location point1, BlockVector size) {
        PlotData data = PlotData.of(index, point1, size, StructureDirection.NORTH, null);
        PlotPlaceProgress progress = PlotPlaceProgress.empty();
        Plot plot = new Plot(data, progress);
        plot.pasteStockStructure(null);
        return plot;
    }

    private Plot(
            @NotNull PlotData data,
            @NotNull PlotPlaceProgress progress) {
        this.data = data;
        this.progress = progress;
    }

    @NotNull
    public PlotData getData() {
        return data;
    }

//    PlotData getData, PlotPlaceProgress getProgress

    public void setData(@NotNull PlotData data) {
        Objects.requireNonNull(data, "'data' cannot be null");
        this.data = data;
    }

    @NotNull
    public PlotPlaceProgress getProgress() {
        return progress;
    }

    /**
     * Will paste the stock structure.
     *
     * @param whenComplete the runnable to run when the structure is pasted
     */
    public void pasteStockStructure(@Nullable Runnable whenComplete) {
        paste(DefaultStructures.STOCK.getStructrador(),
                true,
                whenComplete,
                null,
                true,
                new DirectionOperation(StructureDirection.NORTH, data.getDirection()));
    }

    /**
     * Will paste a structure.
     * If provided a PlotExpansion, it will restructure the plot
     * before pasting the structure.
     *
     * @param getStructrador   the Structrador to use
     * @param getChained       whether to chain the structure in multiple ticks or paste it all in a single tick
     * @param getWhenComplete  the runnable to run when the structure is pasted
     * @param getPlotExpansion the expansion to restructure
     * @param getOperation     the operation to perform on the structure
     * @return The result of the paste
     */
    public PlotPlaceProgress.PlaceResult paste(Structrador getStructrador,
                                               boolean getChained,
                                               @Nullable Runnable getWhenComplete,
                                               @Nullable PlotExpansion getPlotExpansion,
                                               boolean getSkipQueue,
                                               @Nullable DirectionOperation getOperation) {
        return progress.paste(new PlotPlaceRequest(getStructrador, data, getChained, getWhenComplete, getOperation),
                getPlotExpansion, getSkipQueue);
    }

    /**
     * Clears progress queue.
     */
    public void clear() {
        getProgress().clear();
    }
}
