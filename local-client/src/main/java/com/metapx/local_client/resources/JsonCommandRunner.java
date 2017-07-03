package com.metapx.local_client.resources;

import java.sql.SQLException;

import com.metapx.local_client.cli.ClientEnvironment;
import com.metapx.local_client.cli.DerivedEnvironment;
import com.metapx.local_client.cli.DerivedEnvironment.DerivedEnvironmentConfiguration;
import com.metapx.local_client.commands.CommandRunnable;

import io.vertx.core.json.JsonObject;
import rx.Observable;
import rx.subjects.UnicastSubject;

public class JsonCommandRunner {
  private final ClientEnvironment baseEnv;
  private final boolean disconnect;

  /**
   * Creates a new JsonCommandRunner.
   * The Runner will handle its own environment, including connecting and disconnecting from the repository.
   */
  public JsonCommandRunner() {
    baseEnv = ClientEnvironment.newInstance();
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
  
  public Observable<JsonObject> run(CommandRunnable cmd) {
    final UnicastSubject<Resource> resources = UnicastSubject.create();
    final ResourceConsole console = new ResourceConsole(resources);
    final ClientEnvironment env = new DerivedEnvironment(baseEnv, DerivedEnvironmentConfiguration.create().setConsole(console));
    
    return resources.doOnSubscribe(() -> {
        try {
          cmd.run(env);
          env.commit();
          resources.onCompleted();
  
        } catch (Exception e) {
          resources.onError(e);
  
        } finally {
          if (disconnect) {
            try {
              env.closeConnection();
            } catch (SQLException e) {
              // suppress
            }
          }
        }
      })
      .map(resource -> resource.build())
      .share();
  }
}
