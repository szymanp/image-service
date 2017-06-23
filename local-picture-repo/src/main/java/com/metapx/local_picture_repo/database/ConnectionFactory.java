package com.metapx.local_picture_repo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.h2.jdbcx.JdbcConnectionPool;

public final class ConnectionFactory {
  
	private static final String DB_DRIVER = "org.h2.Driver";
	private static final String DB_CONNECTION_PREFIX = "jdbc:h2:";
	private static final String DB_USER = "";
	private static final String DB_PASSWORD = "";

  static {
    try {
      Class.forName(DB_DRIVER);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

	public static Connection newConnection(String filename) throws SQLException {
		Connection connection = DriverManager.getConnection(DB_CONNECTION_PREFIX + filename, DB_USER, DB_PASSWORD);
		connection.setAutoCommit(false);
		
		return connection;
	}
	
	public static Connection newInMemoryConnection() throws Exception {
		Connection connection = DriverManager.getConnection("jdbc:h2:mem:", DB_USER, DB_PASSWORD);
		connection.setAutoCommit(false);
		
		new DatabaseBuilder(connection).build();
		
		return connection;
	}
	
	public static JdbcConnectionPool newConnectionPool(String filename) {
	  return JdbcConnectionPool.create(DB_CONNECTION_PREFIX + filename, DB_USER, DB_PASSWORD);
	}
	
	/**
	 * A single application usually uses only a single picture repository.
	 * 
	 * This class help registering an application-wide connection pool for accessing the repository database.
	 */
	public static class SharedConnectionPool {
	  private static final Logger logger = Logger.getLogger(SharedConnectionPool.class.getName());
	  private static JdbcConnectionPool defaultPool;
	  private static Supplier<JdbcConnectionPool> supplier;
	  
	  public static JdbcConnectionPool getConnectionPool() {
	    if (defaultPool == null && supplier != null) {
	      synchronized(supplier) {
	        logger.info("Creating connection pool.");
	        defaultPool = supplier.get();
	      }
	    }
	    if (defaultPool == null) {
	      throw new RuntimeException("No picture repository database pool is set.");
	    }
	    return defaultPool;
	  }
	  
	  public static void create(String filename) {
	    defaultPool = newConnectionPool(filename);
	  }
	  
	  public static void create(Supplier<JdbcConnectionPool> supplier) {
	    SharedConnectionPool.supplier = supplier;
	  }
	  
	  public static void close() {
	    if (defaultPool != null) {
	      logger.info("Disposing of connection pool.");
	      defaultPool.dispose();
	      defaultPool = null;
	    }
	  }
	}
}
