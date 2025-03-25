package us.mytheria.blobtycoon.entity.structure;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibSoundAPI;
import us.mytheria.bloblib.entities.message.BlobSound;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.blobtycoon.entity.Descriptor;

import java.util.function.Consumer;

public interface TycoonModel extends Descriptor {
    TranslatableItem getTranslatableItem();

    @NotNull
    JavaPlugin getPlugin();

    @NotNull
    Structure getStructure();

    @NotNull
    String getStructurePath();

    @Nullable
    String getPlaceSoundKey();

    @Nullable
    default BlobSound getPlaceSound() {
        return getPlaceSoundKey() == null ? null : BlobLibSoundAPI.getInstance().getSound(getPlaceSoundKey());
    }

    @Nullable
    String getRemoveSoundKey();

    @Nullable
    default BlobSound getRemoveSound() {
        return getRemoveSoundKey() == null ? null : BlobLibSoundAPI.getInstance().getSound(getRemoveSoundKey());
    }

    @NotNull
    Consumer<Player> getWhenPlaced();

    @NotNull
    Consumer<Player> getWhenRemoved();
}
