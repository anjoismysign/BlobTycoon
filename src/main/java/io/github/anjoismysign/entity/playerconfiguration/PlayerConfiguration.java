package io.github.anjoismysign.entity.playerconfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PlayerConfiguration {
    void setConfiguration(@NotNull String key, @NotNull String value);

    @Nullable
    String getConfiguration(@NotNull String key);
}
