package us.mytheria.blobtycoon.event;

import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.entity.structure.ObjectModel;
import us.mytheria.blobtycoon.entity.structure.TycoonModelHolder;

public class ObjectModelPlaceEvent extends TycoonModelHolderOperationEvent<ObjectModel> {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public ObjectModelPlaceEvent(@NotNull TycoonPlayer tycoonPlayer,
                                 @NotNull TycoonModelHolder<ObjectModel> holder,
                                 @NotNull Location location) {
        super(tycoonPlayer, holder, location);
    }
}
