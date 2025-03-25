package us.mytheria.blobtycoon.entity.blobpets;

import me.anjoismysign.blobpets.entity.petexpansion.PetExpansionDirector;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.entity.TycoonPet;

import java.util.Objects;

public class Found implements BlobPetsMiddleman {
    @Override
    public ObjectDirector<?> instantiateTycoonPetDirector(@NotNull TycoonManagerDirector director) {
        Objects.requireNonNull(director, "'director' cannot be null");
        return PetExpansionDirector.of(director, "TycoonPet", TycoonPet::fromFile);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
