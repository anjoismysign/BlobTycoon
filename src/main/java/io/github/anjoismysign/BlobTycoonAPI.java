package io.github.anjoismysign;

import io.github.anjoismysign.director.TycoonManagerDirector;

public class BlobTycoonAPI {
    private static BlobTycoonAPI instance;
    private final TycoonManagerDirector director;
    private final BlobTycoonValuableAPI valuableAPI;

    protected static BlobTycoonAPI getInstance(TycoonManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            BlobTycoonAPI.instance = new BlobTycoonAPI(director);
        }
        return instance;
    }

    public static BlobTycoonAPI getInstance() {
        return getInstance(null);
    }

    private BlobTycoonAPI(TycoonManagerDirector director) {
        this.director = director;
        this.valuableAPI = BlobTycoonValuableAPI.getInstance(director);
    }

    public BlobTycoonValuableAPI getValuableAPI() {
        return valuableAPI;
    }

    public BlobTycoon getPlugin() {
        return director.getPlugin();
    }
}
