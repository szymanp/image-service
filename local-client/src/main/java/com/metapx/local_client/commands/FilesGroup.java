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
import com.metapx.local_picture_repo.FileInformation;
import com.metapx.local_picture_repo.PictureRepositoryException;
import com.metapx.local_picture_repo.impl.DiskFileInformation;

import rx.Observable;

public class FilesGroup {

  @Command(
    name = "add",
    description = "Add image files to the repository",
    groupNames = "files"
  )
  public static class AddCommand extends CommonCommand {
  
    @Arguments(title = "files", description = "File patterns to add to repository")
    @Required
    private List<String> patterns;
    
    @Option(name = { "--group" },
      title = "group",
      description = "Groups to be associated with the added pictures")
    private List<String> groups = new ArrayList<String>();
  
    @Override
    public void run(ClientEnvironment env) throws Exception {
  		final RepositoryActions repoActions = new RepositoryActions(env.getCombinedRepository(), env.getConfiguration());
  
  		final WildcardMatcher matcher = new WildcardMatcher(patterns);

  		env.getConsole().reportFiles(Observable.from(matcher.files.stream()::iterator), targetFile -> {
        final FileInformation targetFileInformation = new DiskFileInformation(targetFile);

        if (targetFileInformation.isImage()) {
          try {
            final TrackedFileInformation trackedFile = repoActions.addFileAsPicture(targetFileInformation);
            groups.forEach(group -> repoActions.addFileToGroup(trackedFile, group));
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
  public static class ListCommand extends CommonCommand {
  
    @Arguments(title = "files", description = "File patterns to list")
    @Required
    private List<String> patterns;

    @Option(name = "-l",
      title = "longFormat",
      description = "Use long format for listing files")
    private boolean longFormat = false;
    
    @Override
    public void run(ClientEnvironment env) throws Exception {
      final WildcardMatcher matcher = new WildcardMatcher(patterns);
      final CombinedRepository repo = env.getCombinedRepository();
      final Stream<RepositoryStatusFileInformation> files =
        matcher.files.stream().map(file -> repo.getFile(file));
      
      env.getConsole().setListingFormat(longFormat ? Console.ListingFormat.LONG : Console.ListingFormat.SHORT);
      env.getConsole().reportFiles(Observable.from(files::iterator));
    }
  }
}
