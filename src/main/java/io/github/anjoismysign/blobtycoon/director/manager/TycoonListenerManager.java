package io.github.anjoismysign.blobtycoon.director.manager;

import io.github.anjoismysign.bloblib.entities.ListenerManager;
import io.github.anjoismysign.blobtycoon.director.TycoonManagerDirector;
import io.github.anjoismysign.blobtycoon.listener.BlobTycoonListener;
import io.github.anjoismysign.blobtycoon.listener.InternalTransferFunds;
import io.github.anjoismysign.blobtycoon.listener.NewProfileKit;
import io.github.anjoismysign.blobtycoon.listener.ObjectAssetHeldTutorialListener;
import io.github.anjoismysign.blobtycoon.listener.OfflineEarningListener;
import io.github.anjoismysign.blobtycoon.listener.PlotHelperCommunityTrades;
import io.github.anjoismysign.blobtycoon.listener.PlotHelperEquip;
import io.github.anjoismysign.blobtycoon.listener.PlotHelperHit;
import io.github.anjoismysign.blobtycoon.listener.PlotHelperInteract;
import io.github.anjoismysign.blobtycoon.listener.PlotHelperLoadChunk;
import io.github.anjoismysign.blobtycoon.listener.PlotHelperPreventDamage;
import io.github.anjoismysign.blobtycoon.listener.RackAssetHeldTutorialListener;
import io.github.anjoismysign.blobtycoon.listener.ShopArticleTransferFundsListener;
import io.github.anjoismysign.blobtycoon.listener.StructureAssetHeldTutorialListener;
import io.github.anjoismysign.blobtycoon.listener.TranslateOnProfileLoad;
import io.github.anjoismysign.blobtycoon.listener.TranslationItemApplier;
import io.github.anjoismysign.blobtycoon.listener.TycoonMenuListener;
import io.github.anjoismysign.blobtycoon.listener.objectmodel.ObjectModelPlaceHit;
import io.github.anjoismysign.blobtycoon.listener.objectmodel.ObjectModelPlaceInteract;
import io.github.anjoismysign.blobtycoon.listener.objectmodel.ObjectModelRemoveHit;
import io.github.anjoismysign.blobtycoon.listener.objectmodel.ObjectModelRemoveInteract;
import io.github.anjoismysign.blobtycoon.listener.structuremodel.StructureModelRemoveHit;
import io.github.anjoismysign.blobtycoon.listener.structuremodel.StructureModelRemoveInteract;
import io.github.anjoismysign.blobtycoon.listener.structuremodel.StructureModelSelection;
import org.bukkit.Bukkit;

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