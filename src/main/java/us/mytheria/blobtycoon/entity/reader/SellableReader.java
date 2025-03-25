package us.mytheria.blobtycoon.entity.reader;

import org.bukkit.configuration.ConfigurationSection;
import us.mytheria.bloblib.exception.ConfigurationFieldException;
import us.mytheria.blobtycoon.entity.Sellable;

public class SellableReader {
    public static Sellable READ(ConfigurationSection configuration) {
        if (!configuration.isString("Buying-Currency"))
            throw new ConfigurationFieldException("'Buying-Currency' is not valid or set");
        if (!configuration.isDouble("Buying-Price"))
            throw new ConfigurationFieldException("'Buying-Price' is not valid or set");
        String buyingCurrency = configuration.getString("Buying-Currency");
        String sellingCurrency = configuration.getString("Selling-Currency", buyingCurrency);
        double buyingPrice = configuration.getDouble("Buying-Price");
        double sellingPrice = configuration.getDouble("Selling-Price", buyingPrice / 5);
        return new Sellable() {
            @Override
            public String getSellingCurrency() {
                return sellingCurrency;
            }

            @Override
            public String getBuyingCurrency() {
                return buyingCurrency;
            }

            @Override
            public double getSellingPrice() {
                return sellingPrice;
            }

            @Override
            public double getBuyingPrice() {
                return buyingPrice;
            }
        };
    }
}
