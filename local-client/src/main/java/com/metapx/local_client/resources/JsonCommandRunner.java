package com.metapx.local_client.resources;

import java.sql.SQLException;

import com.metapx.local_client.cli.ClientEnvironment;
import com.metapx.local_client.commands.CommandRunnable;

import io.vertx.core.json.JsonObject;

public class JsonCommandRunner {
  
  public JsonObject run(CommandRunnable cmd) {
    final ResourceConsole console = new ResourceConsole();
    final ClientEnvironment env = new ClientEnvironment(console);

    try {
      cmd.run(env);
      env.commit();
      return console.getResult();

    } catch (Exception e) {
      final JsonObject exceptionResponse = new JsonObject();
      exceptionResponse.put("type", "exception");
      exceptionResponse.put("resource", new ExceptionResource(e).build());
      return exceptionResponse;

    } finally {
      try {
        env.closeConnection();
      } catch (SQLException e) {
        // suppress
      }
    }
  }
}
