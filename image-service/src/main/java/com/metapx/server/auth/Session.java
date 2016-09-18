package com.metapx.server.auth;

import java.time.Instant;
import java.util.UUID;

import com.metapx.server.data_model.jooq.tables.records.UsersRecord;

import io.vertx.core.json.JsonObject;

public class Session {
  private String key;
  private int id;
  private String handle;
  private String displayName;
  private Instant started;
  private boolean isNew = false;
  
  public static Session create(UsersRecord user) {
    Session session = new Session();
    session.displayName = user.getDisplayName();
    session.handle = user.getHandle();
    session.id = user.getId();
    session.started = Instant.now();
    session.key = Session.generateUniqueHash();
    session.isNew = true;
    return session;
  }
  
  private Session() {
    // private constructor
  }
  
  public Session(JsonObject json) {
    key = json.getString("key");
    id = json.getInteger("id");
    handle = json.getString("handle");
    displayName = json.getString("displayName");
    started = json.getInstant("started");
  }
  
  public String getKey() {
    return key;
  }
  
  public int getId() {
    return id;
  }

  public String getHandle() {
    return handle;
  }

  public String getDisplayName() {
    return displayName;
  }
  
  public boolean isSessionNew() {
    return isNew;
  }

  public JsonObject toJson() {
    return new JsonObject()
        .put("key", key)
        .put("id", id)
        .put("handle", handle)
        .put("displayName", displayName)
        .put("started", started);
  }
  
  private static String generateUniqueHash() {
    return UUID.randomUUID().toString();
  }
}
