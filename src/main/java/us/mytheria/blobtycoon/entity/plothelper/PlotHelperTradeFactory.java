package us.mytheria.blobtycoon.entity.plothelper;

import me.anjoismysign.anjo.entities.Uber;
import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibEconomyAPI;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.bloblib.utilities.PlayerUtil;
import us.mytheria.bloblib.vault.multieconomy.ElasticEconomy;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.ValuableAccount;
import us.mytheria.blobtycoon.entity.configuration.PlotHelperConfiguration;
import us.mytheria.blobtycoon.entity.configuration.PlotHelperTradeSearchConfiguration;

import java.util.List;
import java.util.Objects;

public class PlotHelperTradeFactory {
    private static PlotHelperTradeFactory instance;
    private final BlobLibEconomyAPI economyAPI = BlobLibEconomyAPI.getInstance();

    public static PlotHelperTradeFactory getInstance() {
        if (instance == null) {
            instance = new PlotHelperTradeFactory();
        }
        return instance;
    }

    /**
     * Creates the trade and adds it to the plot helper container
     *
     * @param tradeData           The trade data
     * @param plotHelperContainer The plot helper container
     * @return The trade
     */
    @Nullable
    public PlotHelperTrade createTrade(@NotNull PlotHelperTradeData tradeData,
                                       @NotNull PlotHelperContainer plotHelperContainer) {
        Objects.requireNonNull(tradeData, "'tradeData' cannot be null");
        Objects.requireNonNull(plotHelperContainer, "'plotHelperContainer' cannot be null");
        return buy(tradeData, plotHelperContainer);
    }

    @Nullable
    private PlotHelperTrade buy(@NotNull PlotHelperTradeData tradeData,
                                @NotNull PlotHelperContainer plotHelperContainer) {
        TranslatableItem involvedItem = tradeData.toTranslatableItem();
        Integer order = plotHelperContainer.canAddTrade();
        if (order == null)
            return null;
        PlotHelper plotHelper = plotHelperContainer.plotHelper();
        Uber<Boolean> isForSale = Uber.drive(true);
        PlotHelperTrade trade = new PlotHelperTrade() {

            @NotNull
            public List<String> getOwners() {
                return plotHelperContainer.getOwners();
            }

            public void cancel(@NotNull Player player) {
                serialize().give(player);
                plotHelperContainer.removeTrade(order);
            }

            public boolean canProcess(@NotNull Player player) {
                Objects.requireNonNull(player, "'player' cannot be null");
                ElasticEconomy elasticEconomy = economyAPI.getElasticEconomy();
                IdentityEconomy economy = elasticEconomy.getImplementation(tradeData.getCurrency());
                return economy.has(player, tradeData.getPrice()) && isForSale();
            }

            public boolean isForSale() {
                return isForSale.thanks();
            }

            public void process(@Nullable Player player) {
                PlotHelperConfiguration configuration = PlotHelperConfiguration.getInstance();
                PlotHelperTradeSearchConfiguration tradeSearchConfiguration = configuration.getMerchantConfiguration().getTradeSearchConfiguration();
                String currency = tradeData.getCurrency();
                int amount = tradeData.getItemStack().getAmount();
                double price = tradeData.getPrice();
                plotHelperContainer.removeTrade(order);
                isForSale.talk(false);
                if (player != null) {
                    ItemStack clone = itemStack(player);
                    ElasticEconomy elasticEconomy = economyAPI.getElasticEconomy();
                    IdentityEconomy economy = elasticEconomy.getImplementation(currency);
                    economy.withdrawPlayer(player, price);
                    if (tradeSearchConfiguration.autoTeleportOnSuccessfulTransaction()) {
                        Location playerLocation = player.getLocation();
                        Location helperLocation = plotHelperContainer.getPlotHelper().getLocation();
                        if (!playerLocation.getWorld().getName().equals(helperLocation.getWorld().getName()))
                            player.teleport(helperLocation);
                        else if (playerLocation.distance(helperLocation) > 5.0)
                            player.teleport(helperLocation);
                    }
                    PlayerUtil.giveItemToInventoryOrDrop(player, clone);
                    BlobLibMessageAPI.getInstance().getMessage("BlobTycoon.Plot-Helper-Trade-Successful-Sell", player)
                            .modder()
                            .replace("%item%", display(player))
                            .replace("%amount%", amount + "")
                            .get()
                            .handle(player);
                }
                if (plotHelperContainer instanceof ValuableAccount valuableAccount)
                    valuableAccount.depositValuable(currency, price);
                if (tradeSearchConfiguration.notifyPlotOwners() &&
                        plotHelperContainer instanceof PlotProfile plotProfile) {
                    plotProfile.forEachOnlineProprietor(tycoonPlayer -> {
                        Player onlinePlayer = tycoonPlayer.getPlayer();
                        String display = display(onlinePlayer);
                        if (player != null)
                            BlobLibMessageAPI.getInstance().getMessage("BlobTycoon.Plot-Helper-Trade-Notify-Sell", player)
                                    .modder()
                                    .replace("%player%", player.getName())
                                    .replace("%item%", display)
                                    .replace("%amount%", amount + "")
                                    .get()
                                    .handle(onlinePlayer);
                        else
                            BlobLibMessageAPI.getInstance().getMessage("BlobTycoon.Plot-Helper-Trade-Notify-Server-Sell", player)
                                    .modder()
                                    .replace("%item%", display)
                                    .replace("%amount%", amount + "")
                                    .get()
                                    .handle(onlinePlayer);
                    });
                }
            }

            @NotNull
            public PlotHelperTradeData serialize() {
                return tradeData;
            }
        };
        plotHelperContainer.addTrade(trade, order);
        return trade;
    }
}
