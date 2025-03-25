package us.mytheria.blobtycoon.blobeconomy;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.currency.Currency;
import us.mytheria.blobtycoon.BlobTycoon;

public interface BlobEconomyMiddleman {
    static BlobEconomyMiddleman getInstance() {
        return ((BlobTycoon) Bukkit.getPluginManager().getPlugin("BlobTycoon"))
                .getBlobEconomyMiddleman();
    }

    @Nullable
    Currency getCurrency(String currency);
}
