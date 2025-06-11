package us.mytheria.blobtycoon.blobpets;

import me.anjoismysign.blobpets.entity.petexpansion.PetExpansionDirector;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.entity.TycoonPet;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

public class Found implements BlobPetsMiddleman {
    private static Found instance;
    private final TycoonManagerDirector director;

    public static BlobPetsMiddleman getInstance(TycoonManagerDirector director) {
        if (instance == null) {
            instance = new Found(director);
        }
        return instance;
    }

    private Found(TycoonManagerDirector director) {
        this.director = director;
    }

    @Override
    public boolean isReloading() {
        Optional<Object> optional = director.getTycoonPetDirector();
        if (optional.isEmpty())
            return false;
        PetExpansionDirector<TycoonPet> get = (PetExpansionDirector<TycoonPet>) optional.get();
        return (get.isReloading());
    }

    @Override
    public void reload() {
        Optional<Object> optional = director.getTycoonPetDirector();
        if (optional.isEmpty())
            return;
        PetExpansionDirector<TycoonPet> get = (PetExpansionDirector<TycoonPet>) optional.get();
        boolean tinyDebug = director.getConfigManager().tinyDebug();
        if (tinyDebug)
            director.getPlugin().getLogger().warning("Reloading TycoonPetDirector");
        get.reload();
    }

    @Override
    public void whenDoneLoading(@NotNull Runnable runnable) {
        Objects.requireNonNull(runnable, "'runnable' cannot be null");
        Optional<Object> optional = director.getTycoonPetDirector();
        if (optional.isEmpty())
            return;
        PetExpansionDirector<TycoonPet> get = (PetExpansionDirector<TycoonPet>) optional.get();
        get.whenReloaded(runnable);
    }

    @Override
    public void addExpansion(@NotNull File file) {
        Objects.requireNonNull(file, "'file' cannot be null");
        Optional<Object> optional = director.getTycoonPetDirector();
        if (optional.isEmpty())
            return;
        PetExpansionDirector<TycoonPet> get = (PetExpansionDirector<TycoonPet>) optional.get();
        get.addExpansion(file);
    }

    @Override
    public void setExpansionDirectory(@NotNull File file) {
        Optional<Object> optional = director.getTycoonPetDirector();
        if (optional.isEmpty())
            return;
        PetExpansionDirector<TycoonPet> get = (PetExpansionDirector<TycoonPet>) optional.get();
        get.setExpansionDirectory(file);
    }

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
