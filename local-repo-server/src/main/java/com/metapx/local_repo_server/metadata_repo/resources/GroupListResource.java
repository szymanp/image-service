package com.metapx.local_repo_server.metadata_repo.resources;

import java.util.stream.Stream;

import com.metapx.git_metadata.groups.Group;
import com.metapx.local_repo_server.util.Resource;

import io.vertx.core.json.JsonArray;
import io.vertx.rxjava.ext.web.RoutingContext;

public class GroupListResource extends Resource {
  final Stream<Group> groups;
  
  public GroupListResource(RoutingContext routingContext, Stream<Group> groups) {
    super(routingContext);
    this.groups = groups;

    final JsonArray list = new JsonArray();
    embedded.put("groups", list);
    
    groups.forEach(group -> list.add(new GroupResource(routingContext, group).build()));
  }
}
