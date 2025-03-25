package us.mytheria.blobtycoon.entity.plothelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum CommodityMarketOfferType {
    SELL, // Player is selling TranslatableItem to PlotHelper
    BUY; // Player is buying TranslatableItem to PlotHelper

    private static final Map<String, CommodityMarketOfferType> byName = new HashMap<>();

    static {
        for (CommodityMarketOfferType tradeType : values()) {
            byName.put(tradeType.name(), tradeType);
        }
    }

    @Nullable
    public static CommodityMarketOfferType getByName(@NotNull String name) {
        Objects.requireNonNull(name, "'name' cannot be null");
        return byName.get(name);
    }
}
