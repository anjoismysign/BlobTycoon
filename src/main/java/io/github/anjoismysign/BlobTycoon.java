package io.github.anjoismysign;

import org.bukkit.Bukkit;
import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.IManagerDirector;
import io.github.anjoismysign.blobeconomy.BlobEconomyAbsent;
import io.github.anjoismysign.blobeconomy.BlobEconomyFound;
import io.github.anjoismysign.blobeconomy.BlobEconomyMiddleman;
import io.github.anjoismysign.director.TycoonManagerDirector;
import io.github.anjoismysign.entity.TycoonPH;

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
