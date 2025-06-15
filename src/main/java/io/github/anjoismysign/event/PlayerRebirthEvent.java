package io.github.anjoismysign.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import io.github.anjoismysign.entity.TycoonPlayer;

/**
 * Called on each online TycoonPlayer on a PlotProfile that rebirths.
 */
public class PlayerRebirthEvent extends TycoonPlayerEvent implements Cancellable {
    private boolean cancel;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public PlayerRebirthEvent(TycoonPlayer tycoonPlayer) {
        super(tycoonPlayer);
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
