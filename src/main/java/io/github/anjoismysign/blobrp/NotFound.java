package io.github.anjoismysign.blobrp;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.entity.Sellable;

public class NotFound implements BlobRPMiddleman {
    @Override
    public boolean addShopArticle(@NotNull TranslatableItem translatableItem, @NotNull Sellable sellable, @NotNull NamespacedKey namespacedKey) {
        return false;
    }

    @Override
    public void reloadMerchants() {

    }

    @Override
    public void reloadRecipes() {

    }
}
