package com.metapx.local_repo_server.metadata_repo.resources;

import java.util.Arrays;

import com.metapx.git_metadata.groups.Group;
import com.metapx.local_repo_server.util.Resource;

import io.vertx.core.json.JsonArray;
import io.vertx.rxjava.ext.web.RoutingContext;

public class GroupResource extends Resource {
  final Group group;
  
  public GroupResource(RoutingContext routingContext, Group group) {
    super(routingContext);
    this.group = group;
    
    data.put("title", group.getName());
    data.put("type", group.getType());
    data.put("path", new JsonArray(Arrays.asList(group.getPath())));
    
    final String selfUrl = prefixUrl("/group/" + group.getId()); 
    
    links.put("self", selfUrl);
    links.put("subgroups", selfUrl + "/subgroups");
    links.put("pictures", selfUrl + "/pictures");
    if (group.hasParent()) {
      links.put("parent", prefixUrl("/group/" + group.getParent().get().getId()));
    }
  }
}
