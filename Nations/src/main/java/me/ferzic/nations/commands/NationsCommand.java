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
import java.util.Objects;
import java.util.UUID;

public class NationsCommand implements TabExecutor {

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
            player.sendMessage("§cUsage: /nations <create|disband|invite|accept|deny|leave|reload>");
            return true;
        }

        if(!player.hasPermission("nations.use")) {
            player.sendMessage("§cYou do not have permission to use this command!");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":

                if(!player.hasPermission("nations.create")) {
                    player.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }

                if(!(Nation.getNationByName(args[1]) == null)) {
                    player.sendMessage("§cA nation with that name already exists!");
                    return true;
                }

                // If the player is already a nation owner
                if (manager.isNationOwner(player.getUniqueId())) {
                    player.sendMessage("§cYou already own a nation!");
                    return true;
                }

                // If the player is already in a  nation
                if (manager.isInNation(player.getUniqueId())) {
                    player.sendMessage("§cYou are already in a nation!");
                    return true;
                }
                manager.createNation(args[1], player.getUniqueId());
                player.sendMessage("§aNation §e'" + args[1] + "'§a has been created successfully!");
                return true;

            case "disband":

                if(!player.hasPermission("nations.disband")) {
                    player.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }

                String nName = Nation.getNationOfPlayer(player.getUniqueId());

                // If the player doesn't have a nation
                if(nName == null) {
                    player.sendMessage("§cYou are not in any nation!");
                    return true;
                }

                Nation current = Nation.getNationByName(nName);

                // If the player is not the owner of the nation
                if (!manager.isNationOwner(player.getUniqueId())) {
                    player.sendMessage("§cYou do not own this nation!");
                    return true;
                }

                // If the nation doesn't exist
                if(current == null) {
                    player.sendMessage("§cYou are not in any nation!");
                    return true;
                }

                manager.disbandNation(current);
                player.sendMessage("§aNation §e'" + nName + "'§a has been disbanded.");
                return true;

            case "invite":

                if(!player.hasPermission("nations.invite")) {
                    player.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }

                // If the player doesn't type /nations invite <player>
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /nations invite <player>");
                    return true;
                }

                // If the player isn't the owner of the nation
                if (!manager.isNationOwner(player.getUniqueId())) {
                    player.sendMessage("§cYou are not the owner of this nation!");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);

                // If the target player doesn't exist
                if (target == null) {
                    player.sendMessage("§cThe player is offline!");
                    return true;
                }

                // If the player is the same as the target
                if(target == player) {
                    player.sendMessage("§cYou can't send an invite to yourself!");
                    return true;
                }

                manager.invitePlayer(player.getUniqueId(), target.getUniqueId());
                return true;

            case "accept":

                if(!player.hasPermission("nations.accept")) {
                    player.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }

                String nationName = args[1];
                Nation nation = Nation.getNationByName(nationName);

                if(nation == null) {
                    player.sendMessage("§cThat nation doesn't exist!");
                    return true;
                }

                manager.acceptInvite(nation, player.getUniqueId());
                return true;

            case "deny":

                if(!player.hasPermission("nations.deny")) {
                    player.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }

                String natinName = args[1];
                Nation naton = Nation.getNationByName(natinName);

                if(naton == null) {
                    player.sendMessage("§cThat nation doesn't exist!");
                    return true;
                }

                manager.denyInvite(naton, player.getUniqueId());
                return true;

            case "leave":

                if(!player.hasPermission("nations.leave")) {
                    player.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }

                String natioName = Nation.getNationOfPlayer(player.getUniqueId());
                Nation natio = Nation.getNationByName(natioName);

                if(natio == null) {
                    player.sendMessage("You are not in any nation!");
                    return true;
                }

                if(manager.isNationOwner(player.getUniqueId())) {
                    player.sendMessage("§cYou cannot leave your own nation. Use §e/nations disband§c instead.");
                    return true;
                }

                manager.leaveNation(player.getUniqueId());
                return true;

            case "reload":

                if(!player.hasPermission("nations.reload")) {
                    player.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }

                Nation.reloadConfig();
                player.sendMessage("§aNation configuration reloaded successfully.");
                return true;

            case "help":

                if(!player.hasPermission("nations.help")) {
                    player.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }

                player.sendMessage("§e--------- Nations 1.0 ---------");
                player.sendMessage("§e/nations help - Sends this message.");
                player.sendMessage("§e/nations create <name> - Creates a nation.");
                player.sendMessage("§e/nations disband - Deletes a nation you own.");
                player.sendMessage("§e/nations invite <player - Invites a player to your nation.");
                player.sendMessage("§e/nations accept <nation> - Accepts a nation's invite.");
                player.sendMessage("§e/nations deny <nation> - Deny's a nation's invite.");
                player.sendMessage("§e/nations leave - Makes you leave the nation your in.");
                player.sendMessage("§e/nations reload - Reloads the nation config.");
                player.sendMessage("§e--------------------------------");
                return true;

            case "kick":
                if(!player.hasPermission("nations.use")) {
                    player.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }

                if(!manager.isNationOwner(player.getUniqueId())) {
                    player.sendMessage("§cOnly owners can kick members out!");
                    return true;
                }

                Nation currentNation = Nation.getNationByName(Nation.getNationOfPlayer(player.getUniqueId()));

                if(currentNation == null) {
                    player.sendMessage("§cYou do not own a nation!");
                    return true;
                }

                manager.kickPlayer(currentNation, UUID.fromString(args[1]));
                player.sendMessage("§aSuccessfully kicked " + args[1]);
                return true;

            case "admin":

                if(!player.hasPermission("nations.admin")) {
                    player.sendMessage("§cYou do not have permission to use this command!");
                    return true;
                }

                switch (args[1]) {
                    case "setowner" -> {

                        Nation nation1 = Nation.getNationByName(args[2]);
                        if (nation1 == null) {
                            player.sendMessage("§cThat nation does not exist!");
                            return true;
                        }

                        Player targ = Bukkit.getPlayerExact(args[3]);
                        if (targ == null) {
                            player.sendMessage("§cThis player is not online!");
                            return true;
                        }

                        manager.setOwner(nation1, targ.getUniqueId());
                        player.sendMessage("§aSuccessfully changed the owner of " + args[2] + " to " + args[3] + "!");
                        return true;
                    }
                    case "delete" -> {

                        Nation nation1 = Nation.getNationByName(args[2]);
                        if (nation1 == null) {
                            player.sendMessage("§cThat nation does not exist!");
                            return true;
                        }

                        manager.disbandNation(nation1);
                        player.sendMessage("§aSuccessfully deleted '" + nation1.name + "'!");
                        return true;
                    }
                    case "add" -> {
                        Nation nation1 = Nation.getNationByName(args[2]);
                        if (nation1 == null) {
                            player.sendMessage("§cThat nation does not exist!");
                            return true;
                        }

                        Player targ = Bukkit.getPlayerExact(args[3]);
                        if (targ == null) {
                            player.sendMessage("§cThis player is not online!");
                            return true;
                        }

                        Nation.addPlayerTo(nation1, targ.getUniqueId());
                        player.sendMessage("§aSuccessfully added " + args[3] + " to " + args[2] + "!");
                        return true;
                    }
                }

            default:
                player.sendMessage("§cUnknown subcommand. Use §e/nations help§c for help.");
                return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player player)) return new ArrayList<>();

        // /nations <subcommand>
        if (args.length == 1) {
            return List.of("create", "disband", "invite", "accept", "deny", "leave", "reload", "help", "kick", "admin");
        }

        // /nations <subcommand> <arg1>
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "invite": {
                    String nationName = Nation.getNationOfPlayer(player.getUniqueId());
                    Nation nation = Nation.getNationByName(nationName);

                    if (nation == null) return new ArrayList<>();

                    return Bukkit.getOnlinePlayers().stream()
                            .filter(p -> !nation.members.contains(p.getUniqueId())) // not already in nation
                            .filter(p -> !p.getUniqueId().equals(player.getUniqueId())) // not yourself
                            .map(Player::getName)
                            .toList();
                }

                case "kick": {
                    String nationName = Nation.getNationOfPlayer(player.getUniqueId());
                    Nation nation = Nation.getNationByName(nationName);

                    if (nation == null) return new ArrayList<>();

                    return nation.members.stream()
                            .map(uuid -> {
                                Player p = Bukkit.getPlayer(uuid);
                                return p != null ? p.getName() : null;
                            })
                            .filter(Objects::nonNull)
                            .toList();
                }

                case "create":
                    return List.of("<name>");

                case "accept":
                case "deny": {
                    List<String> invitesForPlayer = new ArrayList<>();

                    for (String invite : manager.pendingInvites) {
                        String[] parts = invite.split("-", 2);
                        if (parts.length < 2) continue;

                        String nationName = parts[0];
                        UUID invited = UUID.fromString(parts[1]);

                        if (player.getUniqueId().equals(invited)) {
                            invitesForPlayer.add(nationName);
                        }
                    }
                    return invitesForPlayer;
                }

                case "admin":
                    return List.of("delete", "setowner", "add");
            }
        }

        // /nations admin <subcommand> <nation>
        if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            switch (args[1].toLowerCase()) {
                case "setowner":
                case "delete":
                case "add":
                    return manager.registeredNations;
            }
        }

        // /nations admin setowner <nation> <player>
        // /nations admin add <nation> <player>
        if (args.length == 4 && args[0].equalsIgnoreCase("admin")) {
            switch (args[1].toLowerCase()) {
                case "setowner":
                case "add":
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .toList();
            }
        }

        return new ArrayList<>();
    }

}
