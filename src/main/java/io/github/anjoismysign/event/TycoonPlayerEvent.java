package io.github.anjoismysign.event;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.entity.TycoonPlayer;

public abstract class TycoonPlayerEvent extends Event {
    @NotNull
    private final TycoonPlayer tycoonPlayer;

    public TycoonPlayerEvent(TycoonPlayer tycoonPlayer) {
        this.tycoonPlayer = tycoonPlayer;
    }

    /**
     * The player involved in this event
     *
     * @return the player
     */
    @NotNull
    public TycoonPlayer getTycoonPlayer() {
        return tycoonPlayer;
    }
}
