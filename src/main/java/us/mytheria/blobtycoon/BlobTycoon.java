package us.mytheria.blobtycoon;

import org.bukkit.Bukkit;
import us.mytheria.bloblib.managers.BlobPlugin;
import us.mytheria.bloblib.managers.IManagerDirector;
import us.mytheria.blobtycoon.blobeconomy.BlobEconomyAbsent;
import us.mytheria.blobtycoon.blobeconomy.BlobEconomyFound;
import us.mytheria.blobtycoon.blobeconomy.BlobEconomyMiddleman;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.entity.TycoonPH;

public final class BlobTycoon extends BlobPlugin {
    private IManagerDirector proxy;
    private BlobTycoonValuableAPI valuableAPI;
    private BlobTycoonInternalAPI internalAPI;
    private BlobTycoonAPI api;
    private TycoonPH tycoonPH;
    private BlobEconomyMiddleman blobEconomyMiddleman;

    @Override
    public void onEnable() {
        TycoonManagerDirector director = new TycoonManagerDirector(this);
        proxy = director.proxy();
        valuableAPI = BlobTycoonValuableAPI.getInstance(director);
        internalAPI = BlobTycoonInternalAPI.getInstance(director);
        api = BlobTycoonAPI.getInstance(director);
        tycoonPH = new TycoonPH(this);
        if (Bukkit.getPluginManager().isPluginEnabled("BlobEconomy"))
            blobEconomyMiddleman = BlobEconomyFound.getInstance();
        else
            blobEconomyMiddleman = BlobEconomyAbsent.getInstance();
    }

    public IManagerDirector getManagerDirector() {
        return proxy;
    }

    public BlobTycoonAPI getApi() {
        return api;
    }

    public BlobEconomyMiddleman getBlobEconomyMiddleman() {
        return blobEconomyMiddleman;
    }
}
