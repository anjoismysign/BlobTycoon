package us.mytheria.blobtycoon.blobpets;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class NotFound implements BlobPetsMiddleman {
    private static NotFound instance;

    private NotFound() {
    }

    public static BlobPetsMiddleman getInstance() {
        if (instance == null) {
            instance = new NotFound();
        }
        return instance;
    }

    @Override
    public boolean isReloading() {
        return false;
    }

    @Override
    public void reload() {

    }

    @Override
    public void whenDoneLoading(@NotNull Runnable runnable) {

    }

    @Override
    public void addExpansion(@NotNull File file) {

    }

    @Override
    public void setExpansionDirectory(@NotNull File file) {

    }
}
