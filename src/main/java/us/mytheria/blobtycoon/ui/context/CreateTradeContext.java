package us.mytheria.blobtycoon.ui.context;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.bloblib.utilities.PlayerUtil;
import us.mytheria.blobtycoon.entity.plothelper.PlotHelperTradeData;

import java.util.Objects;

public class CreateTradeContext {
    @NotNull
    private final String defaultCurrency;
    private final double defaultAmount;

    @NotNull
    private String currency;
    private double amount;
    @Nullable
    private ItemStack tradingItem;

    private CreateTradeContext(@NotNull String currency,
                               double amount) {
        this.defaultCurrency = currency;
        this.currency = currency;
        this.defaultAmount = amount;
        this.amount = amount;
    }

    public static CreateTradeContext of(@NotNull String currency,
                                        double amount) {
        Objects.requireNonNull(currency, "'currency' cannot be null");
        if (amount <= 0)
            throw new IllegalArgumentException("'amount' must be greater than 0");
        return new CreateTradeContext(currency, amount);
    }

    @NotNull
    public String getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setCurrency(@NotNull String currency) {
        Objects.requireNonNull(currency, "'currency' cannot be null");
        this.currency = currency;
    }

    public void setAmount(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("'amount' must be greater than 0");
        this.amount = amount;
    }

    @Nullable
    public TranslatableItem getTradingTranslatableItem() {
        return TranslatableItem.byItemStack(getTradingItem());
    }

    public void cancel(@NotNull Player owner) {
        if (tradingItem == null)
            return;
        TranslatableItem item = getTradingTranslatableItem();
        if (item == null)
            PlayerUtil.giveItemToInventoryOrDrop(owner, tradingItem);
        else
            PlayerUtil.giveItemToInventoryOrDrop(owner, item.localize(owner).getClone());
        tradingItem = null;
    }

    @Nullable
    public ItemStack getTradingItem() {
        return tradingItem;
    }

    public void setTradingItem(@Nullable ItemStack tradingItem) {
        this.tradingItem = tradingItem;
    }

    public boolean isReady() {
        return tradingItem != null && currency != null && Double.compare(amount, 0) > 0;
    }

    public void clear() {
        tradingItem = null;
        currency = defaultCurrency;
        amount = defaultAmount;
    }

    @NotNull
    public PlotHelperTradeData toTradeData() {
        if (!isReady())
            throw new IllegalStateException("Trade data is not ready");
        return new PlotHelperTradeData(tradingItem, amount, currency);
    }
}
