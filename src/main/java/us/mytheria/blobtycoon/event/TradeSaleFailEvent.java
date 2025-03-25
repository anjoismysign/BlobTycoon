package us.mytheria.blobtycoon.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.entity.plothelper.PlotHelperTrade;

/**
 * Called when a TycoonPlayer is done loading, having loaded their
 * PlotProprietorProfile and being already teleported to the plot's home.
 */
public class TradeSaleFailEvent extends TycoonPlayerEvent {
    @NotNull
    private final PlotHelperTrade trade;
    private boolean fix;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public TradeSaleFailEvent(
            @NotNull TycoonPlayer tycoonPlayer,
            @NotNull PlotHelperTrade trade) {
        super(tycoonPlayer);
        this.trade = trade;
        this.fix = false;
    }

    /**
     * The trade involved
     *
     * @return The trade
     */
    public @NotNull PlotHelperTrade getTrade() {
        return trade;
    }

    /**
     * The currency with which the sale was attempted.
     *
     * @return The currency. Null if default currency.
     */
    public @Nullable String getCurrency() {
        return trade.serialize().getCurrency();
    }

    /**
     * The amount of currency that was attempted to be sold.
     *
     * @return The amount of currency.
     */
    public double getAmount() {
        return trade.serialize().getPrice();
    }

    /**
     * Whether this sale is marked as fixed.
     * If sale is fixed, it means that BlobTycoon will attempt to re-try the trade one last time.
     *
     * @return true if the sale is fixed, false otherwise.
     */
    public boolean isFixed() {
        return fix;
    }

    /**
     * Mark this sale as fixed.
     *
     * @param fixed true if the sale is fixed, false otherwise.
     */
    public void setFixed(boolean fixed) {
        this.fix = fixed;
    }
}
