package me.ferzic.nations.managers;

import me.ferzic.nations.classes.Nation;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

import static me.ferzic.nations.classes.Nation.*;

public class NationManager {

    // TODO: Fix all messages.

    public List<String> registeredNations;
    public List<String> pendingInvites;

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
        registeredNations.add(name);
    }

    public void leaveNation(UUID player) {
        for(String nationName : getConfigurtion().getKeys(false)) {
            ConfigurationSection section = getConfigurtion().getConfigurationSection(nationName);

            if (section == null) continue;

            List<String> members = section.getStringList("members");

            if(members.contains(player.toString())) {
                members.remove(player.toString());
                sendMsgToPlr(Bukkit.getPlayer(player), "§aYou have left the nation §e" + nationName + "§a.");
                Nation.saveConfiguration();
                return; // stop here so "not in a nation" isn’t also sent
            }
        }

        sendMsgToPlr(Bukkit.getPlayer(player), "§cYou are not a member of any nation.");
    }


    public void invitePlayer(UUID owner, UUID inviting) {
        Player invited = Bukkit.getPlayer(inviting);

        if(invited == null) {
            sendMsgToPlr(Bukkit.getPlayer(owner), "§cThat player is not online.");
            return;
        }

        if(pendingInvites.contains(getNationOfPlayer(owner) + "-" + inviting)) {
            sendMsgToPlr(Bukkit.getPlayer(owner), "§cThat player already has a pending invite to your nation.");
            return;
        }

        pendingInvites.add(getNationOfPlayer(owner) + "-" + inviting);

        sendMsgToPlr(invited, "§aYou have been invited to join §e" + getNationOfPlayer(owner) + "§a. Use §e/nations accept " + getNationOfPlayer(owner) + "§a to join or §e/nations deny " + getNationOfPlayer(owner) + "§a to refuse. You have 5 minutes.");
        sendMsgToPlr(Bukkit.getPlayer(owner), "§aInvite sent to §e" + invited.getName() + "§a.");

        Timer timer = new Timer();
        TimerTask countdown = new TimerTask() {
            @Override
            public void run() {
                if(pendingInvites.contains(getNationOfPlayer(owner) + "-" + inviting)) {
                    pendingInvites.remove(getNationOfPlayer(owner) + "-" + inviting);
                    sendMsgToPlr(invited, "§cYour invite to join §e" + getNationOfPlayer(owner) + "§c has expired.");
                    sendMsgToPlr(Bukkit.getPlayer(owner), "§cThe invite sent to §e" + invited.getName() + "§c has expired.");
                } else {
                    timer.cancel();
                }
            }
        };

        timer.schedule(countdown, 300000);
        Nation.saveConfiguration();
    }


    public void acceptInvite(Nation nation, UUID invited) {
        for(String invite : pendingInvites) {
            String[] parts = invite.split("-", 2);

            String nationName = parts[0];
            UUID inviting = UUID.fromString(parts[1]);

            if(nationName == null) return;

            if(invited.equals(inviting)) {
                if(nation != null) {
                    addPlayerTo(nation, invited);
                    pendingInvites.remove(nation + "-" + invited);
                    sendMsgToPlr(Bukkit.getPlayer(invited), "§aYou have joined the nation §e" + nationName + "§a.");
                    sendMsgToPlr(getOwner(nationName), "§a§e" + Objects.requireNonNull(Bukkit.getPlayer(invited)).getName() + "§a has joined your nation.");
                    Nation.saveConfiguration();
                    return;
                }
            }
        }

        sendMsgToPlr(Bukkit.getPlayer(invited), "§cYou do not have any pending invites.");
        Nation.saveConfiguration();
    }


    public void denyInvite(Nation nation, UUID denying) {
        for(String invite : pendingInvites) {
            String[] parts = invite.split("-", 2);

            String nationName = parts[0];
            UUID inviting = UUID.fromString(parts[1]);

            if(nationName == null) return;

            if(denying.equals(inviting)) {
                if(nation != null) {
                    pendingInvites.remove(nation + "-" + inviting);
                    sendMsgToPlr(getOwner(nationName), "§c§e" + Objects.requireNonNull(Bukkit.getPlayer(denying)).getName() + "§c has denied your nation invite.");
                    sendMsgToPlr(Bukkit.getPlayer(denying), "§aYou denied the invite to join §e" + nationName + "§a.");
                    Nation.saveConfiguration();
                    return;
                }
            }
        }

        sendMsgToPlr(Bukkit.getPlayer(denying), "§cYou do not have any pending invites.");
        Nation.saveConfiguration();
    }

    public void kickPlayer(Nation nation, UUID player) {
        ConfigurationSection section = getConfigurtion().getConfigurationSection(nation.name);

        if(section == null) {
            return;
        }

        List<String> members = section.getStringList("members");
        members.remove(player.toString());

        section.set("members", members);
        Nation.saveConfiguration();

        Player target = Bukkit.getPlayer(player);
        if(target == null) {
            return;
        }

        target.sendMessage("§cYou have been kicked from your nation!");
        Nation.saveConfiguration();
    }

    public void setOwner(Nation nation, UUID newOwner) {
        ConfigurationSection section = getConfigurtion().getConfigurationSection(nation.name);

        if(section == null) {
            return;
        }

        section.set("owner", newOwner);
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

        // Clear runtime list but don’t null fields
        if (nation.members != null) {
            nation.members.clear();
        }

        registeredNations.remove(nation.name);

        Nation.saveConfiguration();
    }

}
