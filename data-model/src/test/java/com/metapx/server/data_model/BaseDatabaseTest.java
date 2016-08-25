package com.metapx.server.data_model;

import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=TestConfiguration.class)
abstract public class BaseDatabaseTest {
  
  @Autowired
  protected DSLContext dslContext;

  @Before
  public void setUp() {
    dslContext.connection(c -> c.setAutoCommit(false));
  }
  
  @After
  public void tearDown() {
    dslContext.connection(c -> c.rollback());
  }
}
