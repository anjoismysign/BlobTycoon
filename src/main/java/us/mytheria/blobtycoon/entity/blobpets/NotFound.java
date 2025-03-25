package us.mytheria.blobtycoon.entity.blobpets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;

public class NotFound implements BlobPetsMiddleman {
    @Override
    public @Nullable ObjectDirector<?> instantiateTycoonPetDirector(@NotNull TycoonManagerDirector director) {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
