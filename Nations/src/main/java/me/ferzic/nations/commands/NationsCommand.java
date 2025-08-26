package me.ferzic.nations.commands;

import me.ferzic.nations.classes.Nation;
import me.ferzic.nations.managers.NationManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.ferzic.nations.Plugin;

import java.util.ArrayList;
import java.util.List;

public class NationsCommand implements TabExecutor {

    // TODO: Fix all messages.

    private final NationManager manager = Plugin.getInstance().getNationManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /nations <create|disband|invite|accept|deny|leave>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /nations create <name>");
                    return true;
                }
                if (manager.isNationOwner(player.getUniqueId())) {
                    player.sendMessage("§cYou already own a nation!");
                    return true;
                }
                if (manager.isInNation(player.getUniqueId())) {
                    player.sendMessage("§cYou are already in a nation!");
                    return true;
                }
                manager.createNation(args[1], player.getUniqueId());
                player.sendMessage("§a'" + args[1] + "' has successfully been created!");
                return true;

            case "disband":
                String nName = Nation.getNationOfPlayer(player.getUniqueId());

                if(nName == null) {
                    player.sendMessage("§cYou are not in any nation!");
                    return true;
                }

                Nation current = Nation.getNationByName(nName);
                if (!manager.isNationOwner(player.getUniqueId())) {
                    player.sendMessage("§cYou do not own this nation!");
                    return true;
                }
                if (current != null) {
                    manager.disbandNation(current);
                    player.sendMessage("§aSuccessfully disbanded the nation!!");
                } else {
                    player.sendMessage("§cYou do not own a nation!");
                }
                return true;

            case "invite":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /nations invite <player>");
                    return true;
                }
                if (!manager.isNationOwner(player.getUniqueId())) {
                    player.sendMessage("§cYou are not the owner of this nation!");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cThe player is offline!");
                    return true;
                }
                if(target == player) {
                    player.sendMessage("§cYou can't send an invite to yourself!");
                    return true;
                }
                manager.invitePlayer(player.getUniqueId(), target.getUniqueId());
                return true;

            case "accept":
                manager.acceptInvite(player.getUniqueId());
                return true;

            case "deny":
                manager.denyInvite(player.getUniqueId());
                return true;

            case "leave":

                // TODO: Check if the player is the nation owner and send a message.

                manager.leaveNation(player.getUniqueId());
                return true;

            case "reload":
                Nation.reloadConfig();
                player.sendMessage("§aSuccessfully reloaded config.");
                return true;

            default:
                player.sendMessage("§cUnknown command!");
                return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String @NotNull [] args) {

        if (args.length == 1) {
            return List.of("create", "disband", "invite", "accept", "deny", "leave", "reload");
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "invite":
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .toList();
                case "create":
                    return List.of("<name>");
            }
        }

        return new ArrayList<>();
    }
}
