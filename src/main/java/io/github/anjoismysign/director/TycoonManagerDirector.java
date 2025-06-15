package io.github.anjoismysign.director;

import io.github.anjoismysign.BlobTycoon;
import io.github.anjoismysign.bloblib.entities.BlobObject;
import io.github.anjoismysign.bloblib.entities.GenericManagerDirector;
import io.github.anjoismysign.bloblib.entities.ObjectDirector;
import io.github.anjoismysign.bloblib.managers.Manager;
import io.github.anjoismysign.blobpets.BlobPetsMiddleman;
import io.github.anjoismysign.blobpets.Found;
import io.github.anjoismysign.blobpets.NotFound;
import io.github.anjoismysign.blobpets.entity.petexpansion.PetExpansionDirector;
import io.github.anjoismysign.command.BlobTycoonCmd;
import io.github.anjoismysign.command.VisitCmd;
import io.github.anjoismysign.director.manager.ExpansionManager;
import io.github.anjoismysign.director.manager.PlotManager;
import io.github.anjoismysign.director.manager.StructureTracker;
import io.github.anjoismysign.director.manager.TycoonConfigManager;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.PlotProfileManager;
import io.github.anjoismysign.entity.TycoonPet;
import io.github.anjoismysign.entity.TycoonPlayerManager;
import io.github.anjoismysign.entity.asset.ObjectAsset;
import io.github.anjoismysign.entity.asset.RackAsset;
import io.github.anjoismysign.entity.asset.StructureAsset;
import io.github.anjoismysign.entity.mechanics.MechanicsData;
import io.github.anjoismysign.entity.plothelper.PlotHelperInventoryManager;
import io.github.anjoismysign.entity.structure.ObjectModel;
import io.github.anjoismysign.entity.structure.StorageModel;
import io.github.anjoismysign.entity.structure.StructureModel;
import io.github.anjoismysign.entity.structure.TycoonModel;
import io.github.anjoismysign.entity.structure.TycoonModelHolder;
import io.github.anjoismysign.entity.valuable.ValuableDirector;
import io.github.anjoismysign.entity.valuable.ValuableDriverManager;
import io.github.anjoismysign.event.AsyncBlobTycoonLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

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
        BlobPetsMiddleman blobPetsMiddleman = getBlobPetsMiddleman();
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