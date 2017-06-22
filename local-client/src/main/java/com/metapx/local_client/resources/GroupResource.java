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

    // Self-link
    addCommandLink(Rels.Link.SELF, "group ls -d [" + group.getId() + "]");

    // Parent link
    if (group.getParent().isPresent()) {
      addCommandLink(Rels.Link.PARENT, "group ls -d [" + group.getParent().get().getId() + "]");
    }
    
    // Children link
    if (group.subgroups().stream().findAny().isPresent()) {
      addCommandLink(Rels.Link.CHILDREN, "group ls [" + group.getId() + "]");
    }
  }
}
