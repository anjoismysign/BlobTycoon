package us.mytheria.blobtycoon.entity.writers;

import org.bukkit.configuration.ConfigurationSection;
import us.mytheria.blobtycoon.entity.Sellable;

public class SellableWriter {
    public static void WRITE(Sellable sellable, ConfigurationSection section) {
        section.set("Selling-Currency", sellable.getSellingCurrency());
        section.set("Buying-Currency", sellable.getBuyingCurrency());
        section.set("Selling-Price", sellable.getSellingPrice());
        section.set("Buying-Price", sellable.getBuyingPrice());
    }
}
