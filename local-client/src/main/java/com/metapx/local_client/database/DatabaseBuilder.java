package com.metapx.local_client.database;

import java.sql.Connection;
import java.sql.Statement;

public final class DatabaseBuilder {

  final private Connection connection;
  
  public static void main( String[] args ) throws Exception {
    DatabaseBuilder.buildFile("test");
  }

  public static void buildFile(String filename) throws Exception {
    Connection connection = ConnectionFactory.newConnection(filename);
    try {
      DatabaseBuilder b = new DatabaseBuilder(connection);
      b.build();
    } catch (Exception e) {
      throw e;
    } finally {
      connection.close();
    }
  }

  public DatabaseBuilder(Connection connection) {
    this.connection = connection;
  }

  public void build() throws Exception {
    Statement stmt = connection.createStatement();

    stmt.execute("CREATE TABLE SYSTEM(id int primary key, schema_version int)");
    stmt.execute("CREATE TABLE FOLDER("
      + "id int auto_increment primary key,"
      + "name varchar(255),"
      + "parent_id int)");
    stmt.execute("CREATE TABLE FILE("
      + "id int auto_increment primary key,"
      + "folder_id int,"
      + "name varchar(255),"
      + "file_id varchar(64),"  // git-metadata ID of the file
      + "hash varchar(64))");
    stmt.execute("CREATE UNIQUE INDEX FILE_01 ON FILE (folder_id, name)");
    stmt.execute("INSERT INTO SYSTEM(id, schema_version) VALUES(1, 1)");

    connection.commit();
  }
}
