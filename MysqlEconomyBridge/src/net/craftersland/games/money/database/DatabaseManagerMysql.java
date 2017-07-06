package net.craftersland.games.money.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.craftersland.games.money.Money;


public class DatabaseManagerMysql {
	
	private Connection conn = null;

	private Money money;
	
	public DatabaseManagerMysql(Money money) {
		this.money = money;
		connectToDatabase();
		setupDatabase();
	}
	
	public void connectToDatabase() {
		Money.log.info("Connecting to the database...");
		try {
       	 	//Load Drivers
            Class.forName("com.mysql.jdbc.Driver");
            
            //Connect to database
            conn = DriverManager.getConnection("jdbc:mysql://" + money.getConfigurationHandler().getString("database.mysql.host") + ":" + money.getConfigurationHandler().getString("database.mysql.port") + "/" + money.getConfigurationHandler().getString("database.mysql.databaseName") + "?" + "user=" + money.getConfigurationHandler().getString("database.mysql.user") + "&" + "password=" + money.getConfigurationHandler().getString("database.mysql.password"));
           
          } catch (ClassNotFoundException e) {
        	  Money.log.severe("Could not locate drivers for mysql! Error: " + e.getMessage());
            return;
          } catch (SQLException e) {
        	  Money.log.severe("Could not connect to mysql database! Error: " + e.getMessage());
            return;
          }
		Money.log.info("Database connection successful!");
	}
	
	public void setupDatabase() {		
		  //Create tables if needed
		  PreparedStatement query = null;
	      try {	        
	        String data = "CREATE TABLE IF NOT EXISTS `" + money.getConfigurationHandler().getString("database.mysql.tableName") + "` (id int(10) AUTO_INCREMENT, player_name varchar(50) NOT NULL UNIQUE, balance DOUBLE(30,2) NOT NULL, PRIMARY KEY(id));";
	        query = conn.prepareStatement(data);
	        query.execute();
	      } catch (SQLException e) {
	    	  Money.log.severe("Error creating tables! Error: " + e.getMessage());
	    	  e.printStackTrace();
	      } finally {
	    	  try {
					if (query != null) {
						query.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
	      }
	}
	
	public Connection getConnection() {
		checkConnection();
		return conn;
	}
	
	public void closeConnection() {
		try {
			Money.log.info("Closing database connection...");
			conn.close();
			conn = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void checkConnection() {
		try {
			if (conn == null) {
				Money.log.warning("Connection failed. Reconnecting...");
				reConnect();
			}
			if (!conn.isValid(3)) {
				Money.log.warning("Connection is idle or terminated. Reconnecting...");
				reConnect();
			}
			if (conn.isClosed() == true) {
				Money.log.warning("Connection is closed. Reconnecting...");
				reConnect();
			}
		} catch (Exception e) {
			Money.log.severe("Could not reconnect to Database! Error: " + e.getMessage());
		}
	}
	
	public boolean reConnect() {
		try {            
            long start = 0;
			long end = 0;
			
		    start = System.currentTimeMillis();
		    Money.log.info("Attempting to establish a connection to the MySQL server!");
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + money.getConfigurationHandler().getString("database.mysql.host") + ":" + money.getConfigurationHandler().getString("database.mysql.port") + "/" + money.getConfigurationHandler().getString("database.mysql.databaseName") + "?" + "user=" + money.getConfigurationHandler().getString("database.mysql.user") + "&" + "password=" + money.getConfigurationHandler().getString("database.mysql.password"));
		    end = System.currentTimeMillis();
		    Money.log.info("Connection to MySQL server established!");
		    Money.log.info("Connection took " + ((end - start)) + "ms!");
            return true;
		} catch (Exception e) {
			Money.log.severe("Error re-connecting to the database! Error: " + e.getMessage());
			return false;
		}
	}

}
