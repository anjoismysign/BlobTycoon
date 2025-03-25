package us.mytheria.blobtycoon.blobpets;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface BlobPetsMiddleman {
    boolean isReloading();

    void reload();

    void whenDoneLoading(@NotNull Runnable runnable);

    void addExpansion(@NotNull File file);

    void setExpansionDirectory(@NotNull File file);
}
