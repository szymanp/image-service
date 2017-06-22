package com.metapx.local_client.resources;

import java.sql.SQLException;

import com.metapx.local_client.cli.ClientEnvironment;
import com.metapx.local_client.commands.CommandRunnable;

import io.vertx.core.json.JsonObject;

public class JsonCommandRunner {
  private final ClientEnvironment baseEnv;
  private final boolean disconnect;

  /**
   * Creates a new JsonCommandRunner.
   * The Runner will handle its own environment, including connecting and disconnecting from the repository.
   */
  public JsonCommandRunner() {
    baseEnv = new ClientEnvironment();
    disconnect = true;
  }
  
  /**
   * Creates a new JsonCommandRunner.
   * The Runner will use the supplied environment. The repository connection must be handled externally.
   * @param env
   */
  public JsonCommandRunner(ClientEnvironment env) {
    baseEnv = env;
    disconnect = false;
  }
  
  public JsonObject run(CommandRunnable cmd) {
    final ResourceConsole console = new ResourceConsole();
    final ClientEnvironment env = baseEnv.setConsole(console);

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
      if (disconnect) {
        try {
          env.closeConnection();
        } catch (SQLException e) {
          // suppress
        }
      }
    }
  }
}
