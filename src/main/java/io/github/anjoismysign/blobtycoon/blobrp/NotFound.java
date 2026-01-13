package io.github.anjoismysign.blobtycoon.blobrp;

import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.blobtycoon.entity.Sellable;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

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
