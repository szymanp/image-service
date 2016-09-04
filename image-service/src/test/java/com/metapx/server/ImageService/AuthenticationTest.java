package com.metapx.server.ImageService;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.metapx.server.auth.PasswordManager;
import com.metapx.server.data_model.jooq.tables.records.UsersRecord;

import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class AuthenticationTest extends EndpointTest {

  @Override
  protected List<String> getVerticles() {
    return Arrays.asList(RestVerticle.class.getName());
  }
  
  @Before
  public void createUser() {
    // TODO This user is not visible in the RestVerticle due to transaction handling.
    //      The verticle is using another connection than the test.
    final UsersRecord user = new UsersRecord();
    user.setHandle("TestUser");
    user.setDisplayName("Test User");
    user.setEmailAddress("test@user.com");
    user.setActive(true);
    user.setSalt("salt");
    user.setPassword(new PasswordManager.Default().computeHash("123456", user.getSalt()));
    dsl.executeInsert(user);
  }

  @Test
  public void testAuthenticationRequired(TestContext context) {
    final Async async = context.async();
    vertx.createHttpClient().getNow(port, "localhost", "/auth", response -> {
      final String header = response.headers().get("WWW-Authenticate");
      context.assertNotNull(header);
      context.assertEquals("Basic realm=\"image-service\"", header);
      context.assertEquals(401, response.statusCode());
      async.complete();
    });
  }

  @Test
  public void testAuthenticated(TestContext context) {
    final Async async = context.async();
    HttpClientRequest request = vertx.createHttpClient().get(port, "localhost", "/auth", response -> {
      String header = response.headers().get("WWW-Authenticate");
      context.assertNotNull(header);
      context.assertEquals("Basic realm=\"image-service\"", header);
      context.assertEquals(200, response.statusCode());
      async.complete();
    });
    request.putHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("TestUser:123456".getBytes()));
    request.end();
  } 
}
