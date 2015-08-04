package net.craftersland.games.money;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import net.craftersland.games.money.database.AccountDatabaseInterface;
import net.craftersland.games.money.database.DatabaseManagerInterface;
import net.craftersland.games.money.database.DatabaseManagerMysql;
import net.craftersland.games.money.database.MoneyMysqlInterface;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Money extends JavaPlugin {
	
	public static Logger log;
	public static Economy econ = null;
	public HashMap<UUID, Double> playersBalance = new HashMap<UUID, Double>();
	public HashMap<String, Boolean> playersSync = new HashMap<String, Boolean>();
	
	private ConfigurationHandler configurationHandler;
	private DatabaseManagerInterface databaseManager;
	private AccountDatabaseInterface<Double> moneyDatabaseInterface;
	private boolean enabled = false;
	
	@Override
    public void onEnable(){
    	log = getLogger();
    	log.info("Loading MysqlEconomyBridge v"+getDescription().getVersion()+"... ");
    	
    	//Create MysqlEconomyBridge folder
    	(new File("plugins"+System.getProperty("file.separator")+"MysqlEconomyBridge")).mkdir();
    	
    	
    	//Setup Vault for economy
        if (!setupEconomy() ) {
            log.severe("Warning! - Vault installed? If yes Economy system installed?");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    	
    	//Load Configuration
        configurationHandler = new ConfigurationHandler(this);
        
        //Setup Database
        	log.info("Using MySQL as Datasource...");
        	databaseManager = new DatabaseManagerMysql(this);
        	moneyDatabaseInterface = new MoneyMysqlInterface(this);
        	
        	if (databaseManager.getConnection() == null)
        	{
        		getServer().getPluginManager().disablePlugin(this);
                return;
        	}
          
        //Register Listeners
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerListener(this), this);
    	
    	Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			public void run() {
				if (Bukkit.getOnlinePlayers().isEmpty() == true) return;
				for (Player player : Bukkit.getOnlinePlayers()) {
					playersBalance.put(player.getUniqueId(), econ.getBalance(player));
				}
			}
		}, 20L, 20L);
    	
    	//Start the data save task
    	dataSaveTask();
    	
    	enabled = true;
    	log.info("MysqlEconomyBridge has been successfully loaded!");
	}
	
	@Override
    public void onDisable() {
		if (playersBalance.isEmpty() == false) {
			log.info("Saving players data...");
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (playersBalance.get(player.getUniqueId()) != 0) {
					moneyDatabaseInterface.setBalance(player.getUniqueId(), playersBalance.get(player.getUniqueId()));
				}
			}
		}
		
		if (enabled == true) {
			//Closing database connection
			if (databaseManager.getConnection() != null) {
				log.info("Closing MySQL connection...");
				databaseManager.closeDatabase();
			}
		}
    	log.info("MysqlEconomyBridge has been disabled");
    }
	
	//Methods for setting up Vault
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        log.info("Using economy system: " + rsp.getProvider().getName());
        return econ != null;
    }
    
    //Getter for Database Interfaces
    public AccountDatabaseInterface<Double> getMoneyDatabaseInterface() {
    	return moneyDatabaseInterface;
    }
    
    public ConfigurationHandler getConfigurationHandler() {
		return configurationHandler;
	}
    
    public DatabaseManagerInterface getDatabaseManagerInterface() {
		return databaseManager;
	}
    
    private void dataSaveTask() {
    	
    	if (configurationHandler.getString("General.dataSaveTask.enabled").matches("true")) {
    		int configDelay = configurationHandler.getInteger("General.dataSaveTask.interval");
        	int delay = configDelay * 60;
        	
        	Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {

    			@Override
    			public void run() {
    				log.info("Saving online players data...");
    				for (Player x : Bukkit.getOnlinePlayers()) {
    					Double localBalance = econ.getBalance(x);
    					if (localBalance != 0) {
    						moneyDatabaseInterface.setBalance(x.getUniqueId(), localBalance);
    					}
    				}
    				log.info("Data save is complete!");
    			}
        		
        	}, delay * 20L, delay * 20L);
    	}
    }

}
