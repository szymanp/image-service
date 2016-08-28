package com.metapx.server.ImageService;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.metapx.server.data_model.domain.User;

import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class UserEndpointTest extends EndpointTest {

  @Override
  protected List<String> getVerticles() {
    return Arrays.asList(RestVerticle.class.getName());
  }

  @Test
  public void readAdministrator(TestContext context) {
    final Async async = context.async();

    vertx.createHttpClient().getNow(port, "localhost", "/users/1", response -> {
      response.handler(body -> {
        context.assertTrue(body.length() > 0, "Body is empty");
        context.assertTrue(response.getHeader("content-type").equalsIgnoreCase("application/json; charset=utf-8"), 
            "Invalid content-type");
        
        User user = Json.decodeValue(body.toString(), User.class);
        context.assertEquals(1, user.getId());
        context.assertEquals("Administrator", user.getHandle());
        
        async.complete();
      });
    });
}
}
