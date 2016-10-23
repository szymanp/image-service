package com.metapx.server.data_model.resource;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.metapx.server.data_model.BaseDatabaseTest;
import com.metapx.server.data_model.domain.User;
import com.metapx.server.data_model.resource.infrastructure.CrudError;
import com.metapx.server.data_model.resource.infrastructure.Resource;
import com.metapx.server.data_model.resource.infrastructure.ServiceRunner;

import io.vertx.core.json.JsonObject;

public class ServiceRunnerTest extends BaseDatabaseTest {
  
  private ServiceRunner<User, Integer> runner;
  
  @Before
  public void createServiceRunner() {
    runner = ServiceRunner.create(new UserService(), new MockUrlResolver());
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
    runner.read("234", dslContext);
  }
  
  @Test(expected = CrudError.class)
  public void testCreateWithValidationFailure() {
    String user = "{ \"handle\": \"Administrator\" }";
    runner.create(user, dslContext);
  }
  
  @Test
  public void testCreate() {
    JsonObject user = new JsonObject();
    user.put("handle", "JohnDoe");
    user.put("displayName", "John Doe");
    user.put("emailAddress", "john@example.org");
    user.put("password", "123456");
    
    Resource<User> result = runner.create(user.encode(), dslContext);
    
    assertNotNull(result.getRepresentation());
    assertNotNull(result.getRepresentation().getId());
    assertEquals("http://example.org/user/" + result.getRepresentation().getId(), result.getCanonicalUrl());
  }
  
  @Test
  public void testUpdate() {
    JsonObject user = new JsonObject();
    user.put("displayName", "John Doe");
    
    Resource<User> result = runner.update("1", user.encode(), dslContext);
    
    assertNotNull(result.getRepresentation());
    assertEquals(1, result.getRepresentation().getId().intValue());
    assertEquals("John Doe", result.getRepresentation().getDisplayName());
    assertEquals("http://example.org/user/1", result.getCanonicalUrl());
  }
}
