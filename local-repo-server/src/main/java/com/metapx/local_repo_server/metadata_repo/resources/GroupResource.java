package com.metapx.local_repo_server.metadata_repo.resources;

import java.util.Arrays;

import com.metapx.git_metadata.groups.Group;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.ext.web.RoutingContext;

public class GroupResource {
  final Group group;
  final JsonObject links = new JsonObject();
  final JsonObject data = new JsonObject();
  final JsonObject embedded = new JsonObject();
  
  public GroupResource(Group group) {
    this.group = group;
    
    data.put("title", group.getName());
    data.put("type", group.getType());
    data.put("path", new JsonArray(Arrays.asList(group.getPath())));
  }
  
  public void setSelfLink(String link) {
    links.put("self", link);
  }
  
  public JsonObject build() {
    final JsonObject result = new JsonObject();
    result.put("links", links);
    result.put("data", data);
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
}
