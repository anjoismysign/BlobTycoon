package io.github.anjoismysign.entity;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.action.Action;

import java.util.List;

public record ActionHolderData(int getCooldown,
                               boolean isActionHolderEnabled,
                               @NotNull List<Action<Entity>> getActions) {
}
