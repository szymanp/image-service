package com.metapx.local_client.resources;

import com.metapx.local_client.resources.Rels.Link;

import io.vertx.core.json.JsonObject;

public abstract class Resource {
  final protected JsonObject links = new JsonObject();
  final protected JsonObject data = new JsonObject();
  final protected JsonObject embedded = new JsonObject();
  final private String type;
  
  public Resource(String type) {
    this.type = type;
  }
  
  public JsonObject build() {
    final JsonObject result = new JsonObject();
    result.put("type", type);
    if (!links.isEmpty()) {
      result.put("links", links);
    }
    if (!data.isEmpty()) {
      result.put("data", data);
    }
    if (!embedded.isEmpty()) {
      result.put("embedded", embedded);
    }
    return result;
  }
  
  protected void addLink(String link, String type, String href) {
    final JsonObject linkObject = new JsonObject();
    linkObject.put("href", href);
    linkObject.put("type", type);
    linkObject.put("rel", link);
    links.put(link, linkObject);
  }
  
  protected void addCommandLink(Link link, String command) {
    addLink(link.toString(), "command", command);
  }
}
