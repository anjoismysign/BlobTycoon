package io.github.anjoismysign.director;

import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.BlobTycoon;

public class TycoonManager extends GenericManager<BlobTycoon, TycoonManagerDirector> {

    public TycoonManager(TycoonManagerDirector managerDirector) {
        super(managerDirector);
    }
}