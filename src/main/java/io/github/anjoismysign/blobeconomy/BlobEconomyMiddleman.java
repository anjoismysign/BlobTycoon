package io.github.anjoismysign.blobeconomy;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.entities.currency.Currency;
import io.github.anjoismysign.BlobTycoon;

public interface BlobEconomyMiddleman {
    static BlobEconomyMiddleman getInstance() {
        return ((BlobTycoon) Bukkit.getPluginManager().getPlugin("BlobTycoon"))
                .getBlobEconomyMiddleman();
    }

    @Nullable
    Currency getCurrency(String currency);
}
