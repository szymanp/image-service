package com.metapx.local_client.commands.parsers;

import java.util.Map;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.groups.Group;
import com.metapx.git_metadata.groups.Tag;
import com.metapx.local_client.commands.CommandException;

public class GroupType {
  private final Map<String, Class<? extends Group>> groupTypes;

  public GroupType(MetadataRepository repo) {
    groupTypes = repo.groupApi().getGroupTypes();
  }
  
  public Class<? extends Group> get(String typeOrNull) {
    if (typeOrNull == null || typeOrNull.equals("")) {
      return Tag.class;
    } else {
      if (groupTypes.containsKey(typeOrNull)) {
        return groupTypes.get(typeOrNull);
      } else {
        throw new CommandException("Group type \"" + typeOrNull + "\" is not valid.");
      }
    }
  }
}
