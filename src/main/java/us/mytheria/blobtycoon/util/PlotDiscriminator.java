package us.mytheria.blobtycoon.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import us.mytheria.bloblib.api.BlobLibPetAPI;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;

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
