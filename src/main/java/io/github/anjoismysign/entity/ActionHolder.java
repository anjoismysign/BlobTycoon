package io.github.anjoismysign.entity;

import io.github.anjoismysign.anjo.entities.Uber;
import io.github.anjoismysign.bloblib.action.Action;
import io.github.anjoismysign.bloblib.api.BlobLibActionAPI;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface ActionHolder {
    @NotNull
    static ActionHolderData READ(ConfigurationSection section) {
        if (!section.isConfigurationSection("ActionHolder"))
            return new ActionHolderData(0, false, new ArrayList<>());
        ConfigurationSection actionHolderSection = section.getConfigurationSection("ActionHolder");
        if (!actionHolderSection.isList("Actions"))
            throw new ConfigurationFieldException("'Actions' field is not valid or set");
        List<String> actionKeys = actionHolderSection.getStringList("Actions");
        List<Action<Entity>> actions = actionKeys.stream()
                .map(key -> BlobLibActionAPI.getInstance().getAction(key))
                .filter(Objects::nonNull)
                .toList();
        int cooldown = actionHolderSection.getInt("Cooldown", 0);
        if (cooldown < 0)
            cooldown = 0;
        return new ActionHolderData(cooldown, true, actions);
    }

    Uber<Boolean> isOnCooldown();

    /**
     * Checks if the player can process the action
     *
     * @return True if the player can process the action, false otherwise
     */
    default boolean canProcess() {
        if (getCooldown() == 0)
            return true;
        return !isOnCooldown().thanks();
    }

    /**
     * Processes the action
     *
     * @param player The player that is processing the action
     */
    default void process(@NotNull Player player) {
        Objects.requireNonNull(player, "'player' cannot be null");
        getActions().forEach(action -> action.perform(player));
        isOnCooldown().talk(true);
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("BlobTycoon"), () -> {
            isOnCooldown().talk(false);
        }, getCooldown() * 20L);
    }

    int getCooldown();

    boolean isActionHolderEnabled();

    List<Action<Entity>> getActions();
}
