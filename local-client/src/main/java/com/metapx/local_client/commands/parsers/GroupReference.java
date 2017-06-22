package com.metapx.local_client.commands.parsers;

import com.metapx.git_metadata.groups.GroupService;
import com.metapx.local_client.commands.ItemException;
import com.metapx.local_client.util.ValueOrError;

public class GroupReference {
  private final String ref;
  private GroupOrRoot resolved;
  
  public static ValueOrError<GroupOrRoot> resolve(String ref, GroupService groupService) {
    return new GroupReference(ref).resolve(groupService);
  }

  public static GroupOrRoot resolveOrThrow(String ref, GroupService groupService) {
    return new GroupReference(ref).resolveOrThrow(groupService);
  }

  public GroupReference(String ref) {
    this.ref = ref.trim();
  }

  public ValueOrError<GroupOrRoot> resolve(GroupService groupService) {
    return ValueOrError.resolve(() -> resolveOrThrow(groupService));
  }
  
  public GroupOrRoot resolveOrThrow(GroupService groupService) {
    if (resolved == null) {
      if (ref.startsWith("[") && ref.endsWith("]")) {
        resolved = resolveId(groupService, ref.substring(1, ref.length() - 1));
      } else if (ref.length() > 6 && isHex(ref)) {
        resolved = resolveId(groupService, ref);
      } else {
        resolved = resolvePath(groupService, ref);
      }
    }
    return resolved;
  }
  
  private GroupOrRoot resolvePath(GroupService groupService, String path) {
    final GroupPath split = GroupPath.split(ref);
    if (split.isRootPath()) {
      return GroupOrRoot.root();
    } else {
      return GroupOrRoot.group(
        groupService.findGroupByPath(split.getParts())
        .orElseThrow(() -> new ItemException("Group \"" + path + "\" does not exist."))
      );
    }
    
  }
  
  private GroupOrRoot resolveId(GroupService groupService, String hashPrefix) {
    if (isZeroHash(hashPrefix)) {
      return GroupOrRoot.root();
    } else {
      return groupService.groups().findByIdPrefix(hashPrefix)
        .map(group -> GroupOrRoot.group(group))
        .orElseThrow(() -> new ItemException("Group with hash " + hashPrefix + " not found."));
    }
  }
  
  private boolean isHex(String testString) {
    final String valid = "0123456789abcdef";
    for(int i=0;i<testString.length();i++) {
      final String c = testString.substring(i, i + 1).toLowerCase();
      if (!valid.contains(c)) {
        return false;
      }
    }
    return true;
  }
  
  private boolean isZeroHash(String testString) {
    for(int i=0;i<testString.length();i++) {
      if (!testString.substring(i, i + 1).equals("0")) {
        return false;
      }
    }
    return true;
  }
}
