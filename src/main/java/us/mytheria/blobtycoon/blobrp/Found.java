package us.mytheria.blobtycoon.blobrp;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.blobrp.BlobRPAPI;
import us.mytheria.blobtycoon.entity.Sellable;

public class Found implements BlobRPMiddleman {
    @Override
    public boolean addShopArticle(@NotNull TranslatableItem translatableItem,
                                  @NotNull Sellable sellable,
                                  @NotNull NamespacedKey namespacedKey) {

        return BlobRPAPI.getInstance().addComplexShopArticle(translatableItem,
                sellable.getBuyingPrice(),
                namespacedKey,
                sellable.getSellingPrice(),
                sellable.getBuyingCurrency(),
                sellable.getSellingCurrency());
    }

    @Override
    public void reloadMerchants() {
        BlobRPAPI.getInstance().reloadMerchants();
    }

    @Override
    public void reloadRecipes() {
        BlobRPAPI.getInstance().reloadRecipes();
    }
}
