package io.github.anjoismysign.blobtycoon.entity;

import io.github.anjoismysign.blobtycoon.entity.plotdata.PlotData;
import io.github.anjoismysign.util.Structrador;
import org.jetbrains.annotations.Nullable;

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
