package com.metapx.local_repo_server.util;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.ext.web.RoutingContext;

public abstract class Resource {
  final protected RoutingContext routingContext;
  final protected JsonObject links = new JsonObject();
  final protected JsonObject data = new JsonObject();
  final protected JsonObject embedded = new JsonObject();
  
  public Resource(RoutingContext routingContext) {
    this.routingContext = routingContext;
  }
  
  public void addSelfLink() {
    links.put("self", routingContext.request().absoluteURI());
  }
  
  public JsonObject build() {
    final JsonObject result = new JsonObject();
    result.put("links", links);
    if (!data.isEmpty()) {
      result.put("data", data);
    }
    if (!embedded.isEmpty()) {
      result.put("embedded", embedded);
    }
    return result;
  }
  
  public void send(RoutingContext routingContext) {
    routingContext.response()
      .putHeader("content-type", "application/json")
      .end(build().encodePrettily());
  }
  
  protected String prefixUrl(String relativeUrl) {
    final HttpServerRequest req = routingContext.request();
    final StringBuilder result = new StringBuilder();
    
    result.append(req.isSSL() ? "https://" : "http://");
    result.append(req.host());
    result.append(relativeUrl);
    
    return result.toString();
  }
}
