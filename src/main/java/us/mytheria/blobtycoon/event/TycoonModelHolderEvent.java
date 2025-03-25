package us.mytheria.blobtycoon.event;

import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.entity.structure.TycoonModel;
import us.mytheria.blobtycoon.entity.structure.TycoonModelHolder;

public abstract class TycoonModelHolderEvent<T extends TycoonModel>
        extends TycoonPlayerEvent {

    private final TycoonModelHolder<T> holder;

    public TycoonModelHolderEvent(TycoonPlayer tycoonPlayer,
                                  TycoonModelHolder<T> holder) {
        super(tycoonPlayer);
        this.holder = holder;
    }

    /**
     * The holder of the structure
     *
     * @return the holder
     */
    public TycoonModelHolder<T> getHolder() {
        return holder;
    }
}
