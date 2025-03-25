package us.mytheria.blobtycoon.event;

import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.entity.structure.StructureModel;
import us.mytheria.blobtycoon.entity.structure.TycoonModelHolder;

public class StructureModelPlaceEvent extends TycoonModelHolderOperationEvent<StructureModel> {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public StructureModelPlaceEvent(@NotNull TycoonPlayer tycoonPlayer,
                                    @NotNull TycoonModelHolder<StructureModel> holder,
                                    @NotNull Location location) {
        super(tycoonPlayer, holder, location);
    }
}
