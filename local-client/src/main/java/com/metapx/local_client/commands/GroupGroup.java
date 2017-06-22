package com.metapx.local_client.commands;

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
import com.metapx.local_client.cli.ClientEnvironment;
import com.metapx.local_client.cli.Console;
import com.metapx.local_client.cli.Console.ListingFormat;
import com.metapx.local_client.commands.parsers.GroupOrRoot;
import com.metapx.local_client.commands.parsers.GroupReference;
import com.metapx.local_client.commands.parsers.GroupType;
import com.metapx.local_client.util.ValueOrError;

public class GroupGroup {

  @Command(name = "create",
           description = "Create new groups",
           groupNames = "group")
  public static class CreateCommand extends CommonCommand {
    @Arguments(title = "group-path")
    @Required
    private List<String> groups;

    @Option(name = { "-t", "--type" },
            title = "type",
            description = "Specifies the type of the group to be created")
    private String type;

    public void run(ClientEnvironment env) throws Exception {
      final MetadataRepository repo = env.getMetadataRepositoryOrThrow();
      final Class<? extends Group> groupType = new GroupType(repo).get(type);
      
      env.getConsole().setListingFormat(ListingFormat.LONG);
      env.getConsole().reportGroups(
        groups.stream()
          .map(path -> createGroup(repo, env.getConsole(), path, groupType))
      );
    }

    private Group createGroup(MetadataRepository repo, Console console, String groupPath, Class<? extends Group> type) {
      final String[] parts = groupPath.split("/");
      final String[] path = Arrays.copyOf(parts, Math.max(0, parts.length - 1));
      final String name = parts[parts.length - 1];

      final Optional<GroupCollection<Group>> parent = path.length == 0 ?
        Optional.of(repo.groupApi().groups())
        : repo.groupApi().findGroupByPath(path).map(group -> group.subgroups());
      
      if (parent.isPresent()) {
        final Group group = repo.groupApi().create(type, name);
        parent.get().append(group);
        
        console.info("Created \"" + String.join("/", parts) + "\" (" + group.getType() + ").");
        return group;
      } else {
        throw new ItemException("Parent group not found.");
      }
    }
  }
  
  @Command(name = "ls",
    description = "List group hierarchy",
    groupNames = "group")
  public static class ListCommand extends CommonCommand {
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

    @Option(name = "-d",
      description = "Show details for specified groups, not their contents")
    private boolean details;

    @Override
    public void run(ClientEnvironment env) throws Exception {
      final MetadataRepository repo = env.getMetadataRepositoryOrThrow();
      if (groups != null && groups.size() > 0) {
        groups.forEach(path -> listGroup(repo, env.getConsole(), path));
      } else {
        listGroup(repo, env.getConsole(), "/");
      }
    }
    
    private void listGroup(MetadataRepository repo, Console console, String path) {
      final Stream<Group> groups = getGroupsFromPath(repo, console, path);

      final Stream<Group> sorted = groups.sorted((x, y) -> x.getName().compareTo(y.getName()));
      console.setListingFormat(longFormat ? Console.ListingFormat.LONG : Console.ListingFormat.SHORT);
      console.reportGroups(sorted);
    }
    
    private Stream<Group> getGroupsFromPath(MetadataRepository repo, Console console, String path) {
      final ValueOrError<GroupOrRoot> resolvedPath = GroupReference.resolve(path, repo.groupApi());
      
      if (resolvedPath.hasError()) {
        console.error(resolvedPath.error().getMessage());
        return Stream.empty();

      } else if (details) {
        final GroupOrRoot groupOrRoot = resolvedPath.get();
        return 
          groupOrRoot.isRoot() ? Stream.empty() : Stream.of(groupOrRoot.get());

      } else {
        final GroupOrRoot groupOrRoot = resolvedPath.get();
        return
          groupOrRoot.isRoot() ?
            repo.groupApi().groups().stream().filter(group -> !group.hasParent())
            : groupOrRoot.get().subgroups().stream();
      }
    }
  }
}
