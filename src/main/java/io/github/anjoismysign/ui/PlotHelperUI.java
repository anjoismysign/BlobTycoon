package io.github.anjoismysign.ui;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibListenerAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.ReloadableUI;
import io.github.anjoismysign.bloblib.entities.tag.TagSet;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.bloblib.utilities.ItemStackUtil;
import io.github.anjoismysign.bloblib.utilities.TextColor;
import io.github.anjoismysign.bloblib.vault.multieconomy.ElasticEconomy;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.blobeconomy.BlobEconomyMiddleman;
import io.github.anjoismysign.entity.IndexedValue;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.configuration.PlotHelperConfiguration;
import io.github.anjoismysign.entity.plothelper.PlotHelperContainer;
import io.github.anjoismysign.entity.plothelper.PlotHelperInventory;
import io.github.anjoismysign.entity.plothelper.PlotHelperTrade;
import io.github.anjoismysign.entity.plothelper.PlotHelperTradeData;
import io.github.anjoismysign.entity.plothelper.PlotHelperTradeFactory;
import io.github.anjoismysign.event.TradeSaleFailEvent;
import io.github.anjoismysign.ui.context.CreateTradeContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PlotHelperUI implements ReloadableUI {
    protected PlotHelperUI() {
    }

    @Override
    public void reload(@NotNull BlobLibInventoryAPI inventoryAPI) {
        var plotHelperRegistry = inventoryAPI.getInventoryDataRegistry("Plot-Helper");
        plotHelperRegistry.onClick("Storage", blobInventoryClickEvent -> {
            Player player = (Player) blobInventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            Map<Integer, PlotHelperInventory> inventories = profile.getInventories();
            List<IndexedValue<PlotHelperInventory>> indexedInventories = inventories.entrySet().stream()
                    .map(entry -> new IndexedValue<>(entry.getKey(), entry.getValue()))
                    .toList();
            UUID uuid = player.getUniqueId();
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("BlobTycoon"), () -> {
                if (player != Bukkit.getPlayer(uuid) || !profile.isValid())
                    return;
                BlobLibInventoryAPI.getInstance().customSelector(
                        "Plot-Helper-Inventories",
                        player,
                        "Inventories",
                        "Inventory",
                        () -> indexedInventories,
                        indexedInventory -> {
                            PlotHelperInventory inventory = indexedInventory.getValue();
                            inventory.open(player);
                        },
                        indexedInventory -> BlobLibTranslatableAPI.getInstance().getTranslatableItem("BlobTycoon-PlotHelper.Inventory", player)
                                .modder()
                                .replace("%order%", indexedInventory.getIndex() + 1 + "")
                                .get()
                                .get(),
                        profile::openPlotHelperInventory,
                        null,
                        "BlobTycoon.Plot-Helper-Inventory-Open");
            });
        });

        if (PlotHelperConfiguration.getInstance().getMerchantConfiguration().isEnabled())
            plotHelperRegistry.onClick("Trading", inventoryClickEvent -> {
                Player player = (Player) inventoryClickEvent.getWhoClicked();
                TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
                if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                    player.closeInventory();
                    BlobLibMessageAPI.getInstance()
                            .getMessage("Player.Not-Inside-Plugin-Cache", player)
                            .handle(player);
                    return;
                }
                PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
                BlobLibInventoryAPI.getInstance().trackInventory(player, "Plot-Helper-Trading")
                        .getInventory().open(player);
            });

        var cancelTradeRegistry = inventoryAPI.getInventoryDataRegistry("Cancel-Trade");
        cancelTradeRegistry.onClick("Return", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            profile.openManageTradesUI(player);
        });
        cancelTradeRegistry.onClick("Cancel", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            PlotHelperTrade trade = tycoonPlayer.getTrade();
            if (trade == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("System.Error", player)
                        .handle(player);
                return;
            }
            if (!trade.isForSale()) {
                profile.openTradeUI(player, trade, true);
                return;
            }
            player.closeInventory();
            trade.cancel(player);
        });

        var proceedTradeRegistry = inventoryAPI.getInventoryDataRegistry("View-Trade");
        proceedTradeRegistry.onClose("BlobTycoon", ((event, sharableInventory) -> {
            Player player = (Player) event.getPlayer();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            tycoonPlayer.setCommunityTrade(null);
        }));
        proceedTradeRegistry.onClick("Return", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            PlotHelperContainer communityTrade = tycoonPlayer.getCommunityTrade();
            if (communityTrade != null) {
                BlobTycoonInternalAPI.getInstance().openCommunityTrade(player, communityTrade);
                // if it doesn't work on 2nd time, set community trade here
                return;
            }
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            profile.openTradesMarketplaceUI(player, tycoonPlayer.getTradeQuery());
        });
        proceedTradeRegistry.onClick("Buy", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            PlotHelperTrade trade = tycoonPlayer.getTrade();
            if (trade == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("System.Error", player)
                        .handle(player);
                return;
            }
            if (!trade.isForSale()) {
                profile.openTradeUI(player, trade, false);
                return;
            }
            if (!trade.canProcess(player)) {
                TradeSaleFailEvent event = new TradeSaleFailEvent(tycoonPlayer, trade);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isFixed()) {
                    trade.process(player);
                    player.closeInventory();
                    return;
                }
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Withdraw.Insufficient-Balance", player)
                        .handle(player);
                return;
            }
            player.closeInventory();
            trade.process(player);
        });

        var plotHelperTradingRegistry = inventoryAPI.getInventoryDataRegistry("Plot-Helper-Trading");
        plotHelperTradingRegistry.onClick("Create-Trade", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            profile.openCreateTradeUI(player);
        });
        plotHelperTradingRegistry.onClick("Manage-Trades", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            UUID uuid = player.getUniqueId();
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("BlobTycoon"), () -> {
                if (player != Bukkit.getPlayer(uuid) || !profile.isValid())
                    return;
                profile.openManageTradesUI(player);
            });
        });
        plotHelperTradingRegistry.onClick("Marketplace", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            UUID uuid = player.getUniqueId();
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("BlobTycoon"), () -> {
                if (player != Bukkit.getPlayer(uuid) || !profile.isValid())
                    return;
                profile.openTradesMarketplaceUI(player, tycoonPlayer.getTradeQuery());
            });
        });

        var tradesMarketPlaceRegistry = inventoryAPI.getInventoryDataRegistry("Trades-Marketplace");
        if (PlotHelperConfiguration.getInstance().getMerchantConfiguration().getTradeSearchConfiguration().isEnabled()) {
            tradesMarketPlaceRegistry.onClick("Reset-Search", blobInventoryClickEvent -> {
                Player player = (Player) blobInventoryClickEvent.getWhoClicked();
                TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
                if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                    player.closeInventory();
                    BlobLibMessageAPI.getInstance()
                            .getMessage("Player.Not-Inside-Plugin-Cache", player)
                            .handle(player);
                    return;
                }
                if (tycoonPlayer.getTradeQuery() == null)
                    return;
                blobInventoryClickEvent.setClickSound("BlobTycoon.Plot-Helper-Clear-Search");
                PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
                tycoonPlayer.setTradeQuery(null);
                profile.openTradesMarketplaceUI(player, null);
            });
            tradesMarketPlaceRegistry.onClick("Search", blobInventoryClickEvent -> {
                Player player = (Player) blobInventoryClickEvent.getWhoClicked();
                player.closeInventory();
                BlobLibListenerAPI.getInstance().addChatListener(
                        player,
                        300,
                        input -> {
                            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
                            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                                player.closeInventory();
                                BlobLibMessageAPI.getInstance()
                                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                                        .handle(player);
                                return;
                            }
                            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
                            tycoonPlayer.setTradeQuery(input);
                            profile.openTradesMarketplaceUI(player, input);
                        },
                        "BlobTycoon.PlotHelper-Search-Timeout",
                        "BlobTycoon.PlotHelper-Search");
            });
        }

        var createTradeRegistry = inventoryAPI.getInventoryDataRegistry("Create-Trade");
        createTradeRegistry.onClick("Return", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            BlobLibInventoryAPI.getInstance().trackInventory(player, "Plot-Helper-Trading")
                    .getInventory().open(player);
        });
        createTradeRegistry.onClick("Create", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            CreateTradeContext context = profile.getCreateTradeContext(player);
            if (!context.isReady())
                return;
            PlotHelperTradeData tradeData = context.toTradeData();
            PlotHelperTrade trade = PlotHelperTradeFactory.getInstance().createTrade(tradeData, profile);
            if (trade == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon.Plot-Helper-Too-Many-Trades", player)
                        .handle(player);
                return;
            }
            context.clear();
            profile.openCreateTradeUI(player);
        });
        createTradeRegistry.onClick("Item", blobInventoryClickEvent -> {
            Player player = (Player) blobInventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            CreateTradeContext context = profile.getCreateTradeContext(player);
            context.cancel(player);
            profile.openCreateTradeUI(player);
            blobInventoryClickEvent.setPlayClickSound(true);
        });
        createTradeRegistry.onClick("Currency", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
            BlobLibInventoryAPI.getInstance().selector(player, "Currency",
                    () -> elasticEconomy.getAllImplementations().stream().toList(),
                    economy -> {
                        CreateTradeContext context = profile.getCreateTradeContext(player);
                        context.setCurrency(economy.getName());
                        profile.openCreateTradeUI(player);
                    },
                    economy -> {
                        String name = economy.getName();
                        name = BlobEconomyMiddleman.getInstance()
                                .getCurrency(name) == null ?
                                economy.getName() :
                                BlobEconomyMiddleman.getInstance()
                                        .getCurrency(name).getDisplayName(player);
                        return TranslatableItem.by("BlobTycoon-PlotHelper.Trade-Currency")
                                .localize(player)
                                .modder()
                                .replace("%currency%", name)
                                .get()
                                .get();
                    });
        });
        createTradeRegistry.onClick("Price", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            player.closeInventory();
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            BlobLibListenerAPI.getInstance().addChatListener(
                    player,
                    300,
                    input -> {
                        CreateTradeContext context = profile.getCreateTradeContext(player);
                        try {
                            double parsed = Double.parseDouble(input);
                            context.setAmount(parsed);
                            profile.openCreateTradeUI(player);
                        } catch (NumberFormatException exception) {
                            Set<String> allKeywords = BlobTycoonInternalAPI.getInstance().getAllKeywords();
                            Set<String> halfKeywords = BlobTycoonInternalAPI.getInstance().getHalfKeywords();
                            ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
                            IdentityEconomy identityEconomy = Objects.requireNonNull(elasticEconomy.getImplementation(context.getCurrency()));
                            if (allKeywords.contains(input)) {
                                double amount = identityEconomy.getBalance(player);
                                context.setAmount(amount);
                                profile.openCreateTradeUI(player);
                            } else if (halfKeywords.contains(input)) {
                                double amount = identityEconomy.getBalance(player) / 2;
                                context.setAmount(amount);
                                profile.openCreateTradeUI(player);
                            } else {
                                BlobLibMessageAPI.getInstance()
                                        .getMessage("Builder.Number-Exception", player)
                                        .handle(player);
                            }
                        }
                    },
                    "Withdraw.Amount-Timeout",
                    "Withdraw.Amount");
        });
        createTradeRegistry.onPlayerInventoryClick("BlobTycoon", (blobInventoryClickEvent, sharableInventory) -> {
            blobInventoryClickEvent.setCancelled(true);
            Player player = (Player) blobInventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            ItemStack item = blobInventoryClickEvent.getCurrentItem();
            if (item == null || item.getType().isAir())
                return;
            TagSet tagSet = TagSet.by("BlobTycoon.Plot-Helper-Banned-Items");
            if (tagSet.contains(item.getType().name()))
                return;
            TranslatableItem translatableItem = TranslatableItem.byItemStack(item);
            if (translatableItem == null) {
                if (tagSet.contains(item.getType().name()))
                    return;
                ItemStack tradingItem = new ItemStack(item);
                item.setAmount(0);
                PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
                CreateTradeContext context = profile.getCreateTradeContext(player);
                context.cancel(player);
                context.setTradingItem(tradingItem);
                sharableInventory.modify("Item", ignored -> {
                    ItemStack clone = new ItemStack(tradingItem);
                    ItemMeta itemMeta = clone.getItemMeta();
                    if (itemMeta == null)
                        throw new IllegalStateException("Cannot set lore if #getItemMeta returns null");
                    List<String> add = BlobLibTranslatableAPI.getInstance()
                            .getTranslatableBlock("BlobTycoon-PlotHelper.Pick-Up-Item", player)
                            .get();
                    List<String> lore = itemMeta.getLore() == null ? new ArrayList<>() : itemMeta.getLore();
                    lore.addAll(add);
                    itemMeta.setLore(lore);
                    clone.setItemMeta(itemMeta);
                    return clone;
                });
                sharableInventory.modify("Create", current -> {
                    if (!context.isReady())
                        return current;
                    TranslatableItem tradingItemTranslatableItem = context.getTradingTranslatableItem();
                    String itemDisplay = tradingItemTranslatableItem == null ? ItemStackUtil.display(tradingItem) :
                            ItemStackUtil.display(tradingItemTranslatableItem.localize(player).get());
                    RegistryAccess access = RegistryAccess.registryAccess();
                    Registry<ItemType> registry = access.getRegistry(RegistryKey.ITEM);
                    if (registry.get(Key.key(itemDisplay)) != null)
                        itemDisplay = TextColor.PARSE("&f" + itemDisplay);
                    String name = context.getCurrency();
                    String format = BlobLibEconomyAPI.getInstance().getElasticEconomy().getImplementation(name)
                            .format(context.getAmount());
                    return TranslatableItem
                            .by("BlobTycoon-PlotHelper.Create-Trade")
                            .localize(player)
                            .modder()
                            .replace("%item%", itemDisplay)
                            .replace("%format%", format)
                            .get()
                            .get();
                });
                return;
            }
            if (tagSet.contains(translatableItem.identifier()))
                return;
            ItemStack tradingItem = new ItemStack(item);
            item.setAmount(0);
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            CreateTradeContext context = profile.getCreateTradeContext(player);
            context.cancel(player);
            context.setTradingItem(tradingItem);
            sharableInventory.modify("Item", current -> {
                ItemStack clone = new ItemStack(tradingItem);
                ItemMeta itemMeta = clone.getItemMeta();
                if (itemMeta == null)
                    throw new IllegalStateException("Cannot set lore if #getItemMeta returns null");
                List<String> add = BlobLibTranslatableAPI.getInstance()
                        .getTranslatableBlock("BlobTycoon-PlotHelper.Pick-Up-Item", player)
                        .get();
                List<String> lore = itemMeta.getLore() == null ? new ArrayList<>() : itemMeta.getLore();
                lore.addAll(add);
                itemMeta.setLore(lore);
                clone.setItemMeta(itemMeta);
                return clone;
            });
            sharableInventory.modify("Create", current -> {
                if (!context.isReady())
                    return current;
                TranslatableItem tradingItemTranslatableItem = context.getTradingTranslatableItem();
                String itemDisplay = tradingItemTranslatableItem == null ? ItemStackUtil.display(tradingItem) :
                        ItemStackUtil.display(tradingItemTranslatableItem.localize(player).get());
                RegistryAccess access = RegistryAccess.registryAccess();
                Registry<ItemType> registry = access.getRegistry(RegistryKey.ITEM);
                if (registry.get(Key.key(itemDisplay)) != null)
                    itemDisplay = TextColor.PARSE("&f" + itemDisplay);
                String name = context.getCurrency();
                String format = BlobLibEconomyAPI.getInstance().getElasticEconomy().getImplementation(name)
                        .format(context.getAmount());
                return TranslatableItem
                        .by("BlobTycoon-PlotHelper.Create-Trade")
                        .localize(player)
                        .modder()
                        .replace("%item%", itemDisplay)
                        .replace("%format%", format)
                        .get()
                        .get();
            });
        });
    }
}
