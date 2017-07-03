package com.metapx.local_client.daemon;

import io.vertx.core.json.JsonObject;
import rx.subjects.Subject;
import rx.subjects.UnicastSubject;

public class Message {
  private final JsonObject body;
  private final Subject<JsonObject, JsonObject> replyChannel;
  
  Message(JsonObject body) {
    this.body = body;
    this.replyChannel = UnicastSubject.create();
  }
  
  public JsonObject getBody() {
    return body;
  }
  
  public Subject<JsonObject, JsonObject> reply() {
    return replyChannel;
  }
}
