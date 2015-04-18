package net.craftersland.games.money.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import net.craftersland.games.money.Money;

public class DatabaseManagerMysql implements DatabaseManagerInterface{
	
	private Connection conn = null;
	  
	  // Hostname
	  private String dbHost;
	 
	  // Port -- Standard: 3306
	  private String dbPort;
	 
	  // Databankname
	  private String database;
	 
	  // Databank username
	  private String dbUser;
	 
	  // Databank password
	  private String dbPassword;

	private Money money;
	
	public DatabaseManagerMysql(Money money) {
		this.money = money;
		
		setupDatabase();
	}
	
	@Override
	public boolean setupDatabase() {
		try {
       	 	//Load Drivers
            Class.forName("com.mysql.jdbc.Driver");
            
            dbHost = money.getConfigurationHandler().getString("database.mysql.host");
            dbPort = money.getConfigurationHandler().getString("database.mysql.port");
            database = money.getConfigurationHandler().getString("database.mysql.databaseName");
            dbUser = money.getConfigurationHandler().getString("database.mysql.user");
            dbPassword = money.getConfigurationHandler().getString("database.mysql.password");
            
            //Connect to database
            conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
                + dbPort + "/" + database + "?" + "user=" + dbUser + "&"
                + "password=" + dbPassword);
           
          } catch (ClassNotFoundException e) {
            //System.out.println("Could not locate drivers!");
            Money.log.severe("Could not locate drivers for mysql!");
            return false;
          } catch (SQLException e) {
            //System.out.println("Could not connect");
            Money.log.severe("Could not connect to mysql database!");
            money.getPluginLoader().disablePlugin(money);
            return false;
          }
		
		//Create tables if needed
	      Statement query;
	      try {
	        query = conn.createStatement();
	        
	        String accounts = "CREATE TABLE IF NOT EXISTS `meb_accounts` (id int(10) AUTO_INCREMENT, player_name varchar(50) NOT NULL UNIQUE, balance DOUBLE(30,2) NOT NULL, PRIMARY KEY(id));";
	        query.executeUpdate(accounts);
	      } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	      }
      Money.log.info("Mysql has been set up!");
		return true;
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	@Override
	public boolean closeDatabase() {
		try {
			conn.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}
