package io.github.anjoismysign.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.ComplexEventListener;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.PlotProprietorProfile;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.mechanics.Mechanics;
import io.github.anjoismysign.event.ProfileLoadEvent;
import io.github.anjoismysign.util.TycoonUnit;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfflineEarningListener extends BlobTycoonListener {
    private long maxElapsed;
    private double earning;

    public OfflineEarningListener(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @Override
    public void reload() {
        this.unregister();
        if (this.checkIfShouldRegister()) {
            ComplexEventListener listener = getConfigManager().getOfflineEarning();
            this.maxElapsed = listener.getLong("Max-Elapsed");
            this.earning = listener.getDouble("Earning");
            register();
        }
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager().getOfflineEarning().register();
    }

    @EventHandler
    public void onLoad(ProfileLoadEvent event) {
        TycoonPlayer tycoonPlayer = event.getTycoonPlayer();
        Player player = tycoonPlayer.getPlayer();
        PlotProprietorProfile proprietorProfile = tycoonPlayer.getProfile();
        PlotProfile plotProfile = proprietorProfile.getPlotProfile();
        if (plotProfile.getNumberOfOnlinePlayers() > 1)
            throw new IllegalStateException("Offline earning should only be calculated when there is only one player online");
        OfflineEarningResult result = earnOffline(plotProfile);
        switch (result.getStatus()) {
            case NOT_ELAPSED -> {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon-Offline-Earning.Not-Elapsed", player)
                        .handle(player);
            }
            case FALLING_SHORT -> {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon-Offline-Earning.Falling-Short", player)
                        .handle(player);
            }
            case EARNED -> {
                Map<String, Double> earned = new HashMap<>();
                plotProfile.getValuables().forEach((key, balance) -> {
                    double total = plotProfile.getTotalEarnings(key);
                    total *= result.getElapsed();
                    total *= earning;
                    if (total >= 0.000001)
                        earned.put(key, total);
                });
                if (earned.isEmpty())
                    return;
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon-Offline-Earning.Earned", player)
                        .handle(player);
                earned.forEach((key, value) -> {
                    BlobLibMessageAPI.getInstance()
                            .getMessage("BlobTycoon-Offline-Earning.Earned-Details", player)
                            .modder()
                            .replace("%display%", TycoonUnit.THOUSANDS_SEPARATOR.format(value))
                            .replace("%valuable%", key)
                            .get()
                            .handle(player);
                    plotProfile.depositValuable(key, value);
                });
            }
            default -> {
                throw new IllegalStateException("Unexpected value: " + result.getStatus());
            }
        }
    }

    public OfflineEarningResult earnOffline(@NotNull PlotProfile plotProfile) {
        Instant instant = Instant.now();
        long current = instant.toEpochMilli();
        long lastConnection = plotProfile.getLastConnection();
        long elapsed = (current - lastConnection) / 1000;
        if (elapsed < 300)
            // check if getElapsed time is less than 5 minutes
            return OfflineEarningResult.notElapsed();
        List<Mechanics> list = plotProfile.getMechanics().values()
                .stream()
                .filter(Mechanics::isFallingShort)
                .toList();
        if (!list.isEmpty())
            // check if any mechanics are falling short
            return OfflineEarningResult.fallingShort();
        if (maxElapsed > 0 && elapsed > maxElapsed)
            // if elapsed time is greater than maxElapsed, trim it to maxElapsed
            elapsed = maxElapsed;
        return OfflineEarningResult.elapsed(elapsed);
    }

    public enum OfflineEarningStatus {
        NOT_ELAPSED,
        FALLING_SHORT,
        EARNED
    }

    public record OfflineEarningResult(@NotNull OfflineEarningListener.OfflineEarningStatus getStatus,
                                       long getElapsed) {
        public static OfflineEarningResult notElapsed() {
            return new OfflineEarningResult(OfflineEarningStatus.NOT_ELAPSED, 0);
        }

        public static OfflineEarningResult fallingShort() {
            return new OfflineEarningResult(OfflineEarningStatus.FALLING_SHORT, 0);
        }

        public static OfflineEarningResult elapsed(long elapsed) {
            return new OfflineEarningResult(OfflineEarningStatus.EARNED, elapsed);
        }

        public boolean isEarning() {
            return getStatus == OfflineEarningStatus.EARNED;
        }
    }
}
