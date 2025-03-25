package us.mytheria.blobtycoon;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.api.BlobLibTranslatableAPI;
import us.mytheria.bloblib.entities.translatable.TranslatableBlock;
import us.mytheria.bloblib.itemstack.ItemStackModder;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.entity.plothelper.PlotHelperContainer;

import java.util.ArrayList;
import java.util.List;

public class BlobTycoonAPI {
    private static BlobTycoonAPI instance;
    private final TycoonManagerDirector director;
    private final BlobTycoonValuableAPI valuableAPI;

    protected static BlobTycoonAPI getInstance(TycoonManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            BlobTycoonAPI.instance = new BlobTycoonAPI(director);
        }
        return instance;
    }

    public static BlobTycoonAPI getInstance() {
        return getInstance(null);
    }

    private BlobTycoonAPI(TycoonManagerDirector director) {
        this.director = director;
        this.valuableAPI = BlobTycoonValuableAPI.getInstance(director);
    }

    public BlobTycoonValuableAPI getValuableAPI() {
        return valuableAPI;
    }

    public BlobTycoon getPlugin() {
        return director.getPlugin();
    }

    /**
     * Opens a Community Trade to a specific player
     *
     * @param player         The player to open the Community Trade
     * @param communityTrade The container to get the Community Trade
     * @return true if successful, false otherwise
     */
    public boolean openCommunityTrade(@NotNull Player player,
                                      @NotNull PlotHelperContainer communityTrade) {
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null || tycoonPlayer.getProfile() == null)
            return false;
        PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
        BlobLibInventoryAPI.getInstance().customSelector(
                "Community-Trades",
                player,
                "Trades",
                "Trade",
                () -> communityTrade.getTrades().values().stream().toList(),
                trade -> {
                    tycoonPlayer.setCommunityTrade(communityTrade);
                    profile.openTradeUI(player, trade, false);
                },
                trade -> {
                    ItemStack itemStack = trade.itemStack(player);
                    List<String> lore = new ArrayList<>();
                    if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore())
                        lore.addAll(itemStack.getItemMeta().getLore());
                    TranslatableBlock block = BlobLibTranslatableAPI.getInstance()
                            .getTranslatableBlock("BlobTycoon-PlotHelper.Trade-View", player);
                    lore.addAll(block.get());
                    String format;
                    ItemStackModder.mod(itemStack)
                            .lore(lore)
                            .replace("%sellers%", String.join(", ", trade.getOwners()))
                            .replace("%price%", trade.formatPrice());
                    return itemStack;
                });
        return true;
    }
}
