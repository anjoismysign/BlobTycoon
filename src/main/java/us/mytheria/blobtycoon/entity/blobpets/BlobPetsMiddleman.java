package us.mytheria.blobtycoon.entity.blobpets;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;

public interface BlobPetsMiddleman {
    static BlobPetsMiddleman get() {
        if (Bukkit.getPluginManager().getPlugin("BlobPets") == null)
            return new NotFound();
        return new Found();
    }

    @Nullable
    ObjectDirector<?> instantiateTycoonPetDirector(@NotNull TycoonManagerDirector director);

    boolean isEnabled();
}
