package io.github.anjoismysign.event;

import org.bukkit.event.HandlerList;
import io.github.anjoismysign.entity.TycoonPlayer;

/**
 * Called when a TycoonPlayer is done loading, having loaded their
 * PlotProprietorProfile and being already teleported to the plot's home.
 */
public class ProfileLoadEvent extends TycoonPlayerEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public ProfileLoadEvent(TycoonPlayer tycoonPlayer) {
        super(tycoonPlayer);
    }
}
