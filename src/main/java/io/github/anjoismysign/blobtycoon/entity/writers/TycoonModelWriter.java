package io.github.anjoismysign.blobtycoon.entity.writers;

import io.github.anjoismysign.blobtycoon.entity.StructureData;
import io.github.anjoismysign.blobtycoon.entity.structure.StructureModel;
import io.github.anjoismysign.blobtycoon.entity.structure.TycoonModel;
import org.bukkit.configuration.ConfigurationSection;

public class TycoonModelWriter {
    public static void WRITE(TycoonModel model, ConfigurationSection section) {
        section.set("Structure", model.getStructurePath());
        section.set("Place-Sound", model.getPlaceSoundKey());
        section.set("Remove-Sound", model.getRemoveSoundKey());
        ConfigurationSection dataSection = section.createSection("Structure-Data");
        if (model instanceof StructureModel structureModel)
            StructureData.WRITE(structureModel.getStructureData(), dataSection);
    }
}
