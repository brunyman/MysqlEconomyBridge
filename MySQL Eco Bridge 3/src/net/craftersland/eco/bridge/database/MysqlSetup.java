package net.craftersland.eco.bridge.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.craftersland.eco.bridge.Eco;

public class MysqlSetup {
	
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

	private Eco eco;
	
	public String dataTableName;
	
	public MysqlSetup(Eco eco) {
		this.eco = eco;
		
		dataTableName = eco.getConfigHandler().getString("database.mysql.dataTableName");
		
		setupDatabase();
		updateTables();
	}
	
	public boolean setupDatabase() {
		try {
       	 	//Load Drivers
            Class.forName("com.mysql.jdbc.Driver");
            
            dbHost = eco.getConfigHandler().getString("database.mysql.host");
            dbPort = eco.getConfigHandler().getString("database.mysql.port");
            database = eco.getConfigHandler().getString("database.mysql.databaseName");
            dbUser = eco.getConfigHandler().getString("database.mysql.user");
            dbPassword = eco.getConfigHandler().getString("database.mysql.password");
            
            String passFix = dbPassword.replaceAll("%", "%25");
            String passFix2 = passFix.replaceAll("\\+", "%2B");
            
            //Connect to database
            conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&" + "password=" + passFix2);
           
          } catch (ClassNotFoundException e) {
        	  Eco.log.severe("Could not locate drivers for mysql!");
            return false;
          } catch (SQLException e) {
        	  Eco.log.severe("Could not connect to mysql database!");
            return false;
          }
		
		//Create tables if needed
	      Statement query = null;
	      try {
	        query = conn.createStatement();
	        
	        String accounts = "CREATE TABLE IF NOT EXISTS `" + dataTableName + "` (id int(10) AUTO_INCREMENT, player_uuid varchar(50) NOT NULL UNIQUE, player_name varchar(50) NOT NULL, money double(30,2) NOT NULL, sync_complete varchar(5) NOT NULL, last_seen varchar(30) NOT NULL, PRIMARY KEY(id));";
	        query.executeUpdate(accounts);
	      } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	      } finally {
	    	  try {
	    		  if (query != null) {
	    			  query.close();
	    		  }
	    	  } catch (Exception e) {
	    		  e.printStackTrace();
	    	  }
	      }
      Eco.log.info("MySQL setup complete!");
		return true;
	}
	
	public Connection getConnection() {
		checkConnection();
		return conn;
	}
	
	public boolean checkConnection() {
		try {
			if (eco.isDisabling == false) {
				if (conn == null) {
					Eco.log.warning("Connection failed. Reconnecting...");
					if (reConnect() == true) return true;
					return false;
				}
				if (!conn.isValid(3)) {
					Eco.log.warning("Connection is idle or terminated. Reconnecting...");
					if (reConnect() == true) return true;
					return false;
				}
				if (conn.isClosed() == true) {
					Eco.log.warning("Connection is closed. Reconnecting...");
					if (reConnect() == true) return true;
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			Eco.log.severe("Could not reconnect to Database!");
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean reConnect() {
		try {
			dbHost = eco.getConfigHandler().getString("database.mysql.host");
            dbPort = eco.getConfigHandler().getString("database.mysql.port");
            database = eco.getConfigHandler().getString("database.mysql.databaseName");
            dbUser = eco.getConfigHandler().getString("database.mysql.user");
            dbPassword = eco.getConfigHandler().getString("database.mysql.password");
            
            String passFix = dbPassword.replaceAll("%", "%25");
            String passFix2 = passFix.replaceAll("\\+", "%2B");
            
            long start = 0;
			long end = 0;
			
		    start = System.currentTimeMillis();
		    Eco.log.info("Attempting to establish a connection to the MySQL server!");
		    Class.forName("com.mysql.jdbc.Driver");
		    conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&" + "password=" + passFix2);
		    end = System.currentTimeMillis();
		    Eco.log.info("Connection to MySQL server established!");
		    Eco.log.info("Connection took " + ((end - start)) + "ms!");
		    return true;
		} catch (Exception e) {
			Eco.log.severe("Could not connect to MySQL server! because: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean closeConnection() {
		try {
			conn.close();
			conn = null;
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
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
