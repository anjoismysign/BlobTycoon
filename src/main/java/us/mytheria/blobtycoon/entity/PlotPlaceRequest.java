package us.mytheria.blobtycoon.entity;

import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.utilities.Structrador;
import us.mytheria.blobtycoon.entity.plotdata.PlotData;

/**
 * Represents a request to make a plot place.
 *
 * @param getStructrador  the getStructrador to use
 * @param getPlotData     the PlotData to use
 * @param isChained       whether to chain the structure in multiple ticks or paste it all in a single tick
 * @param getWhenComplete the runnable to run when the structure is pasted
 * @param operation       the operation to perform on the structure
 */
public record PlotPlaceRequest(Structrador getStructrador,
                               PlotData getPlotData,
                               boolean isChained,
                               @Nullable Runnable getWhenComplete,
                               @Nullable DirectionOperation operation) {
}
