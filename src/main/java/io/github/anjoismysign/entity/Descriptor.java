package io.github.anjoismysign.entity;

import org.jetbrains.annotations.NotNull;

public interface Descriptor {
    @NotNull
    String getType();

    @NotNull
    String getKey();
}
