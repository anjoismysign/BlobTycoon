package us.mytheria.blobtycoon.director.manager;

import us.mytheria.bloblib.entities.ListenerManager;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.listener.InternalTransferFunds;
import us.mytheria.blobtycoon.listener.NewProfileKit;
import us.mytheria.blobtycoon.listener.ObjectAssetHeldTutorialListener;
import us.mytheria.blobtycoon.listener.OfflineEarningListener;
import us.mytheria.blobtycoon.listener.PlotHelperCommunityTrades;
import us.mytheria.blobtycoon.listener.PlotHelperEquip;
import us.mytheria.blobtycoon.listener.PlotHelperHit;
import us.mytheria.blobtycoon.listener.PlotHelperInteract;
import us.mytheria.blobtycoon.listener.PlotHelperLoadChunk;
import us.mytheria.blobtycoon.listener.PlotHelperPreventDamage;
import us.mytheria.blobtycoon.listener.RackAssetHeldTutorialListener;
import us.mytheria.blobtycoon.listener.StructureAssetHeldTutorialListener;
import us.mytheria.blobtycoon.listener.TranslateOnProfileLoad;
import us.mytheria.blobtycoon.listener.TranslationItemApplier;
import us.mytheria.blobtycoon.listener.TycoonMenuListener;
import us.mytheria.blobtycoon.listener.objectmodel.ObjectModelPlaceHit;
import us.mytheria.blobtycoon.listener.objectmodel.ObjectModelPlaceInteract;
import us.mytheria.blobtycoon.listener.objectmodel.ObjectModelRemoveHit;
import us.mytheria.blobtycoon.listener.objectmodel.ObjectModelRemoveInteract;
import us.mytheria.blobtycoon.listener.structuremodel.StructureModelRemoveHit;
import us.mytheria.blobtycoon.listener.structuremodel.StructureModelRemoveInteract;
import us.mytheria.blobtycoon.listener.structuremodel.StructureModelSelection;

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
    }

    @Override
    public TycoonManagerDirector getManagerDirector() {
        return managerDirector;
    }
}