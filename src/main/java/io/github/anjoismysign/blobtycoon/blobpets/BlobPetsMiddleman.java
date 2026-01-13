package io.github.anjoismysign.blobtycoon.blobpets;

import io.github.anjoismysign.bloblib.entities.ObjectDirector;
import io.github.anjoismysign.blobtycoon.director.TycoonManagerDirector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface BlobPetsMiddleman {
    boolean isReloading();

    void reload();

    void whenDoneLoading(@NotNull Runnable runnable);

    void addExpansion(@NotNull File file);

    void setExpansionDirectory(@NotNull File file);

    @Nullable
    ObjectDirector<?> instantiateTycoonPetDirector(@NotNull TycoonManagerDirector director);

    boolean isEnabled();
}
