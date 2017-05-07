package com.metapx.local_client.cli.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.groups.Group;
import com.metapx.git_metadata.groups.GroupCollection;
import com.metapx.git_metadata.groups.Tag;
import com.metapx.local_client.cli.ClientEnvironment;
import com.metapx.local_client.cli.Console;

public class GroupGroup {

  @Command(name = "create",
           description = "Create new groups",
           groupNames = "group")
  public static class CreateCommand implements CommandRunnable {
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

      final Optional<GroupCollection<Group>> parent = path.length == 0 ?
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
  
  @Command(name = "ls",
    description = "List group hierarchy",
    groupNames = "group")
  public static class ListCommand implements CommandRunnable {
    @Arguments(title = "group-path")
    private List<String> groups;

    @Option(name = { "-t", "--type" },
            title = "type",
            description = "Specifies the type of the group to be listed")
    private String type;

    @Option(name = "-l",
      title = "longFormat",
      description = "Use long format for listing groups")
    private boolean longFormat;

    @Override
    public void run(ClientEnvironment env) throws Exception {
      final MetadataRepository repo = env.getMetadataRepositoryOrThrow();
      if (groups != null && groups.size() > 0) {
        groups.forEach(path -> listGroup(repo, env.console, path));
      } else {
        listGroup(repo, env.console, "/");
      }
    }
    
    private void listGroup(MetadataRepository repo, Console console, String path) {
      if (path.startsWith("/")) path = path.substring(1);
      if (path.endsWith("/")) path = path.substring(0, path.length() - 1);

      final String[] parts = path.split("/");
      final boolean listRoot = parts.length == 1 && parts[0].equals(""); 
      
      final Stream<Group> groups =
        listRoot ?
        repo.groupApi().groups().stream().filter(group -> !group.hasParent())
        : repo.groupApi().findGroupByPath(parts).map(group -> group.subgroups().stream()).orElse(Stream.empty());

      final Stream<Group> sorted = groups.sorted((x, y) -> x.getName().compareTo(y.getName()));
      console.printLines(sorted, longFormat ? Console.LineFormat.LONG : Console.LineFormat.SHORT);
    }
  }
}
