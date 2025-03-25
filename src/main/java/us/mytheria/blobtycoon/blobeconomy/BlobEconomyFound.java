package us.mytheria.blobtycoon.blobeconomy;

import org.jetbrains.annotations.Nullable;
import us.mytheria.blobeconomy.BlobEconomyAPI;
import us.mytheria.bloblib.entities.currency.Currency;

public class BlobEconomyFound implements BlobEconomyMiddleman {
    private final BlobEconomyAPI api;
    private static BlobEconomyFound instance;

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
