package io.github.anjoismysign.event;

import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.structure.TycoonModel;
import io.github.anjoismysign.entity.structure.TycoonModelHolder;

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
