package io.github.anjoismysign.entity.plothelper;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.BlobCrudable;
import io.github.anjoismysign.bloblib.entities.BlobSerializable;
import io.github.anjoismysign.bloblib.utilities.ItemStackUtil;
import io.github.anjoismysign.director.TycoonManagerDirector;

import java.util.Objects;

public record PlotHelperInventory(@NotNull BlobCrudable blobCrudable,
                                  @NotNull Inventory getInventory) implements BlobSerializable {

    public static PlotHelperInventory GENERATE(BlobCrudable crudable, TycoonManagerDirector director) {
        Document document = crudable.getDocument();
        @Nullable String readInventory = document.containsKey("inventory") ?
                document.getString("inventory") :
                null;
        ItemStack[] stored = readInventory == null ? new ItemStack[0] :
                ItemStackUtil.itemStackArrayFromBase64(readInventory);
        Inventory inventory = Bukkit.createInventory(null, 54, "Plot Helper");
        for (int i = 0; i < stored.length; i++) {
            if (stored[i] != null) {
                inventory.setItem(i, stored[i]);
            }
        }
        return new PlotHelperInventory(crudable, inventory);
    }

    public void open(@NotNull Player player) {
        Objects.requireNonNull(player, "'player' cannot be null");
        player.openInventory(getInventory);
        String title = BlobLibTranslatableAPI.getInstance()
                .getTranslatableSnippet("BlobTycoon-PlotHelper.Inventory", player)
                .get();
        player.getOpenInventory().setTitle(title);
    }

    @Override
    public BlobCrudable serializeAllAttributes() {
        ItemStack[] current = getInventory().getContents();
        Document document = blobCrudable.getDocument();
        document.put("inventory", ItemStackUtil.itemStackArrayToBase64(current));
        return blobCrudable;
    }
}
