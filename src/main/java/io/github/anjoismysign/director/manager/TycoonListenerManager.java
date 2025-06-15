package io.github.anjoismysign.director.manager;

import org.bukkit.Bukkit;
import io.github.anjoismysign.bloblib.entities.ListenerManager;
import io.github.anjoismysign.director.TycoonManagerDirector;
import io.github.anjoismysign.listener.BlobTycoonListener;
import io.github.anjoismysign.listener.InternalTransferFunds;
import io.github.anjoismysign.listener.NewProfileKit;
import io.github.anjoismysign.listener.ObjectAssetHeldTutorialListener;
import io.github.anjoismysign.listener.OfflineEarningListener;
import io.github.anjoismysign.listener.PlotHelperCommunityTrades;
import io.github.anjoismysign.listener.PlotHelperEquip;
import io.github.anjoismysign.listener.PlotHelperHit;
import io.github.anjoismysign.listener.PlotHelperInteract;
import io.github.anjoismysign.listener.PlotHelperLoadChunk;
import io.github.anjoismysign.listener.PlotHelperPreventDamage;
import io.github.anjoismysign.listener.RackAssetHeldTutorialListener;
import io.github.anjoismysign.listener.ShopArticleTransferFundsListener;
import io.github.anjoismysign.listener.StructureAssetHeldTutorialListener;
import io.github.anjoismysign.listener.TranslateOnProfileLoad;
import io.github.anjoismysign.listener.TranslationItemApplier;
import io.github.anjoismysign.listener.TycoonMenuListener;
import io.github.anjoismysign.listener.objectmodel.ObjectModelPlaceHit;
import io.github.anjoismysign.listener.objectmodel.ObjectModelPlaceInteract;
import io.github.anjoismysign.listener.objectmodel.ObjectModelRemoveHit;
import io.github.anjoismysign.listener.objectmodel.ObjectModelRemoveInteract;
import io.github.anjoismysign.listener.structuremodel.StructureModelRemoveHit;
import io.github.anjoismysign.listener.structuremodel.StructureModelRemoveInteract;
import io.github.anjoismysign.listener.structuremodel.StructureModelSelection;

public class TycoonListenerManager extends ListenerManager {
    private TycoonManagerDirector managerDirector;

    public TycoonListenerManager(TycoonManagerDirector managerDirector) {
        super(managerDirector);
        this.managerDirector = managerDirector;
        add(StructureModelSelection.getInstance(this));
        add(new TycoonMenuListener(this));
        add(new OfflineEarningListener(this));
        add(new TranslationItemApplier(this));
        add(new TranslateOnProfileLoad(this));
        add(new PlotHelperLoadChunk(this));
        add(new PlotHelperHit(this));
        add(new PlotHelperInteract(this));
        add(new PlotHelperPreventDamage(this));
        add(new PlotHelperEquip(this));
        add(new InternalTransferFunds(this));
        add(new PlotHelperCommunityTrades(this));

        add(new NewProfileKit(this));
        add(new ObjectModelRemoveHit(this));
        add(new ObjectModelRemoveInteract(this));
        add(new ObjectModelPlaceHit(this));
        add(new ObjectModelPlaceInteract(this));
        add(new StructureModelRemoveHit(this));
        add(new StructureModelRemoveInteract(this));
        add(new RackAssetHeldTutorialListener(this));
        add(new StructureAssetHeldTutorialListener(this));
        add(new ObjectAssetHeldTutorialListener(this));


        Bukkit.getScheduler().runTask(getPlugin(), () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("BlobRP")) {
                BlobTycoonListener listener = new ShopArticleTransferFundsListener(this);
                add(listener);
            }
        });
    }

    @Override
    public TycoonManagerDirector getManagerDirector() {
        return managerDirector;
    }
}