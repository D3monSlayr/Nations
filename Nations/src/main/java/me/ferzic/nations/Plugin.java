package me.ferzic.nations;

import me.ferzic.nations.classes.Nation;
import me.ferzic.nations.commands.NationsCommand;
import me.ferzic.nations.managers.NationManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Plugin extends JavaPlugin {

    private static Plugin instance;
    private NationManager manager;

    @Override
    public void onEnable() {

        // Helper methods
        instance = this;
        manager = new NationManager();

        // Register command
        Objects.requireNonNull(getCommand("nations")).setExecutor(new NationsCommand());

        // Generate config.yml
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Handle and save the Nation Registries.
        Nation.generateEmptyConfig();
        Nation.getConfigurtion().options().copyDefaults(true);
        Nation.saveConfiguration();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Plugin getInstance() { return instance; }
    public NationManager getNationManager() { return manager; }
}
