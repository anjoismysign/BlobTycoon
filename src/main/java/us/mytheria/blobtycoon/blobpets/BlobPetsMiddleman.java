package us.mytheria.blobtycoon.blobpets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;

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
