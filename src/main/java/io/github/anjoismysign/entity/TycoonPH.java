package io.github.anjoismysign.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.BlobPHExpansion;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableSnippet;
import io.github.anjoismysign.bloblib.utilities.TextColor;
import io.github.anjoismysign.BlobTycoon;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.entity.mechanics.MechanicsData;

import java.util.Objects;
import java.util.UUID;

public class TycoonPH {
    private final BlobTycoon plugin;
    @Nullable
    private BlobPHExpansion expansion;
    private boolean registeredPAPI;

    public TycoonPH(@NotNull BlobTycoon plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        reloadPAPI();
        reloadExpansion();
    }

    private void reloadPAPI() {
        if (registeredPAPI)
            throw new IllegalStateException("Already registered PlaceholderAPI expansion");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getAnjoLogger().log("PlaceholderAPI not found, not registering PlaceholderAPI expansion for " + plugin.getName());
            return;
        }
        registeredPAPI = true;
    }

    private void reloadExpansion() {
        if (expansion != null)
            return;
        if (!registeredPAPI)
            return;
        expansion = new BlobPHExpansion(plugin, "");
        expansion.putSimple("rebirths", offlinePlayer -> {
            TycoonPlayer tycoonPlayer = getTycoonPlayer(offlinePlayer.getUniqueId());
            if (tycoonPlayer == null) {
                return notOnline().get();
            }
            PlotProfile plotProfile = getPlotProfile(tycoonPlayer);
            if (plotProfile == null) {
                return notOnline().get();
            }
            return plotProfile.getRebirths() + "";
        });
        expansion.putStartsWith("valuableBalance", (offlinePlayer, key) -> {
            TycoonPlayer tycoonPlayer = getTycoonPlayer(offlinePlayer.getUniqueId());
            if (tycoonPlayer == null)
                return notOnline().get();
            PlotProfile plotProfile = getPlotProfile(tycoonPlayer);
            if (plotProfile == null)
                return notOnline().get();
            return plotProfile.parseValuableBalance(key);
        });
        expansion.putStartsWith("valuableBalanceNoFormat", (offlinePlayer, key) -> {
            TycoonPlayer tycoonPlayer = getTycoonPlayer(offlinePlayer.getUniqueId());
            if (tycoonPlayer == null)
                return notOnline().get();
            PlotProfile plotProfile = getPlotProfile(tycoonPlayer);
            if (plotProfile == null)
                return notOnline().get();
            return plotProfile.getValuable(key) + "";
        });
        expansion.putStartsWith("valuableEarner", (offlinePlayer, key) -> {
            TycoonPlayer tycoonPlayer = getTycoonPlayer(offlinePlayer.getUniqueId());
            if (tycoonPlayer == null)
                return notOnline().get();
            PlotProfile plotProfile = getPlotProfile(tycoonPlayer);
            if (plotProfile == null)
                return notOnline().get();
            return plotProfile.parseEarner(key);
        });
        expansion.putStartsWith("valuableEarnerNoFormat", (offlinePlayer, key) -> {
            TycoonPlayer tycoonPlayer = getTycoonPlayer(offlinePlayer.getUniqueId());
            if (tycoonPlayer == null)
                return notOnline().get();
            PlotProfile plotProfile = getPlotProfile(tycoonPlayer);
            if (plotProfile == null)
                return notOnline().get();
            return plotProfile.getTotalEarnings(key) + "";
        });
        expansion.putSimple("selectedPlot", offlinePlayer -> {
            TycoonPlayer tycoonPlayer = getTycoonPlayer(offlinePlayer.getUniqueId());
            if (tycoonPlayer == null) {
                return notOnline().get();
            }
            Player player = tycoonPlayer.getPlayer();
            PlotProfile plotProfile = getPlotProfile(tycoonPlayer);
            if (plotProfile == null) {
                return notOnline().get();
            }
            return getSnippet("BlobTycoon-Placeholder.Plot", player).get()
                    .replace("%n%", plotProfile.getSelectedExpansionIndex() + 1 + "");
        });
        expansion.putSimple("selectedPlotNoFormat", offlinePlayer -> {
            TycoonPlayer tycoonPlayer = getTycoonPlayer(offlinePlayer.getUniqueId());
            if (tycoonPlayer == null) {
                return notOnline().get();
            }
            PlotProfile plotProfile = getPlotProfile(tycoonPlayer);
            if (plotProfile == null) {
                return notOnline().get();
            }
            return plotProfile.getSelectedExpansionIndex() + 1 + "";
        });
        expansion.putStartsWith("production", (offlinePlayer, key) -> {
            TycoonPlayer tycoonPlayer = getTycoonPlayer(offlinePlayer.getUniqueId());
            if (tycoonPlayer == null) {
                return notOnline().get();
            }
            Player player = tycoonPlayer.getPlayer();
            PlotProfile plotProfile = getPlotProfile(tycoonPlayer);
            if (plotProfile == null) {
                return notOnline().get();
            }
            MechanicsData mechanicsData = BlobTycoonInternalAPI.getInstance()
                    .getMechanicsData(key);
            if (mechanicsData == null)
                return TextColor.PARSE("&cMechanics not found");
            if (!mechanicsData.isEnabled())
                return TextColor.PARSE("&cMechanics disabled");
            double amount = plotProfile.getProduction(mechanicsData);
            amount += mechanicsData.getDefaultAmount();
            return mechanicsData.display(amount, player);
        });
        expansion.putStartsWith("productionNoFormat", (offlinePlayer, key) -> {
            TycoonPlayer tycoonPlayer = getTycoonPlayer(offlinePlayer.getUniqueId());
            if (tycoonPlayer == null) {
                return notOnline().get();
            }
            PlotProfile plotProfile = getPlotProfile(tycoonPlayer);
            if (plotProfile == null) {
                return notOnline().get();
            }
            MechanicsData mechanicsData = BlobTycoonInternalAPI.getInstance()
                    .getMechanicsData(key);
            if (mechanicsData == null)
                return TextColor.PARSE("&cMechanics not found");
            if (!mechanicsData.isEnabled())
                return TextColor.PARSE("&cMechanics disabled");
            double amount = plotProfile.getProduction(mechanicsData);
            amount += mechanicsData.getDefaultAmount();
            return amount + "";
        });
        expansion.putStartsWith("consumption", (offlinePlayer, key) -> {
            TycoonPlayer tycoonPlayer = getTycoonPlayer(offlinePlayer.getUniqueId());
            if (tycoonPlayer == null) {
                return notOnline().get();
            }
            Player player = tycoonPlayer.getPlayer();
            PlotProfile plotProfile = getPlotProfile(tycoonPlayer);
            if (plotProfile == null) {
                return notOnline().get();
            }
            MechanicsData mechanicsData = BlobTycoonInternalAPI.getInstance()
                    .getMechanicsData(key);
            if (mechanicsData == null)
                return TextColor.PARSE("&cMechanics not found");
            if (!mechanicsData.isEnabled())
                return TextColor.PARSE("&cMechanics disabled");
            double amount = plotProfile.getConsumption(mechanicsData);
            amount += mechanicsData.getDefaultAmount();
            return mechanicsData.display(amount, player);
        });
        expansion.putStartsWith("consumptionNoFormat", (offlinePlayer, key) -> {
            TycoonPlayer tycoonPlayer = getTycoonPlayer(offlinePlayer.getUniqueId());
            if (tycoonPlayer == null) {
                return notOnline().get();
            }
            PlotProfile plotProfile = getPlotProfile(tycoonPlayer);
            if (plotProfile == null) {
                return notOnline().get();
            }
            MechanicsData mechanicsData = BlobTycoonInternalAPI.getInstance()
                    .getMechanicsData(key);
            if (mechanicsData == null)
                return TextColor.PARSE("&cMechanics not found");
            if (!mechanicsData.isEnabled())
                return TextColor.PARSE("&cMechanics disabled");
            double amount = plotProfile.getConsumption(mechanicsData);
            amount += mechanicsData.getDefaultAmount();
            return amount + "";
        });
    }

    @NotNull
    private TranslatableSnippet notOnline() {
        return getSnippet("BlobLib.Player-Not-Online");
    }

    @NotNull
    private TranslatableSnippet getSnippet(String key, Player player) {
        return Objects.requireNonNull(BlobLibTranslatableAPI.getInstance().getTranslatableSnippet(key, player));
    }

    @NotNull
    private TranslatableSnippet getSnippet(String key) {
        return Objects.requireNonNull(BlobLibTranslatableAPI.getInstance().getTranslatableSnippet(key));
    }

    @Nullable
    private TycoonPlayer getTycoonPlayer(@NotNull UUID uuid) {
        return BlobTycoonInternalAPI.getInstance()
                .getTycoonPlayer(uuid);
    }

    @Nullable
    private PlotProfile getPlotProfile(@NotNull TycoonPlayer tycoonPlayer) {
        if (tycoonPlayer == null)
            return null;
        PlotProprietorProfile plotProprietorProfile = tycoonPlayer.getProfile();
        if (plotProprietorProfile == null)
            return null;
        return plotProprietorProfile.getPlotProfile();
    }
}
