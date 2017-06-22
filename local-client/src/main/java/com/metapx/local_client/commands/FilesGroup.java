package com.metapx.local_client.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.metapx.local_client.cli.ClientEnvironment;
import com.metapx.local_client.cli.Console;
import com.metapx.local_client.cli.WildcardMatcher;
import com.metapx.local_client.combined_repo.CombinedRepository;
import com.metapx.local_client.combined_repo.RepositoryActions;
import com.metapx.local_client.combined_repo.RepositoryStatusFileInformation;
import com.metapx.local_client.combined_repo.TrackedFileInformation;
import com.metapx.local_client.commands.parsers.GroupPath;
import com.metapx.local_picture_repo.FileInformation;
import com.metapx.local_picture_repo.PictureRepositoryException;
import com.metapx.local_picture_repo.impl.DiskFileInformation;

public class FilesGroup {

  @Command(
    name = "add",
    description = "Add image files to the repository",
    groupNames = "files"
  )
  public static class AddCommand implements CommandRunnable {
  
    @Arguments(title = "files", description = "File patterns to add to repository")
    @Required
    private List<String> patterns;
    
    @Option(name = { "--group" },
      title = "group",
      description = "Groups to be associated with the added pictures")
    private List<String> groups = new ArrayList<String>();
  
    public void run(ClientEnvironment env) throws Exception {
  		RepositoryActions repoActions = new RepositoryActions(env.getCombinedRepository(), env.configuration);
  
  		WildcardMatcher matcher = new WildcardMatcher(patterns);
  
  		env.console.reportFiles(matcher.files.stream(), targetFile -> {
        FileInformation targetFileInformation = new DiskFileInformation(targetFile);

        if (targetFileInformation.isImage()) {
          try {
            final TrackedFileInformation trackedFile = repoActions.addFileAsPicture(targetFileInformation);
            groups.forEach(group -> repoActions.addFileToGroup(trackedFile, GroupPath.split(group)));
            return trackedFile;
          } catch (PictureRepositoryException | IOException e) {
            throw new ItemException(e);
          }
        } else {
          throw new ItemException("Skipping - not an image");
        }
  		});
  	}
  }
  
  @Command(
    name = "ls",
    description = "List tracked and untracked image files in a directory",
    groupNames = "files"
  )
  public static class ListCommand implements CommandRunnable {
  
    @Arguments(title = "files", description = "File patterns to list")
    @Required
    private List<String> patterns;

    @Option(name = "-l",
      title = "longFormat",
      description = "Use long format for listing files")
    private boolean longFormat = false;
    
    public void run(ClientEnvironment env) throws Exception {
      final WildcardMatcher matcher = new WildcardMatcher(patterns);
      final CombinedRepository repo = env.getCombinedRepository();
      final Stream<RepositoryStatusFileInformation> files =
        matcher.files.stream().map(file -> repo.getFile(file));
      
      env.console.setListingFormat(longFormat ? Console.ListingFormat.LONG : Console.ListingFormat.SHORT);
      env.console.reportFiles(files);
    }
  }
}
