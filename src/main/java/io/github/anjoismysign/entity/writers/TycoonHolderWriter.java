package io.github.anjoismysign.entity.writers;

import org.bukkit.configuration.file.YamlConfiguration;
import io.github.anjoismysign.entity.structure.TycoonModelHolder;

public class TycoonHolderWriter {
    public static void WRITE(TycoonModelHolder<?> holder, YamlConfiguration configuration) {
        TycoonModelWriter.WRITE(holder.getModel(), configuration.getConfigurationSection("Structure-Model"));
        if (holder.getSellable() != null)
            SellableWriter.WRITE(holder.getSellable(), configuration.getConfigurationSection("Sellable"));
    }
}
