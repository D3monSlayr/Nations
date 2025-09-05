package me.ferzic.nations.classes;

import me.ferzic.nations.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Nation {
    public UUID owner;
    public List<UUID> members;
    public String name;
    static File file;
    static FileConfiguration config;

    public Nation(UUID owner, String name) {
        this.owner = owner;
        this.name = name;
        this.members = new ArrayList<>();
    }

    // Memory
    public static void generateEmptyConfig() {
        file = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Nations")).getDataFolder(), "registry.yml");

        if(!(file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static void saveConfiguration() {
        try {
            config.save(file);
        } catch (IOException e) {
            System.out.println("File 'registry.yml' couldn't be saved!");
            throw new RuntimeException(e);
        }
    }

    public static void reloadConfig() {
        Plugin plugin = Plugin.getInstance();

        // --- Save current states first ---
        plugin.saveConfig();   // saves config.yml
        saveConfiguration();   // saves registry.yml

        // --- Reload from disk ---
        plugin.reloadConfig(); // reloads config.yml
        config = YamlConfiguration.loadConfiguration(file); // reloads registry.yml
    }

    public static FileConfiguration getConfigurtion() {
        return config;
    }

    public static String getNationOfPlayer(UUID player) {
        for(String nationName : getConfigurtion().getKeys(false)) {
            ConfigurationSection section = getConfigurtion().getConfigurationSection(nationName);

            if (section == null) continue;

            if(player.toString().equals(section.getString("owner"))) {
                return nationName;
            }

            List<String> members = section.getStringList("members");
            if (members.contains(player.toString())) {
                return nationName;
            }

        }

        return null;
    }

    public static Player getOwner(String nationName) {
        ConfigurationSection section = getConfigurtion().getConfigurationSection(nationName);

        if (section == null) {
            return null;
        }

        UUID owner = UUID.fromString(Objects.requireNonNull(section.getString("owner")));

        return Bukkit.getPlayer(owner);
    }

    public static Nation getNationByName(String nationName) {

        if (nationName == null) return null;

        ConfigurationSection section = getConfigurtion().getConfigurationSection(nationName);
        if (section == null) return null;

        // Read values from config
        String owner = section.getString("owner");
        List<String> members = section.getStringList("members");

        // Create and return your Nation object

        if(owner == null) {
            Plugin.getInstance().getLogger().severe("You better contact the plugin publisher with the error code [ERR101]!");
            return null;
        }

        return new Nation(UUID.fromString(owner), nationName);
    }


    public static void addPlayerTo(Nation nation, UUID player) {
        nation.members.add(player);

        ConfigurationSection section = getConfigurtion().getConfigurationSection(nation.name);

        if (section == null) {
            Plugin.getInstance().getLogger().severe("Could not find nation section for '" + nation.name + "'. [ERR102]");
            return;
        }
        List<String> members = section.getStringList("members");
        members.add(player.toString());

        section.set("members", members);
        saveConfiguration();
    }

}
