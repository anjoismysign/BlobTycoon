package io.github.anjoismysign.blobtycoon.entity.writers;

import io.github.anjoismysign.blobtycoon.entity.structure.TycoonModelHolder;
import org.bukkit.configuration.file.YamlConfiguration;

public class TycoonHolderWriter {
    public static void WRITE(TycoonModelHolder<?> holder, YamlConfiguration configuration) {
        TycoonModelWriter.WRITE(holder.getModel(), configuration.getConfigurationSection("Structure-Model"));
        if (holder.getSellable() != null)
            SellableWriter.WRITE(holder.getSellable(), configuration.getConfigurationSection("Sellable"));
    }
}
