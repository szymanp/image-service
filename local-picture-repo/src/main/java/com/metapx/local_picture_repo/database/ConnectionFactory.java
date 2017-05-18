package com.metapx.local_picture_repo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {

	private static final String DB_DRIVER = "org.h2.Driver";
	private static final String DB_CONNECTION_PREFIX = "jdbc:h2:";
	private static final String DB_USER = "";
	private static final String DB_PASSWORD = "";

	public static Connection newConnection(String filename) throws ClassNotFoundException, SQLException {
		Class.forName(DB_DRIVER);

		Connection connection = DriverManager.getConnection(DB_CONNECTION_PREFIX + filename, DB_USER, DB_PASSWORD);
		connection.setAutoCommit(false);
		
		return connection;
	}
	
	public static Connection newInMemoryConnection() throws Exception {
		Class.forName(DB_DRIVER);

		Connection connection = DriverManager.getConnection("jdbc:h2:mem:", DB_USER, DB_PASSWORD);
		connection.setAutoCommit(false);
		
		new DatabaseBuilder(connection).build();
		
		return connection;
	}
}
