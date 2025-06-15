package io.github.anjoismysign.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public interface Host {
    Set<UUID> getInvited();

    /**
     * Invite a player to the host's party.
     * The player will have a certain amount of time to accept the invite.
     *
     * @param player  The player to invite
     * @param seconds The amount of time the player has to accept the invite
     */
    default void invite(@NotNull Player player,
                        int seconds) {
        Objects.requireNonNull(player, "'player' cannot be null");
        getInvited().add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("BlobTycoon"), () -> {
            getInvited().remove(player.getUniqueId());
        }, seconds * 20L);
    }

    /**
     * Check if a player is invited to the host's party.
     *
     * @param player The player to check
     * @return true if the player is invited, false otherwise
     */
    default boolean isInvited(@NotNull Player player) {
        Objects.requireNonNull(player, "'player' cannot be null");
        return getInvited().contains(player.getUniqueId());
    }
}
