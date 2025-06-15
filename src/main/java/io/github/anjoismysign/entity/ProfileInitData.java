package io.github.anjoismysign.entity;

import io.github.anjoismysign.anjo.entities.Tuple2;
import io.github.anjoismysign.bloblib.entities.BlobCrudable;
import io.github.anjoismysign.entity.configuration.HologramConfiguration;
import org.bson.Document;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ProfileInitData(BlobCrudable crudable, Plot plot) {

    /**
     * Will reset the profile and unlink all players
     *
     * @param profile the profile to reset
     * @return the new ProfileInitData
     */
    public static Tuple2<ProfileInitData, List<TycoonPlayer>> reset(
            @NotNull PlotProfile profile,
            boolean rebirth) {
        Objects.requireNonNull(profile);
        HologramConfiguration.getInstance().deleteHologram(profile.getPlot().getData());
        Entity plotHelper = profile.getPlotHelper();
        if (plotHelper != null)
            plotHelper.remove();
        List<TycoonPlayer> unlinked = new ArrayList<>();
        profile.forEachOnlineProprietor(tycoonPlayer -> {
            Player player = tycoonPlayer.getPlayer();
            Objects.requireNonNull(player);
            player.getInventory().clear();
            tycoonPlayer.unlinkProfile();
            unlinked.add(tycoonPlayer);
        });
        profile.forEachProprietor(uuid -> profile.reset(uuid, rebirth));
        profile.resetPlotHelper(rebirth);
        profile.resetProgress();
        BlobCrudable crudable = new BlobCrudable(profile.blobCrudable().getIdentification());
        Document previous = profile.blobCrudable().getDocument();
        Document current = crudable.getDocument();
        current.put("Proprietors", previous.get("Proprietors"));
        if (rebirth)
            current.put("Rebirths", profile.getRebirths() + 1);
        return new Tuple2<>(new ProfileInitData(crudable,
                profile.getPlot()), unlinked);
    }
}
