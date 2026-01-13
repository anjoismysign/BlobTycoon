package io.github.anjoismysign.blobtycoon.blobeconomy;

import io.github.anjoismysign.bloblib.entities.currency.Currency;
import io.github.anjoismysign.blobtycoon.BlobTycoon;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public interface BlobEconomyMiddleman {
    static BlobEconomyMiddleman getInstance() {
        return ((BlobTycoon) Bukkit.getPluginManager().getPlugin("BlobTycoon"))
                .getBlobEconomyMiddleman();
    }

    @Nullable
    Currency getCurrency(String currency);
}
