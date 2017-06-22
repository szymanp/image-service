package com.metapx.local_client.commands;

import java.util.List;
import java.util.stream.Stream;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.metapx.git_metadata.core.IntegrityException;
import com.metapx.git_metadata.core.collections.KeyedCollection;
import com.metapx.git_metadata.groups.Group;
import com.metapx.git_metadata.pictures.Picture;
import com.metapx.local_client.cli.ClientEnvironment;
import com.metapx.local_client.cli.Console;
import com.metapx.local_client.combined_repo.CombinedRepository;
import com.metapx.local_client.combined_repo.TrackedFileGroup;
import com.metapx.local_client.combined_repo.TrackedFileGroupImpl;
import com.metapx.local_client.commands.parsers.GroupPath;

@Command(
  name = "ls",
  description = "List files"
)
public class ListCommand implements CommandRunnable {

  @Arguments(title = "target")
  @Required
  private List<String> targets;

  @Option(name = { "-g", "--group" },
          description = "List files belonging to a group")
  private boolean group = false;
  
  @Option(name = "-l",
    title = "longFormat",
    description = "Use long format for listing files")
  private boolean longFormat = false;
  
  public void run(ClientEnvironment env) throws Exception {
    final CombinedRepository repo = env.getCombinedRepository();

    if (group) {
      targets.forEach((path) -> listFilesInGroup(repo, env.console, path));
    }
  }
  
  private void listFilesInGroup(CombinedRepository repo, Console console, String path) {
    final GroupPath groupPath = GroupPath.split(path);
    final KeyedCollection<String, Picture> pictures = repo.getMetadataRepository().pictures();
    
    if (groupPath.isRootPath()) {
      return; // Nothing to list as no files are members of the root of the group hierarchy.
    }
    
    final Group group = repo.getMetadataRepository().groupApi().findGroupByPath(groupPath.getParts())
      .orElseThrow(() -> new ItemException("Group \"" + path + "\" does not exist."));

    Stream<TrackedFileGroup> fileGroups =
      group.pictures().stream()
      .map((ref) ->
        pictures.findWithKey(ref.getObjectId())
        .orElseThrow(() -> new IntegrityException(ref, "Not found"))
      )
      .map((picture) ->
        picture.files().stream().findFirst()
        .orElseThrow(() -> new IntegrityException(picture.getReference(), "No member files defined"))
      )
      .map((file) -> new TrackedFileGroupImpl(repo, file.getFileHash()));
    
    console.reportFileGroups(fileGroups);
  }
}
