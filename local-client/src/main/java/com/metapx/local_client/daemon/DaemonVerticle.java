package com.metapx.local_client.daemon;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.rvesse.airline.parser.ParseResult;
import com.metapx.local_client.cli.Client;
import com.metapx.local_client.cli.ClientEnvironment;
import com.metapx.local_client.commands.CommandRunnable;
import com.metapx.local_client.resources.JsonCommandRunner;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.core.Future;

public class DaemonVerticle extends AbstractVerticle {
  
  private final ClientEnvironment env = new ClientEnvironment();
  private final com.github.rvesse.airline.Cli<CommandRunnable> cli = new com.github.rvesse.airline.Cli<CommandRunnable>(Client.class);
  private final String delimeter = "\n";
  private int counter = 0;
  private AsyncInputStream input;
  
  @Override
  public void start() throws Exception {
    super.start();
    
    input = new AsyncInputStream(vertx, System.in);
    
    System.out.println("START");
    System.out.flush();
    
    final RecordParser parser = RecordParser.newDelimited(delimeter, buffer -> {
      final Optional<JsonObject> inputJson = parse(buffer);
      
      if (!inputJson.isPresent()) {
        System.out.println("IGNORED" + delimeter);
        System.out.flush();
        return;
      }

      final int id = ++counter;
      final Handler<AsyncResult<JsonObject>> resultHandler = (result) -> {
        if (result.succeeded()) {
          System.out.println("OK " + id + " " + result.result().toString() + delimeter);
        } else {
          final JsonObject exception = new JsonObject();
          exception.put("message", result.cause().getMessage());
          exception.put("class", result.cause().getClass().getName());
          System.out.println("FAILED " + id + " " + exception.toString() + delimeter);
        }
        System.out.flush();
      };

      System.out.println("QUEUED " + counter + delimeter);
      System.out.flush();

      try {
        dispatch(inputJson.get(), resultHandler);
      } catch (Exception e) {
        resultHandler.handle(Future.<JsonObject>failedFuture(e));
      }
    });
    
    input.handler(buffer -> parser.handle(buffer));
  }
  
  @Override
  public void stop() throws Exception {
    super.stop();

    input.close();
    env.closeConnection();
  }
  
  private void dispatch(JsonObject request, Handler<AsyncResult<JsonObject>> resultHandler) {
    final String command = request.getString("command");
    
    if (command.equals("exit")) {
      resultHandler.handle(Future.succeededFuture(new JsonObject()));
      vertx.close();
    } else if (command.equals("echo")) {
      final JsonObject result = new JsonObject();
      result.put("value", request);
      resultHandler.handle(Future.succeededFuture(result));
    } else {
      final ParseResult<CommandRunnable> parseResult = cli.parseWithResult(splitArgs(command));
      final JsonCommandRunner runner = new JsonCommandRunner(env);
      
      if (parseResult.wasSuccessful()) {
        final CommandRunnable cmd = parseResult.getCommand();
        vertx.<JsonObject>executeBlocking(
          future -> future.complete(runner.run(cmd)),
          result -> resultHandler.handle(result)
        );
      } else {
        throw new RuntimeException("Invalid command.");
      }
    }
  }
  
  private Optional<JsonObject> parse(Buffer buffer) {
    try {
      return Optional.of(buffer.toJsonObject());
    } catch (Exception e) {
      return Optional.empty();
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
