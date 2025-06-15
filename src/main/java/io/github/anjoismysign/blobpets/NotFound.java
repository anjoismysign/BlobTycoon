package io.github.anjoismysign.blobpets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.entities.ObjectDirector;
import io.github.anjoismysign.director.TycoonManagerDirector;

import java.io.File;

public class NotFound implements BlobPetsMiddleman {
    private static NotFound instance;

    public static BlobPetsMiddleman getInstance() {
        if (instance == null) {
            instance = new NotFound();
        }
        return instance;
    }

    private NotFound() {
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

    @Override
    public @Nullable ObjectDirector<?> instantiateTycoonPetDirector(@NotNull TycoonManagerDirector director) {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
