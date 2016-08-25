package com.metapx.server.data_model;

import junit.framework.TestCase;

import java.util.List;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.metapx.server.data_model.domain.User;
import com.metapx.server.data_model.jooq.Tables;
import com.metapx.server.data_model.jooq.tables.records.*;

/**
 * Unit test for simple App.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=TestConfiguration.class)
public class AppTest extends TestCase {

  @Autowired
  private DSLContext dslContext;
  
  /**
   * Rigourous Test :-)
   */
  @Test
  public void testApp() {
    /*
    User user = new User();
    user.setDisplayName("Test user");
    user.setHandle("Test");
    user.create(dslContext);
    */
    
    User user = new User();
    assertTrue(user.isNew());
    
    List<User> users = dslContext.select()
        .from(Tables.USERS)
        .fetchStream()
        .map(r -> new User((UsersRecord)r)).collect(Collectors.toList());
    
    assertFalse(users.get(0).isNew());
    System.out.println(users);
  }
}
