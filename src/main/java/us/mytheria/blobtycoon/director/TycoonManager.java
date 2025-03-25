package us.mytheria.blobtycoon.director;

import us.mytheria.bloblib.entities.GenericManager;
import us.mytheria.blobtycoon.BlobTycoon;

public class TycoonManager extends GenericManager<BlobTycoon, TycoonManagerDirector> {

    public TycoonManager(TycoonManagerDirector managerDirector) {
        super(managerDirector);
    }
}