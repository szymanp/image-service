package com.metapx.local_client.daemon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.rvesse.airline.parser.ParseResult;
import com.metapx.local_client.cli.Client;
import com.metapx.local_client.cli.ClientEnvironment;
import com.metapx.local_client.commands.CommandRunnable;
import com.metapx.local_client.resources.JsonCommandRunner;
import com.metapx.local_picture_repo.database.ConnectionFactory;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.RxHelper;
import rx.Observable;

public class DaemonVerticle extends AbstractVerticle {
  
  private final ClientEnvironment env = ClientEnvironment.newInstance();
  private final com.github.rvesse.airline.Cli<CommandRunnable> cli = new com.github.rvesse.airline.Cli<CommandRunnable>(Client.class);
  private StreamVerticle stream;
  
  @Override
  public void start() throws Exception {
    // Setup repository database connection.
    Client.configure();
    
    stream = new StreamVerticle(System.in, System.out);
    vertx.getDelegate().deployVerticle(stream);
    stream.messages()
      .subscribeOn(RxHelper.scheduler(vertx))
      .subscribe((message) -> {
        try {
          dispatch(message);
        } catch (Exception e) {
          message.reply().onError(e);
        }
      });
  }
  
  @Override
  public void stop() throws Exception {
    vertx.undeploy(stream.deploymentID());
    env.closeConnection();
    ConnectionFactory.SharedConnectionPool.close();
  }
  
  private void dispatch(Message message) {
    final String command = message.getBody().getString("command");
    
    if (command.equals("exit")) {
      message.reply().onCompleted();
      vertx.close();
    } else if (command.equals("echo")) {
      Observable
        .interval(1, TimeUnit.SECONDS)
        .take(10)
        .subscribe(
          (index) -> message.reply().onNext(new JsonObject().put("value", index)),
          (error) -> message.reply().onError(error),
          () -> message.reply().onCompleted()
        );
    } else {
      final ParseResult<CommandRunnable> parseResult = cli.parseWithResult(splitArgs(command));
      final JsonCommandRunner runner = new JsonCommandRunner(env);
      
      if (parseResult.wasSuccessful()) {
        final CommandRunnable cmd = parseResult.getCommand();
        runner.run(cmd)
          .subscribeOn(RxHelper.blockingScheduler(vertx))
          .subscribe(
            (json) -> message.reply().onNext(json),
            (error) -> message.reply().onError(error),
            () -> message.reply().onCompleted()
          );
      } else {
        throw new RuntimeException("Invalid command.");
      }
    }
  }
  
  private List<String> splitArgs(String input) {
    final List<String> result = new ArrayList<String>();
    final String regex = "\"([^\"]*)\"|(\\S+)";
    final Matcher m = Pattern.compile(regex).matcher(input);

    while (m.find()) {
      if (m.group(1) != null) {
        result.add(m.group(1));
      } else {
        result.add(m.group(2));
      }
    }
    return result;
  }
}
