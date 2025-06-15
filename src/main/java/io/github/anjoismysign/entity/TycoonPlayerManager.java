package io.github.anjoismysign.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import io.github.anjoismysign.bloblib.entities.BlobSerializableManager;
import io.github.anjoismysign.director.TycoonManagerDirector;

import java.util.Optional;
import java.util.UUID;

public class TycoonPlayerManager extends BlobSerializableManager<TycoonPlayer> {

    public TycoonPlayerManager(TycoonManagerDirector director,
                               boolean logActivity) {
        super(director,
                x -> x,
                crudable -> new TycoonPlayer(crudable, director),
                "TycoonPlayer",
                logActivity,
                null,
                null);
    }

    @Override
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Optional<TycoonPlayer> optional = isBlobSerializable(uuid);
        if (optional.isEmpty())
            return;
        TycoonPlayer serializable = optional.get();
        PlotProprietorProfile profile = serializable.getProfile();
        if (profile != null) {
            PlotProfile plotProfile = profile.getPlotProfile();
            plotProfile.close(player, null);
        }
        addSaving(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(getManagerDirector().getPlugin(), () -> {
            crudManager.update(serializable.serializeAllAttributes());
            removeObject(uuid);
            removeSaving(uuid);
        });
    }
}
