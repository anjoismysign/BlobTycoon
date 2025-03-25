package us.mytheria.blobtycoon.entity.playerconfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobtycoon.entity.plothelper.CommodityMarketOfferType;

import java.util.Objects;

public class CreateOfferTypeConfiguration extends ConfigurationRecord<CommodityMarketOfferType> {
    private static CreateOfferTypeConfiguration instance;

    private CreateOfferTypeConfiguration() {
    }

    public static CreateOfferTypeConfiguration getInstance() {
        if (instance == null) {
            instance = new CreateOfferTypeConfiguration();
        }
        return instance;
    }

    @NotNull
    @Override
    public String getKey() {
        return "createOfferType";
    }

    @NotNull
    public CommodityMarketOfferType getOrDefault(
            @NotNull PlayerConfiguration configuration,
            @NotNull CommodityMarketOfferType defaultTradeType) {
        Objects.requireNonNull(configuration, "'configuration' cannot be null");
        Objects.requireNonNull(defaultTradeType, "'defaultTradeType' cannot be null");
        @Nullable String value = configuration.getConfiguration(getKey());
        if (value == null) {
            setConfiguration(configuration, defaultTradeType);
            return defaultTradeType;
        }
        CommodityMarketOfferType tradeType = CommodityMarketOfferType.getByName(value);
        if (tradeType == null) {
            setConfiguration(configuration, defaultTradeType);
            return defaultTradeType;
        }
        return tradeType;
    }

    public void setConfiguration(@NotNull PlayerConfiguration configuration,
                                 @NotNull CommodityMarketOfferType tradeType) {
        Objects.requireNonNull(configuration, "'configuration' cannot be null");
        Objects.requireNonNull(tradeType, "'tradeType' cannot be null");
        configuration.setConfiguration(getKey(), tradeType.name());
    }
}
