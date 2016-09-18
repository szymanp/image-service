package com.metapx.server.ImageService;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.junit.Test;

import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class AuthenticationTest extends EndpointTest {

  @Override
  protected List<String> getVerticles() {
    return Arrays.asList(RestVerticle.class.getName());
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
  public void testAuthenticatedViaPassword(TestContext context) {
    final Async async = context.async();
    HttpClientRequest request = vertx.createHttpClient().get(port, "localhost", "/auth", response -> {
      context.assertEquals(200, response.statusCode());
      response.bodyHandler(body -> {
        context.assertEquals("Authenticated", body.toString());
        async.complete();
      });
    });
    request.putHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("test-user:123456".getBytes()));
    request.end();
  } 
  
  @Test
  public void testAuthenticatedViaSession(TestContext context) {
    final Async async = context.async();
    

    HttpClientRequest request = vertx.createHttpClient().get(port, "localhost", "/auth", response -> {
      context.assertEquals(200, response.statusCode());
      // This header should be available on the first login only.
      String sessionToken = response.headers().get("X-SessionToken");
      context.assertNotNull(sessionToken);

      HttpClientRequest request2 = vertx.createHttpClient().get(port, "localhost", "/auth", response2 -> {
        context.assertEquals(200, response2.statusCode());
        context.assertNull(response2.headers().get("X-SessionToken"));
        async.complete();
      });
      
      request2.putHeader("Authorization", "Basic " + sessionToken);
      request2.end();
    });
    
    request.putHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("test-user:123456".getBytes()));
    request.end();
  }
  
  @Test
  public void testLogout(TestContext context) {
    final Async async = context.async();
    
    // Login
    HttpClientRequest request = vertx.createHttpClient().get(port, "localhost", "/auth/any", response -> {
      context.assertEquals(200, response.statusCode());
      String sessionToken = response.headers().get("X-SessionToken");
      context.assertNotNull(sessionToken);
      
      // Logout
      HttpClientRequest request2 = vertx.createHttpClient().get(port, "localhost", "/auth/logout", response2 -> {
        context.assertEquals(204, response2.statusCode());
        context.assertNull(response2.headers().get("X-SessionToken"));

        // Verify that the token is now invalid.
        HttpClientRequest request3 = vertx.createHttpClient().get(port, "localhost", "/auth/any", response3 -> {
          context.assertEquals(401, response3.statusCode());
          async.complete();
        });
        
        request3.putHeader("Authorization", "Basic " + sessionToken);
        request3.end();
      });
      
      request2.putHeader("Authorization", "Basic " + sessionToken);
      request2.end();
    });
    request.putHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("test-user:123456".getBytes()));
    request.end();
  } 
}
