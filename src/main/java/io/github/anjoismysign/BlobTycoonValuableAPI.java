package io.github.anjoismysign;

import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.director.TycoonManagerDirector;
import io.github.anjoismysign.entity.valuable.ValuableDriver;

public class BlobTycoonValuableAPI {
    private static BlobTycoonValuableAPI instance;
    private final TycoonManagerDirector director;

    protected static BlobTycoonValuableAPI getInstance(TycoonManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            BlobTycoonValuableAPI.instance = new BlobTycoonValuableAPI(director);
        }
        return instance;
    }

    public static BlobTycoonValuableAPI getInstance() {
        return getInstance(null);
    }

    private BlobTycoonValuableAPI(TycoonManagerDirector director) {
        this.director = director;
    }

    /**
     * Will get a driver from the manager by its key
     *
     * @param key the key to identify the driver
     * @return the driver
     */
    public ValuableDriver getValuableDriver(String key) {
        return director.getValuableDriverManager().getValuableDriver(key);
    }

    /**
     * Will get a driver from a valuable
     *
     * @param valuable The valuable linked with
     * @return The ValuableDriver if found, null otherwise
     */
    @Nullable
    public ValuableDriver getLinkedDriver(String valuable) {
        String driver = director.getValuableDirector()
                .getDriver(valuable);
        if (driver == null)
            return null;
        return getValuableDriver(driver);
    }

    /**
     * Will add a driver to the manager
     *
     * @param key    the key to identify the driver
     * @param driver the driver to add
     * @return the previous driver if it existed
     */
    @Nullable
    public ValuableDriver addDriver(String key, ValuableDriver driver) {
        return director.getValuableDriverManager().addDriver(key, driver);
    }
}
