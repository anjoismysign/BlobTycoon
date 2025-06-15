package io.github.anjoismysign.entity.plothelper;

import io.github.anjoismysign.bloblib.entities.BlobSerializableManager;
import io.github.anjoismysign.director.TycoonManagerDirector;

public class PlotHelperInventoryManager extends BlobSerializableManager<PlotHelperInventory> {

    public PlotHelperInventoryManager(TycoonManagerDirector director,
                                      boolean logActivity) {
        super(director,
                x -> x,
                crudable -> PlotHelperInventory.GENERATE(crudable, director),
                "PlotHelperInventory",
                logActivity,
                null,
                null,
                null,
                null);
    }

    /**
     * Deletes the object from the database.
     *
     * @param key The getKey of the object.
     */
    public void deleteObject(String key) {
        crudManager.delete(key);
    }
}
