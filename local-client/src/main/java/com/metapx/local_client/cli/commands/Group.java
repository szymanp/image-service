package com.metapx.local_client.cli.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.groups.GroupCollection;
import com.metapx.git_metadata.groups.Tag;
import com.metapx.local_client.cli.ClientEnvironment;

public class Group {

  @Command(name = "create",
           description = "Creates a new group",
           groupNames = "group")
  public static class Create implements CommandRunnable {
    @Arguments(title = "group-path")
    @Required
    private List<String> groups;

    @Option(name = { "-t", "--type" },
            title = "type",
            description = "Specifies the type of the group to be created")
    private String type;

    public void run(ClientEnvironment env) throws Exception {
      final MetadataRepository repo = env.getMetadataRepositoryOrThrow();

      groups.forEach(path -> createGroup(repo, path, Optional.ofNullable(type)));
    }

    private void createGroup(MetadataRepository repo, String groupPath, Optional<String> type) {
      final String[] parts = groupPath.split("/");
      final String[] path = Arrays.copyOf(parts, Math.max(0, parts.length - 1));
      final String name = parts[parts.length - 1];

      final Optional<GroupCollection<com.metapx.git_metadata.groups.Group>> parent = path.length == 0 ?
        Optional.of(repo.groupApi().groups())
        : repo.groupApi().findGroupByPath(path).map(group -> group.subgroups());
      
      if (parent.isPresent()) {
        parent.get().append(repo.groupApi().create(Tag.class, name));
        System.out.println("Created \"" + String.join("/", parts) + "\"");
      } else {
        System.out.println("Parent group not found.");
      }
    }
  }
}
