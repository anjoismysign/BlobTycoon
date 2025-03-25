package us.mytheria.blobtycoon.command;

import me.anjoismysign.skeramidcommands.command.Command;
import me.anjoismysign.skeramidcommands.command.CommandBuilder;
import me.anjoismysign.skeramidcommands.commandtarget.BukkitCommandTarget;
import me.anjoismysign.skeramidcommands.server.bukkit.BukkitAdapter;
import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibEconomyAPI;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.inventory.BlobInventory;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.director.manager.PlotManager;
import us.mytheria.blobtycoon.entity.*;
import us.mytheria.blobtycoon.entity.configuration.CostIncreaseConfiguration;
import us.mytheria.blobtycoon.entity.configuration.RebirthConfiguration;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BlobTycoonCmd {

    public static BlobTycoonCmd of(@NotNull TycoonManagerDirector director) {
        Objects.requireNonNull(director);
        return new BlobTycoonCmd(director);
    }

    private BlobTycoonCmd(TycoonManagerDirector managerDirector) {
        Command blobtycoonCommand = CommandBuilder.of("blobtycoon")
                .build();
        Command profileCommand = blobtycoonCommand.child("profile");
        Command createCommand = profileCommand.child("create");
        createCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        createCommand.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            UUID uuid = target.getUniqueId();
            Bukkit.getScheduler().runTask(managerDirector.getPlugin(), () -> {
                if (target != Bukkit.getPlayer(uuid))
                    return;
                BlobLibInventoryAPI.getInstance()
                        .trackInventory(target.getPlayer(), "BlobTycoon-New-Profile")
                        .getInventory().open(target);
            });
        });
        Command switchCommand = profileCommand.child("switch");
        switchCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        switchCommand.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                    .getTycoonPlayer(target);
            if (tycoonPlayer == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", target)
                        .handle(target);
                return;
            }
            UUID uuid = target.getUniqueId();
            Bukkit.getScheduler().runTask(managerDirector.getPlugin(), () -> {
                if (target != Bukkit.getPlayer(uuid))
                    return;
                BlobLibInventoryAPI.getInstance()
                        .customSelector("BlobTycoon-Switch-Profile",
                                target, "Profiles", "Profile",
                                tycoonPlayer::getProfileData,
                                profile -> {
                                    target.closeInventory();
                                    int index = profile.getIndex();
                                    ProfileSwitchResult result = tycoonPlayer.switchProfile(index, null, false);
                                    if (result == ProfileSwitchResult.ALREADY_LOADED)
                                        BlobLibMessageAPI.getInstance()
                                                .getMessage("BlobTycoon.Profile-Already-Loaded", target)
                                                .toCommandSender(target);
                                },
                                profile -> TranslatableItem.by("BlobTycoon.Switch-Profile-Element")
                                        .localize(target)
                                        .modder()
                                        .replace("%profile%", profile.getName())
                                        .get()
                                        .get());
            });
        });
        Command inviteCommand = profileCommand.child("invite");
        inviteCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        inviteCommand.onExecute(((permissionMessenger, args) -> {
            CommandSender commandSender = BukkitAdapter.getInstance().of(permissionMessenger);
            if (!(commandSender instanceof Player sender)) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("System.Console-Not-Allowed-Command", commandSender)
                        .toCommandSender(commandSender);
                return;
            }
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", commandSender)
                        .toCommandSender(commandSender);
                return;
            }
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                    .getTycoonPlayer(target);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", target)
                        .handle(target);
                return;
            }
            PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
            plotProfile.invite(target, 120);
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Player-Invited-Success", sender)
                    .modder()
                    .replace("%player%", target.getName())
                    .get()
                    .handle(sender);
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Profile-Invite-Notify", target)
                    .modder()
                    .replace("%player%", target.getName())
                    .get()
                    .handle(target);
        }));
        Command inviterCommand = profileCommand.child("inviter");
        inviterCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        inviterCommand.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                    .getTycoonPlayer(target);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", target)
                        .handle(target);
                return;
            }
            UUID uuid = target.getUniqueId();
            PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
            Bukkit.getScheduler().runTask(managerDirector.getPlugin(), () -> {
                if (target != Bukkit.getPlayer(uuid) || !plotProfile.isValid())
                    return;
                BlobLibInventoryAPI.getInstance()
                        .customSelector("BlobTycoon-Invite-Profile",
                                target, "Players", "Player",
                                () -> {
                                    List<UUID> proprietors = plotProfile.getProprietors().values().stream()
                                            .map(PlotProprietorProfile::getIdentification)
                                            .map(UUID::fromString)
                                            .toList();
                                    return Bukkit.getOnlinePlayers().stream()
                                            .filter(player -> !proprietors.contains(player.getUniqueId()))
                                            .toList();
                                },
                                invitedPlayer -> {
                                    target.closeInventory();
                                    if (!plotProfile.isValid())
                                        return;
                                    plotProfile.invite(invitedPlayer, 120);
                                    BlobLibMessageAPI.getInstance()
                                            .getMessage("BlobTycoon.Player-Invited-Success", target)
                                            .modder()
                                            .replace("%player%", invitedPlayer.getName())
                                            .get()
                                            .handle(target);
                                    BlobLibMessageAPI.getInstance()
                                            .getMessage("BlobTycoon.Profile-Invite-Notify", invitedPlayer)
                                            .modder()
                                            .replace("%player%", target.getName())
                                            .get()
                                            .handle(invitedPlayer);
                                },
                                player -> TranslatableItem.by("BlobTycoon.Invite-Profile-Element")
                                        .localize(target)
                                        .modder()
                                        .replace("%player%", player.getName())
                                        .get()
                                        .get());
            });
        });
        Command joinCommand = profileCommand.child("join");
        joinCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        joinCommand.onExecute((permissionMessenger, args) -> {
            CommandSender commandSender = BukkitAdapter.getInstance().of(permissionMessenger);
            if (!(commandSender instanceof Player sender)) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("System.Console-Not-Allowed-Command", commandSender)
                        .toCommandSender(commandSender);
                return;
            }
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", commandSender)
                        .toCommandSender(commandSender);
                return;
            }
            TycoonPlayer targetTycoonPlayer = BlobTycoonInternalAPI.getInstance()
                    .getTycoonPlayer(target);
            if (targetTycoonPlayer == null || targetTycoonPlayer.getProfile() == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", target)
                        .handle(target);
                return;
            }
            TycoonPlayer senderTycoonPlayer = BlobTycoonInternalAPI.getInstance()
                    .getTycoonPlayer(sender);
            if (senderTycoonPlayer == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", sender)
                        .handle(sender);
                return;
            }
            PlotProfile plotProfile = targetTycoonPlayer.getProfile().getPlotProfile();
            if (!plotProfile.isInvited(sender)) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon.Profile-Not-Invited", sender)
                        .handle(sender);
                return;
            }
            if (senderTycoonPlayer.getProfileData().stream()
                    .map(ProfileData::getId)
                    .toList().contains(plotProfile.getIdentification())) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon.Duplicate-Profile", sender)
                        .handle(sender);
                return;
            }
            sender.closeInventory();
            if (!senderTycoonPlayer.joinProfile(target))
                return;
            plotProfile.forEachOnlineProprietor(tycoonPlayer -> {
                Player onlinePlayer = tycoonPlayer.getPlayer();
                if (onlinePlayer == null)
                    return;
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon.Profile-Player-Joined", onlinePlayer)
                        .modder()
                        .replace("%player%", sender.getName())
                        .get()
                        .handle(onlinePlayer);
            });
        });
        Command deleteCommand = profileCommand.child("delete");
        deleteCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        deleteCommand.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                    .getTycoonPlayer(target);
            if (tycoonPlayer == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", target)
                        .handle(target);
                return;
            }
            tycoonPlayer.deleteSelectedProfile();
        });
        Command progressCommand = blobtycoonCommand.child("progress");
        Command rebirtherCommand = progressCommand.child("rebirther");
        rebirtherCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        rebirtherCommand.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                    .getTycoonPlayer(target);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", sender)
                        .toCommandSender(sender);
                return;
            }
            PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
            int rebirths = plotProfile.getRebirths();
            CostIncreaseConfiguration configuration = RebirthConfiguration.getInstance().getCostIncreaseConfiguration();
            IdentityEconomy economy = BlobLibEconomyAPI.getInstance().getElasticEconomy().getImplementation(configuration.getCostCurrency());
            BlobInventory blobInventory = BlobLibInventoryAPI.getInstance()
                    .trackInventory(target.getPlayer(), "Rebirth").getInventory();
            blobInventory.modder("Rebirth", modder -> {
                modder.replace("%format%", economy.format(configuration.getCost(rebirths)));
            });
            UUID uuid = target.getUniqueId();
            Bukkit.getScheduler().runTask(managerDirector.getPlugin(), () -> {
                if (target != Bukkit.getPlayer(uuid) || !plotProfile.isValid())
                    return;
                blobInventory.open(target);
            });
        });
        Command rebirthCommand = progressCommand.child("rebirth");
        rebirthCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        rebirthCommand.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                    .getTycoonPlayer(target);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", sender)
                        .toCommandSender(sender);
                return;
            }
            PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
            plotProfile.rebirth();
        });
        Command resetCommand = progressCommand.child("reset");
        resetCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        resetCommand.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                    .getTycoonPlayer(target);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", sender)
                        .toCommandSender(sender);
                return;
            }
            PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
            plotProfile.reset(sender, target);
        });
        Command plotCommand = blobtycoonCommand.child("plot");
        Command addCommand = plotCommand.child("add");
        addCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        addCommand.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            /*
             * getSource != null
             * proceeding to add a plot to player's plot profile's expansions
             */
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                    .getTycoonPlayer(target);
            if (tycoonPlayer == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", sender)
                        .toCommandSender(sender);
                return;
            }
            PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
            int size = plotProfile.getExpansions().size();
            plotProfile.getExpansions().add(new PlotExpansion(DefaultStructures.CLEAN.getStructrador(),
                    plotProfile.getPlot().getData().getDirection(),
                    size,
                    managerDirector.getPlugin()));
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoonCmd.Plot-Successfully-Given", sender)
                    .modder()
                    .replace("%player%", target.getName())
                    .get()
                    .toCommandSender(sender);
        });

        Command setupCommand = plotCommand.child("setup");
        setupCommand.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            if (!(sender instanceof Player player)) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("System.Console-Not-Allowed-Command", sender)
                        .toCommandSender(sender);
                return;
            }
            PlotManager plotManager = managerDirector.getPlotManager();
            plotManager.getPlotDataBuilderManager()
                    .getOrDefault(player.getUniqueId()).openInventory();
        });

        Command tellerCommand = blobtycoonCommand.child("teller");
        Command withdrawCommand = tellerCommand.child("withdraw");
        withdrawCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        withdrawCommand.onExecute((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            ValuableTeller.getInstance().withdraw(target);
        });
    }
}
