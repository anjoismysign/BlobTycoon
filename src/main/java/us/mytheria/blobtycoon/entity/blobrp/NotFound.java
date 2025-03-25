package us.mytheria.blobtycoon.entity.blobrp;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.blobtycoon.entity.Sellable;

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
