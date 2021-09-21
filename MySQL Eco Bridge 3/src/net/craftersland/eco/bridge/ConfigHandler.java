package net.craftersland.eco.bridge;

import java.io.File;

public class ConfigHandler {
	
	private Eco eco;
	
	public ConfigHandler(Eco eco) {
		this.eco = eco;
		loadConfig();
	}
	
	public void loadConfig() {
		File pluginFolder = new File("plugins" + System.getProperty("file.separator") + Eco.pluginName);
		if (pluginFolder.exists() == false) {
    		pluginFolder.mkdir();
    	}
		File configFile = new File("plugins" + System.getProperty("file.separator") + Eco.pluginName + System.getProperty("file.separator") + "resources/config.yml");
		if (configFile.exists() == false) {
			Eco.log.info("No config file found! Creating new one...");
			eco.saveDefaultConfig();
		}
    	try {
    		Eco.log.info("Loading the config file...");
    		eco.getConfig().load(configFile);
    	} catch (Exception e) {
    		Eco.log.severe("Could not load the config file! You need to regenerate the config! Error: " + e.getMessage());
			e.printStackTrace();
    	}
	}
	
	public String getString(String key) {
		if (!eco.getConfig().contains(key)) {
			eco.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Eco.pluginName + " folder! (Try generating a new one by deleting the current)");
			return "errorCouldNotLocateInConfigYml:" + key;
		} else {
			return eco.getConfig().getString(key);
		}
	}
	
	public Integer getInteger(String key) {
		if (!eco.getConfig().contains(key)) {
			eco.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Eco.pluginName + " folder! (Try generating a new one by deleting the current)");
			return null;
		} else {
			return eco.getConfig().getInt(key);
		}
	}
	
	public Boolean getBoolean(String key) {
		if (!eco.getConfig().contains(key)) {
			eco.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Eco.pluginName + " folder! (Try generating a new one by deleting the current)");
			return null;
		} else {
			return eco.getConfig().getBoolean(key);
		}
	}

}
