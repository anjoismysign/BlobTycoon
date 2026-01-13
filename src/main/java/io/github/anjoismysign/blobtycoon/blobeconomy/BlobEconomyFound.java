package io.github.anjoismysign.blobtycoon.blobeconomy;

import io.github.anjoismysign.blobeconomy.BlobEconomyAPI;
import io.github.anjoismysign.bloblib.entities.currency.Currency;
import org.jetbrains.annotations.Nullable;

public class BlobEconomyFound implements BlobEconomyMiddleman {
    private static BlobEconomyFound instance;
    private final BlobEconomyAPI api;

    public static BlobEconomyFound getInstance() {
        if (instance == null) {
            instance = new BlobEconomyFound();
        }
        return instance;
    }

    private BlobEconomyFound() {
        this.api = BlobEconomyAPI.getInstance();
    }


    public @Nullable Currency getCurrency(String currency) {
        return api.getCurrency(currency);
    }
}
