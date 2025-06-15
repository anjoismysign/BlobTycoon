package io.github.anjoismysign.util;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.entity.TycoonKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a 3D model of entities which all are inside
 * the bounding box of an interaction entity.
 *
 * @param getInteraction the interaction entity
 * @param getEntities    the entities that belong to the interaction entity
 */
public record InteractionModel(Interaction getInteraction, List<Entity> getEntities) {

    public static Interaction findInteraction(@NotNull Location location) {
        Objects.requireNonNull(location);
        World world = Objects.requireNonNull(location.getWorld());
        location = location.getBlock().getLocation();
        double add = 1 - Vector.getEpsilon();
        BoundingBox box = BoundingBox.of(location.toVector(),
                location.clone().add(new Vector(add, add, add)).toVector());
        for (Entity entity : location.getWorld().getNearbyEntities(location, 3, 3, 3)) {
            if (entity.getType() != EntityType.INTERACTION)
                continue;
            if (!entity.getBoundingBox().overlaps(box))
                continue;
            PersistentDataContainer container = entity.getPersistentDataContainer();
            if (container.has(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING))
                continue; // already a registered PlotObject
            return (Interaction) entity;
        }
        return null;
    }

    @Nullable
    public static InteractionModel of(@NotNull Interaction interaction) {
        Objects.requireNonNull(interaction);
        List<Entity> list = new ArrayList<>();
        Location location = interaction.getLocation().getBlock()
                .getLocation().add(0.5, 0.5, 0.5);
        BoundingBox box = interaction.getBoundingBox();
        for (Entity entity : location.getWorld().getNearbyEntities(location, 3, 3, 3)) {
            if (!entity.getBoundingBox().overlaps(box) &&
                    !box.contains(entity.getLocation().toVector()))
                continue;
            EntityType type = entity.getType();
            if (PlotDiscriminator.dontRemove(entity))
                continue;
            if (type == EntityType.INTERACTION)
                continue;
            list.add(entity);
        }
        return new InteractionModel(interaction, list);
    }
}
