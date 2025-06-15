package io.github.anjoismysign.blobrp;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.entity.Sellable;

public interface BlobRPMiddleman {
    static BlobRPMiddleman get() {
        if (Bukkit.getPluginManager().getPlugin("BlobRP") == null)
            return new NotFound();
        return new Found();
    }

    boolean addShopArticle(@NotNull TranslatableItem translatableItem,
                           @NotNull Sellable sellable,
                           @NotNull NamespacedKey namespacedKey);

    void reloadMerchants();

    void reloadRecipes();
}
