package io.github.anjoismysign.blobtycoon.director;

import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.blobtycoon.BlobTycoon;

public class TycoonManager extends GenericManager<BlobTycoon, TycoonManagerDirector> {

    public TycoonManager(TycoonManagerDirector managerDirector) {
        super(managerDirector);
    }
}