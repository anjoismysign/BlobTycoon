package io.github.anjoismysign.blobtycoon.listener;

import io.github.anjoismysign.bloblib.entities.BlobListener;
import io.github.anjoismysign.blobtycoon.director.manager.TycoonConfigManager;
import io.github.anjoismysign.blobtycoon.director.manager.TycoonListenerManager;

public abstract class BlobTycoonListener implements BlobListener {
    private final TycoonListenerManager listenerManager;

    public BlobTycoonListener(TycoonListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    public TycoonListenerManager getListenerManager() {
        return listenerManager;
    }

    public TycoonConfigManager getConfigManager() {
        return getListenerManager().getManagerDirector().getConfigManager();
    }
}
