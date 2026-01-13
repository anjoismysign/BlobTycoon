package io.github.anjoismysign.blobtycoon.entity;

import io.github.anjoismysign.bloblib.action.Action;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ActionHolderData(int getCooldown,
                               boolean isActionHolderEnabled,
                               @NotNull List<Action<Entity>> getActions) {
}
