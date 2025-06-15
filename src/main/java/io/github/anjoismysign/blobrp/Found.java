package io.github.anjoismysign.blobrp;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.blobrp.BlobRPAPI;
import io.github.anjoismysign.entity.Sellable;

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
