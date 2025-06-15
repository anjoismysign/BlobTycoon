package io.github.anjoismysign.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import io.github.anjoismysign.bloblib.api.BlobLibPetAPI;
import io.github.anjoismysign.BlobTycoonInternalAPI;

public class PlotDiscriminator {

    /**
     * Checks whether the entity should not be removed from the plot.
     *
     * @param entity The entity to check.
     * @return Whether the entity should not be removed.
     */
    public static boolean dontRemove(Entity entity) {
        if (entity.getType() == EntityType.PLAYER)
            return true;
        if (BlobTycoonInternalAPI.getInstance().isPlotHelper(entity) != null)
            return true;
        return BlobLibPetAPI.getInstance().isPet(entity);
    }
}
