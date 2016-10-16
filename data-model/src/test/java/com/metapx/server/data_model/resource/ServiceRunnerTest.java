package com.metapx.server.data_model.resource;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.metapx.server.data_model.BaseDatabaseTest;
import com.metapx.server.data_model.domain.User;

public class ServiceRunnerTest extends BaseDatabaseTest {
  
  private ServiceRunner runner;
  
  @Before
  public void createServiceRunner() {
    runner = new ServiceRunner(new MockUrlResolver());
  }

  @Test
  public void testRead() {
    Resource<User> result = runner.read("1", this.dslContext);
    
    assertEquals("http://example.org/user/1", result.getCanonicalUrl());
    assertEquals("Administrator", result.getRepresentation().getHandle());
  }
}
