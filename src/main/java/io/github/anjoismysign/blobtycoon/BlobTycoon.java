package io.github.anjoismysign.blobtycoon;

import org.bukkit.Bukkit;
import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.IManagerDirector;
import io.github.anjoismysign.blobtycoon.blobeconomy.BlobEconomyAbsent;
import io.github.anjoismysign.blobtycoon.blobeconomy.BlobEconomyFound;
import io.github.anjoismysign.blobtycoon.blobeconomy.BlobEconomyMiddleman;
import io.github.anjoismysign.blobtycoon.director.TycoonManagerDirector;
import io.github.anjoismysign.blobtycoon.entity.TycoonPH;

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
