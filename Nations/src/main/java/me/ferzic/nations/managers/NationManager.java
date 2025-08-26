package me.ferzic.nations.managers;

import me.ferzic.nations.classes.Nation;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

import static me.ferzic.nations.classes.Nation.*;

public class NationManager {

    // TODO: Fix all messages.

    List<String> registeredNations;
    List<String> pendingInvites;

    public NationManager() {
        this.registeredNations = new ArrayList<>();
        this.pendingInvites = new ArrayList<>();
    }

    // Helpful methods
    public Boolean isNationOwner(UUID player) {
        for(String nationName : getConfigurtion().getKeys(false)) {
            ConfigurationSection section = getConfigurtion().getConfigurationSection(nationName);

            if (section == null) continue;

            String owner = section.getString("owner");

            if(owner == null) {
                continue;
            }

            return owner.equals(player.toString());
        }

        return false;
    }

    public Boolean isInNation(UUID player) {
        Object value = Nation.getNationOfPlayer(player);
        return value != null;
    }

    public void sendMsgToPlr(Player player, String msg) {

        if(player == null) {
            return;
        }

        player.sendMessage(msg);
    }

    // Functions
    public void createNation(String name, UUID owner) {
        // Make the nation.
        Nation nation = new Nation(owner, name);

        // Add owner's name.
        ConfigurationSection section = getConfigurtion().createSection(name);
        section.set("owner", owner.toString());
        section.set("members", new ArrayList<>());
        Nation.saveConfiguration();

        // Add nation to registered nations.
        registeredNations.add(getConfigurtion().getString(name));
    }

    public void leaveNation(UUID player) {

        for(String nationName : getConfigurtion().getKeys(false)) {
            ConfigurationSection section = getConfigurtion().getConfigurationSection(nationName);

            if (section == null) continue;

            List<String> members = section.getStringList("members");

            if(members.contains(player.toString())) {
                members.remove(player.toString());
                sendMsgToPlr(Bukkit.getPlayer(player), "§aYou have left '" + nationName + "'");
                Nation.saveConfiguration();
            }
        }

        sendMsgToPlr(Bukkit.getPlayer(player), "§cYou are not in any nation!");
    }

    public void invitePlayer(UUID owner, UUID inviting) {
        Player invited = Bukkit.getPlayer(inviting);

        if(invited == null) {
            sendMsgToPlr(Bukkit.getPlayer(owner), "§cThat player is offline!");
            return;
        }

        if(pendingInvites.contains(getNationOfPlayer(owner) + "-" + inviting)) {
            sendMsgToPlr(Bukkit.getPlayer(owner), "§cThat player has already been invited!");
            return;
        }

        pendingInvites.add(getNationOfPlayer(owner) + "-" + inviting);

        sendMsgToPlr(invited, "§aYou have been invited to join '" + getNationOfPlayer(owner) + "'");

        Timer timer = new Timer();
        TimerTask countdown = new TimerTask() {
            @Override
            public void run() {
                pendingInvites.remove(getNationOfPlayer(owner) + "-" + inviting);
                sendMsgToPlr(invited, "§cYour invite to " + getNationOfPlayer(owner) + "' has expired!");
                sendMsgToPlr(Bukkit.getPlayer(owner), "§cYour invite to " + invited.getName() + "' has expired!");
            }
        };

        timer.schedule(countdown, 10000);

        Nation.saveConfiguration();

    }

    public void acceptInvite(UUID invited) {
        for(String invite : pendingInvites) {
            String[] parts = invite.split("-", 2);

            String nationName = parts[0];
            UUID inviting = UUID.fromString(parts[1]);

            if(nationName == null) {
                return;
            }

            if(Objects.equals(getNationOfPlayer(invited), invited.toString()) && invited == inviting) {
                Nation nation = getNationByName(nationName);

                if(nation != null) {
                    addPlayerTo(nation, inviting);
                    pendingInvites.remove(nation + "-" + inviting);
                    sendMsgToPlr(Bukkit.getPlayer(inviting), "§aYou have been added to '" + nationName + "'");
                    return;
                }
            }
        }

        sendMsgToPlr(Bukkit.getPlayer(invited), "§cYou do not have any pending invites!");

        Nation.saveConfiguration();

    }

    public void denyInvite(UUID denying) {
        for(String invite : pendingInvites) {
            String[] parts = invite.split("-", 2);

            String nationName = parts[0];
            UUID inviting = UUID.fromString(parts[1]);

            if(nationName == null) {
                return;
            }

            if(Objects.equals(getNationOfPlayer(denying), denying.toString()) && denying == inviting) {
                Nation nation = getNationByName(nationName);

                if(nation != null) {
                    pendingInvites.remove(nation + "-" + inviting);
                    sendMsgToPlr(getOwner(nationName), "§c" + Objects.requireNonNull(Bukkit.getPlayer(denying)).getName() + " has denied your invite!");
                    sendMsgToPlr(Bukkit.getPlayer(inviting), "§aYou have successfully denied the invite to '" + nationName + "'");
                    return;
                }
            }
        }

        sendMsgToPlr(Bukkit.getPlayer(denying), "§cYou do not have any pending invites!");

        Nation.saveConfiguration();
    }

    public void disbandNation(Nation nation) {
        ConfigurationSection section = getConfigurtion().getConfigurationSection(nation.name);

        if (section != null) {
            // Clear config values
            section.set("owner", null);
            section.set("members", null);
        }
        getConfigurtion().set(nation.name, null);

        // Clear runtime object
        nation.members = null;
        nation.name = null;
        nation.owner = null;

        Nation.saveConfiguration();
    }

}
