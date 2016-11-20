package net.craftersland.eco.bridge.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.craftersland.eco.bridge.Eco;

public class MysqlSetup {
	
	private Connection conn = null;
	private Eco eco;
	
	public MysqlSetup(Eco eco) {
		this.eco = eco;
		connectToDatabase();
		setupDatabase();
		updateTables();
	}
	
	public void connectToDatabase() {
		Eco.log.info("Connecting to the database...");
		try {
       	 	//Load Drivers
            Class.forName("com.mysql.jdbc.Driver");
            
            //Connect to database
            conn = DriverManager.getConnection("jdbc:mysql://" + eco.getConfigHandler().getString("database.mysql.host") + ":" + eco.getConfigHandler().getString("database.mysql.port") + "/" + eco.getConfigHandler().getString("database.mysql.databaseName") + "?" + "user=" + eco.getConfigHandler().getString("database.mysql.user") + "&" + "password=" + eco.getConfigHandler().getString("database.mysql.password"));
           
          } catch (ClassNotFoundException e) {
        	  Eco.log.severe("Could not locate drivers for mysql! Error: " + e.getMessage());
            return;
          } catch (SQLException e) {
        	  Eco.log.severe("Could not connect to mysql database! Error: " + e.getMessage());
            return;
          }
		Eco.log.info("Database connection successful!");
	}
	
	public void setupDatabase() {
		//Create tables if needed
		PreparedStatement query = null;
	      try {	        
	        String data = "CREATE TABLE IF NOT EXISTS `" + eco.getConfigHandler().getString("database.mysql.dataTableName") + "` (id int(10) AUTO_INCREMENT, player_uuid varchar(50) NOT NULL UNIQUE, player_name varchar(50) NOT NULL, money double(30,2) NOT NULL, sync_complete varchar(5) NOT NULL, last_seen varchar(30) NOT NULL, PRIMARY KEY(id));";
	        query = conn.prepareStatement(data);
	        query.execute();
	      } catch (SQLException e) {
	        e.printStackTrace();
	        Eco.log.severe("Error creating tables! Error: " + e.getMessage());
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
	
	public void checkConnection() {
		try {
			if (conn == null) {
				Eco.log.warning("Connection failed. Reconnecting...");
				reConnect();
			}
			if (!conn.isValid(3)) {
				Eco.log.warning("Connection is idle or terminated. Reconnecting...");
				reConnect();
			}
			if (conn.isClosed() == true) {
				Eco.log.warning("Connection is closed. Reconnecting...");
				reConnect();
			}
		} catch (Exception e) {
			Eco.log.severe("Could not reconnect to Database! Error: " + e.getMessage());
		}
	}
	
	public boolean reConnect() {
		try {            
            long start = 0;
			long end = 0;
			
		    start = System.currentTimeMillis();
		    Eco.log.info("Attempting to establish a connection to the MySQL server!");
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + eco.getConfigHandler().getString("database.mysql.host") + ":" + eco.getConfigHandler().getString("database.mysql.port") + "/" + eco.getConfigHandler().getString("database.mysql.databaseName") + "?" + "user=" + eco.getConfigHandler().getString("database.mysql.user") + "&" + "password=" + eco.getConfigHandler().getString("database.mysql.password"));
		    end = System.currentTimeMillis();
		    Eco.log.info("Connection to MySQL server established!");
		    Eco.log.info("Connection took " + ((end - start)) + "ms!");
            return true;
		} catch (Exception e) {
			Eco.log.severe("Error re-connecting to the database! Error: " + e.getMessage());
			return false;
		}
	}
	
	public void closeConnection() {
		try {
			Eco.log.info("Closing database connection...");
			conn.close();
			conn = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void updateTables() {
		if (conn != null) {
			DatabaseMetaData md = null;
	    	ResultSet rs1 = null;
	    	PreparedStatement query1 = null;
	    	try {
	    		md = conn.getMetaData();
	    		rs1 = md.getColumns(null, null, eco.getConfigHandler().getString("database.mysql.dataTableName"), "sync_complete");
	            if (rs1.next()) {
			    	
			    } else {
			        String data = "ALTER TABLE `" + eco.getConfigHandler().getString("database.mysql.dataTableName") + "` ADD sync_complete varchar(5) NOT NULL DEFAULT 'true';";
			        query1 = conn.prepareStatement(data);
			        query1.execute();
			    }
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	} finally {
	    		try {
	    			if (query1 != null) {
	    				query1.close();
	    			}
	    			if (rs1 != null) {
	    				rs1.close();
	    			}
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    	}
		}
	}

}
