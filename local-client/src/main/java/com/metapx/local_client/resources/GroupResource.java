package com.metapx.local_client.resources;

import java.util.Arrays;

import com.metapx.git_metadata.groups.Group;

import io.vertx.core.json.JsonArray;

public class GroupResource extends Resource {
  final Group group;
  
  public GroupResource(Group group) {
    super("resource:group");
    this.group = group;
    
    data.put("id", group.getId());
    data.put("parent_id", group.getParent().map(p -> p.getId()).orElse(""));
    data.put("name", group.getName());
    data.put("type", group.getType());
    data.put("path", new JsonArray(Arrays.asList(group.getPath())));
    
    if (group.getParent().isPresent()) {
      links.put(Rels.Link.PARENT.toString(), "group ls -d [" + group.getParent().get().getId() + "]");
    }
  }
}
