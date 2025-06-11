package us.mytheria.blobtycoon.director;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.BlobExecutor;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.bloblib.entities.ObjectDirectorData;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.blobtycoon.blobrp.BlobRPMiddleman;
import us.mytheria.blobtycoon.entity.Sellable;
import us.mytheria.blobtycoon.entity.structure.TycoonModel;
import us.mytheria.blobtycoon.entity.structure.TycoonModelHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ModelHolderDirector<T extends TycoonModelHolder<M>,
        M extends TycoonModel> extends ObjectDirector<T> {
    private final Map<String, String> linkedItems;
    private final Map<String, Function<TranslatableItem, ItemStack>> appliers;

    public static <T extends TycoonModelHolder<M>,
            M extends TycoonModel> ModelHolderDirector<T, M> of(@NotNull TycoonManagerDirector managerDirector,
                                                                @NotNull Function<File, T> readFunction,
                                                                @NotNull String objectName) {
        Objects.requireNonNull(managerDirector);
        Objects.requireNonNull(readFunction);
        Objects.requireNonNull(objectName);
        return new ModelHolderDirector<>(managerDirector, readFunction, objectName);
    }

    private ModelHolderDirector(TycoonManagerDirector managerDirector,
                                Function<File, T> readFunction,
                                String objectName) {
        super(managerDirector, ObjectDirectorData.simple(managerDirector
                        .getRealFileManager(), objectName), readFunction,
                false);
        linkedItems = new HashMap<>();
        appliers = new HashMap<>();
        addAdminChildCommand(executorData -> {
            CommandSender sender = executorData.sender();
            BlobExecutor executor = executorData.executor();
            if (!executor.isInstanceOfPlayer(sender))
                return true;
            String[] args = executorData.args();
            if (args.length == 0)
                return false;
            String arg = args[0];
            if (!arg.equalsIgnoreCase("give"))
                return false;
            if (args.length < 3) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoonCmd.TycoonModel-Give-Usage", sender)
                        .modder()
                        .replace("%o%", objectName)
                        .get()
                        .toCommandSender(sender);
                return true;
            }
            String key = args[1];
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException exception) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoonCmd.TycoonModel-Give-Usage", sender)
                        .modder()
                        .replace("%o%", objectName)
                        .get()
                        .toCommandSender(sender);
                return true;
            }
            T holder = getObjectManager().getObject(key);
            if (holder == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoonCmd.TycoonModel-Not-Found", sender)
                        .modder()
                        .replace("%o%", key)
                        .get()
                        .toCommandSender(sender);
                return true;
            }
            String target = args[3];
            Player player = Bukkit.getPlayer(target);
            if (player == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return true;
            }
            M model = holder.getModel();
            ItemStack itemStack = holder.display(player);
            itemStack.setAmount(amount);
            player.getInventory().addItem(itemStack);
            if (sender instanceof ConsoleCommandSender)
                return true;
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoonCmd.TycoonModel-Give-Success", sender)
                    .modder()
                    .replace("%o%", key)
                    .replace("%a%", amount + "")
                    .replace("%p%", player.getName())
                    .get()
                    .toCommandSender(sender);
            return true;
        });
        addAdminChildCommand(executorData -> {
            CommandSender sender = executorData.sender();
            BlobExecutor executor = executorData.executor();
            if (!executor.isInstanceOfPlayer(sender))
                return true;
            Player player = (Player) sender;
            String[] args = executorData.args();
            if (args.length == 0)
                return false;
            String arg = args[0];
            if (!arg.equalsIgnoreCase("get"))
                return false;
            if (args.length < 2) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoonCmd.TycoonModel-Get-Usage", player)
                        .modder()
                        .replace("%o%", objectName)
                        .get()
                        .handle(player);
                return true;
            }
            String key = args[1];
            int amount = 1;
            if (args.length >= 3) {
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException exception) {
                    BlobLibMessageAPI.getInstance()
                            .getMessage("BlobTycoonCmd.TycoonModel-Get-Usage", player)
                            .modder()
                            .replace("%o%", objectName)
                            .get()
                            .handle(player);
                    return true;
                }
            }
            T holder = getObjectManager().getObject(key);
            if (holder == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoonCmd.TycoonModel-Not-Found", player)
                        .modder()
                        .replace("%o%", key)
                        .get()
                        .handle(player);
                return true;
            }
            M model = holder.getModel();
            ItemStack itemStack = holder.display(player);
            itemStack.setAmount(amount);
            player.getInventory().addItem(itemStack);
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoonCmd.TycoonModel-Get-Success", player)
                    .modder()
                    .replace("%o%", key)
                    .replace("%a%", amount + "")
                    .get()
                    .handle(player);
            return true;
        });
        addAdminChildTabCompleter((executorData -> {
            CommandSender sender = executorData.sender();
            BlobExecutor executor = executorData.executor();
            String[] args = executorData.args();
            if (args.length == 0)
                return null;
            List<String> list = new ArrayList<>();
            switch (args.length) {
                case 1 -> {
                    list.add("get");
                    list.add("give");
                    return list;
                }
                case 2 -> {
                    String firstArg = args[0];
                    if (!firstArg.equalsIgnoreCase("get")
                            && !firstArg.equals("give"))
                        return null;
                    String key = args[1];
                    list.addAll(getObjectManager().keys());
                    return list;
                }
                case 3 -> {
                    String firstArg = args[0];
                    if (!firstArg.equalsIgnoreCase("get")
                            && !firstArg.equals("give"))
                        return null;
                    list.add("1");
                    list.add("16");
                    list.add("32");
                    list.add("64");
                    return list;
                }
                case 4 -> {
                    String firstArg = args[0];
                    if (!firstArg.equalsIgnoreCase("give"))
                        return null;
                    list.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName)
                            .toList());
                    return list;
                }
            }
            return null;
        }));
    }

    @Nullable
    public String isLinked(TranslatableItem item) {
        return linkedItems.get(item.identifier());
    }

    @Nullable
    public ItemStack hasDisplay(TranslatableItem item) {
        Function<TranslatableItem, ItemStack> function = appliers.get(item.identifier());
        if (function == null)
            return null;
        return function.apply(item);
    }

    @Override
    public void reload() {
        linkedItems.clear();
        appliers.clear();
        super.reload();
    }

    /**
     * Will process the holder, linking its TranslatabeItem and ShopArticle.
     *
     * @param holder The holder
     */
    public void process(T holder) {
        BlobRPMiddleman middleman = BlobRPMiddleman.get();
        TycoonModel model = holder.getModel();
        TranslatableItem translatableItem = model.getTranslatableItem();
        String key = holder.getKey();
        linkedItems.put(translatableItem.identifier(), key);
        appliers.put(translatableItem.identifier(), holder::apply);
        if (!holder.isSellable())
            return;
        Sellable sellable = Objects.requireNonNull(holder.getSellable());
        Bukkit.getScheduler().runTask(getPlugin(), () -> middleman
                .addShopArticle(translatableItem, sellable, getManagerDirector()
                        .createNamespacedKey(key)));
    }
}
