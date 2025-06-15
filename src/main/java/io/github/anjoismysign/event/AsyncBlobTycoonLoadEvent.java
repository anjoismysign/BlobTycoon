package io.github.anjoismysign.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.entity.asset.ObjectAsset;
import io.github.anjoismysign.entity.asset.RackAsset;
import io.github.anjoismysign.entity.asset.StructureAsset;

import java.util.Collection;

public class AsyncBlobTycoonLoadEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Collection<Structure> structures;
    private final Collection<RackAsset> rackAssets;
    private final Collection<ObjectAsset> objectAssets;
    private final Collection<StructureAsset> structureAssets;
    private final boolean isFirstLoad;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public AsyncBlobTycoonLoadEvent(@NotNull Collection<Structure> structures,
                                    @NotNull Collection<RackAsset> rackAssets,
                                    @NotNull Collection<ObjectAsset> objectAssets,
                                    @NotNull Collection<StructureAsset> structureAssets,
                                    boolean isFirstLoad) {
        super(true);
        this.structures = structures;
        this.rackAssets = rackAssets;
        this.objectAssets = objectAssets;
        this.structureAssets = structureAssets;
        this.isFirstLoad = isFirstLoad;
    }

    /**
     * Get the structures that were tracked.
     *
     * @return The structures that were tracked.
     */
    public Collection<Structure> getStructures() {
        return structures;
    }

    /**
     * Get the rack assets that were tracked.
     *
     * @return The rack assets that were tracked.
     */
    public Collection<RackAsset> getRackAssets() {
        return rackAssets;
    }

    /**
     * Get the object assets that were tracked.
     *
     * @return The object assets that were tracked.
     */
    public Collection<ObjectAsset> getObjectAssets() {
        return objectAssets;
    }

    /**
     * Get the structure assets that were tracked.
     *
     * @return The structure assets that were tracked.
     */
    public Collection<StructureAsset> getStructureAssets() {
        return structureAssets;
    }

    /**
     * Check if this is the first load.
     *
     * @return {@code true} if this is the first load, {@code false} otherwise.
     */
    public boolean isFirstLoad() {
        return isFirstLoad;
    }
}
