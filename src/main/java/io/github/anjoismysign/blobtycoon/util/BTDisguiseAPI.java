package io.github.anjoismysign.blobtycoon.util;

import io.github.anjoismysign.blobtycoon.BlobTycoon;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public enum BTDisguiseAPI {
    INSTANCE;

    private static final NamespacedKey NAMESPACED_KEY =
            new NamespacedKey(BlobTycoon.getInstance(), "disguise_api");

    public boolean has(@NotNull PersistentDataContainer container){
        Objects.requireNonNull(container, "'container' cannot be null");
        return container.has(NAMESPACED_KEY);
    }

    @Nullable
    public String raw(@NotNull PersistentDataContainer container){
        Objects.requireNonNull(container, "'container' cannot be null");
        return container.get(NAMESPACED_KEY, PersistentDataType.STRING);
    }

    public void set(@NotNull PersistentDataContainer container,
                    @NotNull String raw){
        Objects.requireNonNull(container, "'container' cannot be null");
        Objects.requireNonNull(raw, "'raw' cannot be null");
        container.set(NAMESPACED_KEY, PersistentDataType.STRING, raw);
    }

}
