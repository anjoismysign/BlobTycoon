package io.github.anjoismysign.blobeconomy;

import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.entities.currency.Currency;

public class BlobEconomyAbsent implements BlobEconomyMiddleman {
    private static BlobEconomyAbsent instance;

    public static BlobEconomyAbsent getInstance() {
        if (instance == null) {
            instance = new BlobEconomyAbsent();
        }
        return instance;
    }

    private BlobEconomyAbsent() {
    }

    public @Nullable Currency getCurrency(String currency) {
        return null;
    }
}
