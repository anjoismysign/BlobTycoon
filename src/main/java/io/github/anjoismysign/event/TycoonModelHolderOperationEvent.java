package io.github.anjoismysign.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.structure.TycoonModel;
import io.github.anjoismysign.entity.structure.TycoonModelHolder;

/**
 * Represents either placement or removal of a TycoonModel's structure
 *
 * @param <T> the type of TycoonModel
 */
public abstract class TycoonModelHolderOperationEvent<T extends TycoonModel>
        extends TycoonModelHolderEvent<T> implements Cancellable {
    private final Location location;
    private boolean cancelled;

    public TycoonModelHolderOperationEvent(TycoonPlayer tycoonPlayer,
                                           TycoonModelHolder<T> holder,
                                           Location location) {
        super(tycoonPlayer, holder);
        this.location = location;
    }

    /**
     * Where the structure is being placed or removed
     *
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * If it's cancelled
     *
     * @return if it's cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Set if it's cancelled
     *
     * @param cancelled true if you wish to cancel this event
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
