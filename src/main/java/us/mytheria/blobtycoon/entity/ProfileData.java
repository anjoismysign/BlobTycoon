package us.mytheria.blobtycoon.entity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ProfileData(@NotNull String getId,
                          @NotNull String getName,
                          int getIndex) {

    public static ProfileData of(@NotNull String id,
                                 @NotNull String name,
                                 int index) {
        Objects.requireNonNull(id, "'id' cannot be null");
        Objects.requireNonNull(name, "'name' cannot be null");
        return new ProfileData(id, name, index);
    }
}
