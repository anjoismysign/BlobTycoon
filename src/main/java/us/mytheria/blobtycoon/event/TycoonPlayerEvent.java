package us.mytheria.blobtycoon.event;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobtycoon.entity.TycoonPlayer;

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
