package us.mytheria.blobtycoon.command;

import me.anjoismysign.skeramidcommands.command.Command;
import me.anjoismysign.skeramidcommands.command.CommandBuilder;
import me.anjoismysign.skeramidcommands.commandtarget.BukkitCommandTarget;
import me.anjoismysign.skeramidcommands.server.bukkit.BukkitAdapter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.TycoonPlayer;

import java.util.Objects;

public class VisitCmd {

    public static VisitCmd of(@NotNull TycoonManagerDirector director) {
        Objects.requireNonNull(director);
        return new VisitCmd(director);
    }

    private VisitCmd(TycoonManagerDirector managerDirector) {
        Command visitCommand = CommandBuilder.of("visitplot")
                .build();
        visitCommand.addUsage("/visitplot <player>");
        visitCommand.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        visitCommand.onExecute((permissionMessenger, args) -> {
            CommandSender commandSender = BukkitAdapter.getInstance().of(permissionMessenger);
            if (!(commandSender instanceof Player playerSender)) {
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
            if (target.getName().equals(playerSender.getName())) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon.Cannot-Visit-Yourself", playerSender)
                        .handle(playerSender);
                return;
            }
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(target);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", playerSender)
                        .handle(playerSender);
                return;
            }
            PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
            plotProfile.visit(playerSender);
        });
    }
}
