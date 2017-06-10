package com.metapx.local_client.daemon;

import java.util.Optional;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.RecordParser;

public class DaemonVerticle extends AbstractVerticle {
  
  private final String delimeter = "\n";
  private int counter = 0;
  
  @Override
  public void start() throws Exception {
    super.start();
    
    System.out.println("START");
    System.out.flush();
    
    final AsyncInputStream input = new AsyncInputStream(vertx, context, System.in);
    final RecordParser parser = RecordParser.newDelimited(delimeter, buffer -> {
      final Optional<JsonObject> inputJson = parse(buffer);
      
      if (!inputJson.isPresent()) {
        System.out.println("IGNORED" + delimeter);
        System.out.flush();
        return;
      }

      final int id = ++counter;
      
      System.out.println("QUEUED " + counter + delimeter);
      System.out.flush();
      
      vertx.executeBlocking(f -> {
        try {
          Thread.sleep(1000);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        
        final JsonObject outputJson = new JsonObject();
        outputJson.put("success", true);
        outputJson.put("value", inputJson.get());
        f.complete(outputJson);
      }, false, result -> {
        if (result.succeeded()) {
          System.out.println("OK " + id + " " + result.result().toString() + delimeter);
        } else {
          System.out.println("FAILED " + id + delimeter);
        }
        System.out.flush();
      });
      
    });
    
    input.handler(buffer -> parser.handle(buffer));
  }
  
  private Optional<JsonObject> parse(Buffer buffer) {
    try {
      return Optional.of(buffer.toJsonObject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
