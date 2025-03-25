package us.mytheria.blobtycoon.blobeconomy;

import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.currency.Currency;

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
