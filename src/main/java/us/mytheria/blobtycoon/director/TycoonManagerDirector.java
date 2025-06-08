package us.mytheria.blobtycoon.director;

import me.anjoismysign.blobpets.entity.petexpansion.PetExpansionDirector;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.BlobObject;
import us.mytheria.bloblib.entities.GenericManagerDirector;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.bloblib.managers.Manager;
import us.mytheria.blobtycoon.BlobTycoon;
import us.mytheria.blobtycoon.blobpets.BlobPetsMiddleman;
import us.mytheria.blobtycoon.blobpets.Found;
import us.mytheria.blobtycoon.blobpets.NotFound;
import us.mytheria.blobtycoon.command.BlobTycoonCmd;
import us.mytheria.blobtycoon.command.VisitCmd;
import us.mytheria.blobtycoon.director.manager.ExpansionManager;
import us.mytheria.blobtycoon.director.manager.PlotManager;
import us.mytheria.blobtycoon.director.manager.StructureTracker;
import us.mytheria.blobtycoon.director.manager.TycoonConfigManager;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.PlotProfileManager;
import us.mytheria.blobtycoon.entity.TycoonPet;
import us.mytheria.blobtycoon.entity.TycoonPlayerManager;
import us.mytheria.blobtycoon.entity.asset.ObjectAsset;
import us.mytheria.blobtycoon.entity.asset.RackAsset;
import us.mytheria.blobtycoon.entity.asset.StructureAsset;
import us.mytheria.blobtycoon.entity.blobrp.BlobRPMiddleman;
import us.mytheria.blobtycoon.entity.mechanics.MechanicsData;
import us.mytheria.blobtycoon.entity.plothelper.PlotHelperInventoryManager;
import us.mytheria.blobtycoon.entity.structure.ObjectModel;
import us.mytheria.blobtycoon.entity.structure.StorageModel;
import us.mytheria.blobtycoon.entity.structure.StructureModel;
import us.mytheria.blobtycoon.entity.structure.TycoonModel;
import us.mytheria.blobtycoon.entity.structure.TycoonModelHolder;
import us.mytheria.blobtycoon.entity.valuable.ValuableDirector;
import us.mytheria.blobtycoon.entity.valuable.ValuableDriverManager;
import us.mytheria.blobtycoon.event.AsyncBlobTycoonLoadEvent;

import java.io.File;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public class TycoonManagerDirector extends GenericManagerDirector<BlobTycoon>
        implements Listener {
    private final Logger logger;
    private final BlobPetsMiddleman blobPetsMiddleman;

    public TycoonManagerDirector(BlobTycoon plugin) {
        super(plugin);
        blobPetsMiddleman = Bukkit.getPluginManager()
                .getPlugin("BlobPets") == null ?
                NotFound.getInstance() :
                Found.getInstance(this);
        logger = plugin.getLogger();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        registerMetaBlobInventory("Tycoon-Menu");
        registerBlobInventory("Change-Plot");
        registerBlobInventory("Teller-Withdraw");
        registerBlobInventory("PlotData");
        registerBlobInventory("Action-Holder-Allowed");
        registerBlobInventory("Action-Holder-Denied");
        registerBlobInventory("New-Profile-Kit");
        registerBlobInventory("BlobTycoon-New-Profile");
        registerBlobInventory("BlobTycoon-Switch-Profile");
        registerBlobInventory("BlobTycoon-Invite-Profile");
        registerBlobInventory("BlobTycoon-Delete-Profile");
        registerBlobInventory("Rebirth");
        registerBlobMessage("blobtycoon_visitors");
        registerBlobMessage("blobtycoon_rebirth");
        registerBlobMessage("blobtycoon_plot_helper");
        registerTranslatableSnippet("plothelper_translatable_snippets");
        registerTranslatableBlock("plothelper_translatable_blocks");
        registerTranslatableItem("plothelper_translatable_items");
        registerBlobInventory("Plot-Helper");
        registerBlobInventory("Plot-Helper-Inventories");
        registerBlobInventory("Plot-Helper-Trading");
        registerBlobInventory("Trades-Marketplace");
        registerBlobInventory("Trades-Manager");
        registerBlobInventory("Create-Trade");
        registerBlobInventory("View-Trade");
        registerBlobInventory("Cancel-Trade");
        registerBlobInventory("Community-Trades");
        addManager("ConfigManager", new TycoonConfigManager(this));
        boolean tinyDebug = getConfigManager().tinyDebug();
        addDirector("MechanicsData", MechanicsData::fromFile, false);
        if (tinyDebug)
            logger.warning("Loading MechanicsData");
        getMechanicsDataDirector().whenObjectManagerFilesLoad(mechanicsDataObjectManager -> {
            if (tinyDebug)
                logger.warning("Done loading MechanicsData");
            addManager("StructureTracker", new StructureTracker(this));
            if (tinyDebug)
                logger.warning("Loading StructureTracker");
            getStructureTracker().whenDoneLoading(() -> {
                if (tinyDebug)
                    logger.warning("Done loading StructureTracker");
                addModelHolderDirector("RackAsset",
                        RackAsset::fromFile);
                if (tinyDebug)
                    logger.warning("Loading RackAsset");
                addModelHolderDirector("ObjectAsset",
                        ObjectAsset::fromFile);
                if (tinyDebug)
                    logger.warning("Loading ObjectAsset");
                addModelHolderDirector("StructureAsset",
                        StructureAsset::fromFile);
                if (tinyDebug)
                    logger.warning("Loading StructureAsset");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (isReloading())
                            return;
                        this.cancel();
                        if (tinyDebug)
                            logger.warning("Loading ExpansionManager");
                        addManager("ExpansionManager",
                                new ExpansionManager(TycoonManagerDirector.this));
                        getPlotProfileManager().reload();
                        if (tinyDebug)
                            logger.warning("Done loading ExpansionManager");
                        AsyncBlobTycoonLoadEvent event = new AsyncBlobTycoonLoadEvent(
                                getStructureTracker().getTracked(),
                                Collections.unmodifiableCollection(getRackAssetDirector().getObjectManager().values()),
                                Collections.unmodifiableCollection(getObjectAssetDirector().getObjectManager().values()),
                                Collections.unmodifiableCollection(getStructureAssetDirector().getObjectManager().values()),
                                true);
                        Bukkit.getPluginManager().callEvent(event);
                    }
                }.runTaskTimerAsynchronously(getPlugin(), 1L, 1L);
            });
        });
        addManager("PlotManager", new PlotManager(this));
        addManager("ListenerManager", new TycoonListenerManager(this));
        addManager("TycoonPlayerManager",
                new TycoonPlayerManager(this, getConfigManager().tinyDebug()));
        addManager("PlotHelperInventoryManager",
                new PlotHelperInventoryManager(this, getConfigManager().tinyDebug()));
        addManager("PlotProfileManager",
                new PlotProfileManager(this, getConfigManager().tinyDebug()));
        addManager("ValuableDirector", new ValuableDirector(this));
        if (tinyDebug)
            logger.warning("Loading ValuableDirector");
        getValuableDirector().whenObjectManagerFilesLoad(a -> {
            if (tinyDebug)
                logger.warning("Done loading ValuableDirector");
        });
        addManager("ValuableDriverManager", new ValuableDriverManager(this));
        us.mytheria.blobtycoon.entity.blobpets.BlobPetsMiddleman blobPetsMiddleman = us.mytheria.blobtycoon.entity.blobpets.BlobPetsMiddleman.get();
        boolean isBlobPetsEnabled = blobPetsMiddleman.isEnabled();
        if (isBlobPetsEnabled) {
            addManager("TycoonPetDirector", blobPetsMiddleman.instantiateTycoonPetDirector(this));
            if (tinyDebug)
                logger.warning("Loading TycoonPetDirector");
        }
        BlobTycoonCmd.of(this);
        VisitCmd.of(this);
    }

    private <T extends TycoonModelHolder<R>, R extends TycoonModel>
    void addModelHolderDirector(String objectName,
                                BiFunction<File, TycoonManagerDirector, T> function) {
        ModelHolderDirector<T, R> director = ModelHolderDirector.of(this, file -> {
            return function.apply(file, this);
        }, objectName);
        addManager(objectName + "Director", director);
        boolean tinyDebug = getConfigManager().tinyDebug();
        if (tinyDebug)
            logger.warning("Loading " + objectName + "Director");
        director.whenObjectManagerFilesLoad(a -> {
            if (tinyDebug)
                logger.warning("Done loading " + objectName + "Director");
        });
    }

    private <T extends BlobObject> void addEncapsulatedDirector(String objectName,
                                                                BiFunction<File, TycoonManagerDirector, T> function) {
        addDirector(objectName, file -> {
            return function.apply(file, this);
        }, false);
    }

    @EventHandler
    public void onLoad(AsyncBlobTycoonLoadEvent event) {
        boolean tinyDebug = getConfigManager().tinyDebug();
        if (tinyDebug)
            logger.warning("BlobTycoon has loaded");
        BlobRPMiddleman blobrp = BlobRPMiddleman.get();
        blobrp.reloadMerchants();
        if (tinyDebug)
            logger.warning("Reloaded Merchants");
        blobrp.reloadRecipes();
        if (tinyDebug)
            logger.warning("Reloaded Recipes");
    }

    /**
     * From top to bottom, follow the order.
     */
    @Override
    public void reload() {
        getPlotManager().reload();
        getConfigManager().reload();
        boolean tinyDebug = getConfigManager().tinyDebug();
        getListenerManager().reload();
        getMechanicsDataDirector().reload();
        if (tinyDebug)
            logger.warning("Reloading MechanicsData");
        getMechanicsDataDirector().whenObjectManagerFilesLoad(mechanicsDataObjectManager -> {
            if (tinyDebug)
                logger.warning("Done reloading MechanicsData");
            getStructureTracker().reload();
            if (tinyDebug)
                logger.warning("Reloading StructureTracker");
        });
        getValuableDirector().reload();
        if (tinyDebug)
            logger.warning("Reloading ValuableDirector");
        getValuableDirector().whenObjectManagerFilesLoad(a -> {
            if (tinyDebug)
                logger.warning("Done reloading ValuableDirector");
        });
        getTycoonPetDirector().ifPresent(director -> {
            blobPetsMiddleman.reload();
            blobPetsMiddleman.whenDoneLoading(() -> {
                if (tinyDebug)
                    logger.warning("Done reloading TycoonPetDirector");
            });
        });
        getStructureTracker().whenDoneLoading(() -> {
            if (tinyDebug)
                logger.warning("Done reloading StructureTracker");
            getRackAssetDirector().reload();
            getObjectAssetDirector().reload();
            getStructureAssetDirector().reload();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (isReloading())
                        return;
                    cancel();
                    if (tinyDebug)
                        logger.warning("Reloading ExpansionManager");
                    getExpansionManager().reload();
                    if (tinyDebug)
                        logger.warning("Done reloading ExpansionManager");
                    getPlotProfileManager().reload();
                    AsyncBlobTycoonLoadEvent event = new AsyncBlobTycoonLoadEvent(
                            getStructureTracker().getTracked(),
                            Collections.unmodifiableCollection(getRackAssetDirector().getObjectManager().values()),
                            Collections.unmodifiableCollection(getObjectAssetDirector().getObjectManager().values()),
                            Collections.unmodifiableCollection(getStructureAssetDirector().getObjectManager().values()),
                            false);
                    Bukkit.getPluginManager().callEvent(event);
                }
            }.runTaskTimerAsynchronously(getPlugin(), 1L, 1L);
            if (tinyDebug)
                logger.warning("Reloading RackAssetDirector");
            if (tinyDebug)
                logger.warning("Reloading ObjectAssetDirector");
            if (tinyDebug)
                logger.warning("Reloading StructureAssetDirector");
            getRackAssetDirector().whenObjectManagerFilesLoad(a -> {
                if (tinyDebug)
                    logger.warning("Done reloading RackAssetDirector");
            });
            getObjectAssetDirector().whenObjectManagerFilesLoad(a -> {
                if (tinyDebug)
                    logger.warning("Done reloading ObjectAssetDirector");
            });
            getStructureAssetDirector().whenObjectManagerFilesLoad(a -> {
                if (tinyDebug)
                    logger.warning("Done reloading StructureAssetDirector");
            });
        });
        getTycoonPlayerManager().reload();
    }

    @Override
    public void unload() {
        getPlotProfileManager().unload();
        getTycoonPlayerManager().unload();
    }

    @Override
    public boolean isReloading() {
        return getMechanicsDataDirector().isReloading() ||
                getValuableDirector().isReloading() ||
                getStructureTracker().isReloading() ||
                getRackAssetDirector().isReloading() ||
                getObjectAssetDirector().isReloading() ||
                getStructureAssetDirector().isReloading() ||
                blobPetsMiddleman.isReloading();
    }

    @NotNull
    public final PlotManager getPlotManager() {
        return getManager("PlotManager", PlotManager.class);
    }

    @NotNull
    public final PlotProfileManager getPlotProfileManager() {
        return getManager("PlotProfileManager", PlotProfileManager.class);
    }

    @NotNull
    public final TycoonPlayerManager getTycoonPlayerManager() {
        return getManager("TycoonPlayerManager", TycoonPlayerManager.class);
    }

    @NotNull
    public final PlotHelperInventoryManager getPlotHelperInventoryManager() {
        return getManager("PlotHelperInventoryManager", PlotHelperInventoryManager.class);
    }

    @NotNull
    public final TycoonConfigManager getConfigManager() {
        return getManager("ConfigManager", TycoonConfigManager.class);
    }

    @NotNull
    public final TycoonListenerManager getListenerManager() {
        return getManager("ListenerManager", TycoonListenerManager.class);
    }

    @NotNull
    public final StructureTracker getStructureTracker() {
        return getManager("StructureTracker", StructureTracker.class);
    }

    @NotNull
    public final ValuableDirector getValuableDirector() {
        return getManager("ValuableDirector", ValuableDirector.class);
    }

    @NotNull
    public final ValuableDriverManager getValuableDriverManager() {
        return getManager("ValuableDriverManager", ValuableDriverManager.class);
    }

    @NotNull
    public final ObjectDirector<MechanicsData> getMechanicsDataDirector() {
        return getDirector("MechanicsData", MechanicsData.class);
    }

    @NotNull
    public final ExpansionManager getExpansionManager() {
        return getManager("ExpansionManager", ExpansionManager.class);
    }

    @NotNull
    public final ModelHolderDirector<RackAsset, StorageModel> getRackAssetDirector() {
        return (ModelHolderDirector<RackAsset, StorageModel>) getManager("RackAssetDirector");
    }

    @NotNull
    public final ModelHolderDirector<StructureAsset, StructureModel> getStructureAssetDirector() {
        return (ModelHolderDirector<StructureAsset, StructureModel>) getManager("StructureAssetDirector");
    }

    @NotNull
    public final ModelHolderDirector<ObjectAsset, ObjectModel> getObjectAssetDirector() {
        return (ModelHolderDirector<ObjectAsset, ObjectModel>) getManager("ObjectAssetDirector");
    }

    /**
     * Gets the TycoonPetDirector if BlobPets is enabled.
     *
     * @return The TycoonPetDirector if BlobPets is enabled, otherwise an empty Optional.
     */
    public final Optional<Object> getTycoonPetDirector() {
        Manager manager = getManager("TycoonPetDirector");
        if (manager == null)
            return Optional.empty();
        PetExpansionDirector<TycoonPet> director = (PetExpansionDirector<TycoonPet>) manager;
        return Optional.of(director);
    }

    public BlobPetsMiddleman getBlobPetsMiddleman() {
        return blobPetsMiddleman;
    }
}