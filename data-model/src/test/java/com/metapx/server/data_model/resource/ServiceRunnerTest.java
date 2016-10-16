package com.metapx.server.data_model.resource;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.metapx.server.data_model.BaseDatabaseTest;
import com.metapx.server.data_model.domain.User;
import com.metapx.server.data_model.resource.infrastructure.CrudError;
import com.metapx.server.data_model.resource.infrastructure.Resource;
import com.metapx.server.data_model.resource.infrastructure.ServiceRunner;

public class ServiceRunnerTest extends BaseDatabaseTest {
  
  private ServiceRunner runner;
  
  @Before
  public void createServiceRunner() {
    runner = new ServiceRunner(new MockUrlResolver());
  }

  @Test
  public void testReadExisting() {
    Resource<User> result = runner.read("1", this.dslContext);
    
    assertEquals("http://example.org/user/1", result.getCanonicalUrl());
    assertEquals("Administrator", result.getRepresentation().getHandle());
  }

  @Test(expected = CrudError.class)
  public void testReadInvalidFormat() {
    runner.read("qwe", this.dslContext);
  }

  @Test(expected = CrudError.class)
  public void testReadNotFound() {
    runner.read("234", this.dslContext);
  }
}
