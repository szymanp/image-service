package com.metapx.local_client.daemon;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Optional;

import com.metapx.local_client.resources.ExceptionResource;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.rxjava.core.RxHelper;
import rx.Observable;
import rx.subjects.UnicastSubject;
import io.vertx.rxjava.core.AbstractVerticle;

public class StreamVerticle extends AbstractVerticle {
  private final InputStream in;
  private final PrintStream out;
  private final UnicastSubject<Message> messages = UnicastSubject.create();
  private final String delimeter = "\n";
  private int counter = 0;
  private AsyncInputStream input;
  
  public StreamVerticle(InputStream in, PrintStream out) {
    this.in = in;
    this.out = out;
  }

  public Observable<Message> messages() {
    return messages;
  }
  
  @Override
  public void start() throws Exception {
    input = new AsyncInputStream(vertx.getDelegate(), in);
    
    send("START");
    
    final RecordParser parser = RecordParser.newDelimited(delimeter, buffer -> {
      final Optional<JsonObject> inputJson = parse(buffer);
      
      if (!inputJson.isPresent()) {
        send("IGNORED");
        return;
      }

      final int id = ++counter;
      final Message message = new Message(inputJson.get());
      
      message.reply()
        .subscribeOn(RxHelper.scheduler(vertx))
        .subscribe(
          (reply) -> send("OK " + id + " " + reply.toString()),
          (error) -> {
            final JsonObject exception = new ExceptionResource(error).build();
            send("FAILED " + id + " " + exception.toString());
          },
          () -> send("DONE " + id)
        );

      send("QUEUED " + counter);
      
      messages.onNext(message);
    });
    
    input.handler(buffer -> parser.handle(buffer));
  }
  
  @Override
  public void stop() throws Exception {
    messages.onCompleted();
    input.close();
  }
  
  private void send(String message) {
    out.print(message + delimeter);
  }

  private Optional<JsonObject> parse(Buffer buffer) {
    try {
      return Optional.of(buffer.toJsonObject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
