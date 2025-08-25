package io.github.anjoismysign.blobtycoon.entity;

import org.jetbrains.annotations.NotNull;

public interface Overclock {

    boolean isActive();

    @NotNull String getMechanics();

    double getMechanicsMultiplier();

    double getValuableEarnerMultiplier();

}
