package us.mytheria.blobtycoon.entity;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibMessageAPI;

import java.util.List;
import java.util.Objects;

/**
 * Holds a Plot.
 * Implementation needs to do logic about preventing visitors or not
 * through #canVisit
 */
public interface PlotContainer {
    /**
     * Retrieves the plot associated with this container.
     *
     * @return The plot associated with this container.
     */
    @NotNull
    Plot getPlot();

    /**
     * Whether a player can visit this plot.
     *
     * @param visitor The player that's visitor
     * @return True if allowed, false otherwise
     */
    boolean canVisit(@NotNull Player visitor);

    /**
     * Gets the owners
     *
     * @return the owners
     */
    @NotNull
    List<String> getOwners();

    /**
     * Makes a player visit this plot
     *
     * @param visitor The player that's visitor
     */
    default void visit(@NotNull Player visitor) {
        Objects.requireNonNull(visitor, "'visit' cannot be null");
        visitor.closeInventory();
        if (!canVisit(visitor))
            return;
        visitor.teleport(getPlot().getData().getHomeLocation());
        List<String> owners = getOwners();
        BlobLibMessageAPI.getInstance().getMessage("BlobTycoon.Visiting", visitor)
                .modder()
                .replace("%player%", String.join(", ", owners))
                .get()
                .handle(visitor);
        if (this instanceof PlotProfile plotProfile) {
            plotProfile.forEachOnlineProprietor(tycoonPlayer -> {
                Player player = tycoonPlayer.getPlayer();
                BlobLibMessageAPI.getInstance().getMessage("BlobTycoon.Visit", player)
                        .modder()
                        .replace("%player%", visitor.getName())
                        .get()
                        .handle(player);
            });
        }
    }
}
