package wcorrupt.aeternum;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Aeternum extends JavaPlugin {
    private static Aeternum instance;
    private FileConfiguration warpsConfig;
    private File warpsFile;
    private FileConfiguration kitsConfig;
    private File kitsFile;

    @Override
    public void onEnable() {
        instance = this;

        LuckPerms luckPerms = LuckPermsProvider.get();
        saveDefaultConfig();

        // Register event listeners and commands
        getServer().getPluginManager().registerEvents(new NameSanitizer(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this, luckPerms), this);

        registerCommand("fly", new FlyCommand());
        registerCommand("flyspeed", new FlySpeedCommand());
        registerCommand("itemname", new ItemNameCommand());

        FreezeCommand freezeCommand = new FreezeCommand(this);
        registerCommand("freeze", freezeCommand);
        registerCommand("unfreeze", freezeCommand);
        getServer().getPluginManager().registerEvents(freezeCommand, this);



        // Initialize and load the warps configuration
        createWarpsConfig();

        // Register warp commands and their tab completers
        this.getCommand("createwarp").setExecutor(new CreateWarpCommand(this));
        this.getCommand("warp").setExecutor(new WarpCommand(this));
        this.getCommand("deletewarp").setExecutor(new DeleteWarpCommand(this));
        this.getCommand("warp").setTabCompleter(new WarpTabCompleter(this));
        this.getCommand("deletewarp").setTabCompleter(new WarpTabCompleter(this));

        // Initialize and load the kits configuration
        createKitsConfig();

        // Register kit commands and their tab completers
        this.getCommand("createkit").setExecutor(new CreateKitCommand(this));
        this.getCommand("kit").setExecutor(new KitCommand(this));
        this.getCommand("deletekit").setExecutor(new DeleteKitCommand(this));
        this.getCommand("kit").setTabCompleter(new KitTabCompleter(this));
        this.getCommand("deletekit").setTabCompleter(new KitTabCompleter(this));

        // Additional existing commands and listeners
        this.getCommand("giveinfcart").setExecutor(new GiveInfCartCommand(this));
        getServer().getPluginManager().registerEvents(new CartPlaceListener(this), this);

        // Register the spawn command
        wcorrupt.aeternum.SpawnCommand spawnCommand = new wcorrupt.aeternum.SpawnCommand(this);
        registerCommand("spawn", spawnCommand);
        registerCommand("spawnadmin", spawnCommand);
        getServer().getPluginManager().registerEvents(spawnCommand, this);

        // Register the ShieldDisabler command and event listener
        ShieldDisabler shieldDisabler = new ShieldDisabler(this);
        this.getCommand("giveshieldbreaker").setExecutor(shieldDisabler);
        getServer().getPluginManager().registerEvents(shieldDisabler, this);

        // Register the TrashCommand and event listener
        TrashCommand trashCommand = new TrashCommand(this);
        this.getCommand("trash").setExecutor(trashCommand);
        getServer().getPluginManager().registerEvents(trashCommand, this);

        // Register the AFKRoomManager
        AFKRoomManager afkRoomManager = new AFKRoomManager(this);
        this.getCommand("setafkitem").setExecutor(afkRoomManager);

        // Register the PortalManager
        PortalManager portalManager = new PortalManager(this);
        this.getCommand("portalwand").setExecutor(portalManager);
    }

    @Override
    public void onDisable() {
        // Save configurations on disable
        if (warpsConfig != null) {
            saveWarpsConfig();
        }
        if (kitsConfig != null) {
            saveKitsConfig();
        }
    }

    public static Aeternum getInstance() {
        return instance;
    }

    private void registerCommand(String name, Object executor) {
        if (getCommand(name) == null) {
            getLogger().severe("Command /" + name + " is not registered in plugin.yml!");
        } else {
            if (executor instanceof CommandExecutor) {
                getCommand(name).setExecutor((CommandExecutor) executor);
            }
            if (executor instanceof TabCompleter) {
                getCommand(name).setTabCompleter((TabCompleter) executor);
            }
        }
    }

    private void createWarpsConfig() {
        warpsFile = new File(getDataFolder(), "warps.yml");
        if (!warpsFile.exists()) {
            warpsFile.getParentFile().mkdirs();
            try {
                warpsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
    }

    public void saveWarpsConfig() {
        try {
            if (warpsConfig != null && warpsFile != null) {
                warpsConfig.save(warpsFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getWarpsConfig() {
        return this.warpsConfig;
    }

    private void createKitsConfig() {
        kitsFile = new File(getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            kitsFile.getParentFile().mkdirs();
            try {
                kitsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
    }

    public void saveKitsConfig() {
        try {
            if (kitsConfig != null && kitsFile != null) {
                kitsConfig.save(kitsFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadKitsConfig() {
        if (kitsFile == null) {
            kitsFile = new File(getDataFolder(), "kits.yml");
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
    }

    public FileConfiguration getKitsConfig() {
        if (kitsConfig == null) {
            reloadKitsConfig();
        }
        return kitsConfig;
    }
}
