package net.craftersland.games.money;

import java.io.File;

public class ConfigurationHandler {
	
	private Money money;

	public ConfigurationHandler(Money money) {
		this.money = money;
		loadConfig();
	}
	
	public void loadConfig() {
		File pluginFolder = new File("plugins" + System.getProperty("file.separator") + Money.pluginName);
		if (pluginFolder.exists() == false) {
    		pluginFolder.mkdir();
    	}
		File configFile = new File("plugins" + System.getProperty("file.separator") + Money.pluginName + System.getProperty("file.separator") + "config.yml");
		if (configFile.exists() == false) {
    		Money.log.info("No config file found! Creating new one...");
    		money.saveDefaultConfig();
		}
    	try {
    		Money.log.info("Loading the config file...");
			money.getConfig().load(configFile);
    	} catch (Exception e) {
    		Money.log.severe("Could not load the config file! You need to regenerate the config! Error: " + e.getMessage());
			e.printStackTrace();
    	}
	}
	
	public String getString(String key) {
		if (!money.getConfig().contains(key)) {
			money.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Money.pluginName + " folder! (Try generating a new one by deleting the current)");
			return "errorCouldNotLocateInConfigYml:" + key;
		} else {
			return money.getConfig().getString(key);
		}
	}
	
	public Integer getInteger(String key) {
		if (!money.getConfig().contains(key)) {
			money.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Money.pluginName + " folder! (Try generating a new one by deleting the current)");
			return null;
		} else {
			return money.getConfig().getInt(key);
		}
	}
	
	public Boolean getBoolean(String key) {
		if (!money.getConfig().contains(key)) {
			money.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Money.pluginName + " folder! (Try generating a new one by deleting the current)");
			return null;
		} else {
			return money.getConfig().getBoolean(key);
		}
	}

}
