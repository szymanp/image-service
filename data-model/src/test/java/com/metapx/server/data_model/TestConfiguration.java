package com.metapx.server.data_model;

import java.sql.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DatabaseProperties.class})
public class TestConfiguration {

  @Autowired
  private DatabaseProperties db;
  
  @Bean
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(db.getConnectionURL(), db.getUsername(), db.getPassword());
  }
  
  @Bean
  public DSLContext getDSLContext() throws SQLException {
    return DSL.using(getConnection(), SQLDialect.POSTGRES);
  }
}
