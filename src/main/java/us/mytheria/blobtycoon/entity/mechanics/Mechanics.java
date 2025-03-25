package us.mytheria.blobtycoon.entity.mechanics;

import org.jetbrains.annotations.NotNull;

public interface Mechanics {

    @NotNull
    MechanicsOperator getOperator();

    boolean isFallingShort();

    @NotNull
    String getShortening();

    @NotNull
    String getKey();
}
