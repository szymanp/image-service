package com.metapx.local_client.cli.commands;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.files.FileRecord;
import com.metapx.git_metadata.groups.Group;
import com.metapx.local_client.cli.ClientEnvironment;
import com.metapx.local_client.cli.Console;
import com.metapx.local_client.cli.WildcardMatcher;
import com.metapx.local_client.combined_repo.CombinedRepository;
import com.metapx.local_client.combined_repo.RepositoryActions;
import com.metapx.local_client.combined_repo.RepositoryStatusFileInformation;
import com.metapx.local_client.picture_repo.DiskFileInformation;
import com.metapx.local_client.picture_repo.FileInformation;
import com.metapx.local_client.picture_repo.Repository;

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
  
    public void run(ClientEnvironment env) throws Exception {
  		final Repository pictureRepo = env.getPictureRepository();
      final MetadataRepository metadataRepo = env.getMetadataRepositoryOrThrow();
  		RepositoryActions repoActions = new RepositoryActions(env.configuration, env.connection, pictureRepo, metadataRepo);
  
  		WildcardMatcher matcher = new WildcardMatcher(patterns);
  
  		matcher.files.stream()
  			.forEach(targetFile -> {
  				Console.ProcessedFileStatus status = env.console.startProcessingFile(targetFile);
  				FileInformation targetFileInformation = new DiskFileInformation(targetFile);
  
  				if (targetFileInformation.isImage()) {
  					try {
  						repoActions.addFileAsPicture(targetFileInformation);
  						status.success(targetFileInformation);
  					} catch (Repository.RepositoryException e) {
  						status.fail(e.getMessage());
  					} catch (IOException e) {
  						e.printStackTrace();
  					}
  				} else {
  					status.fail("Skipping - not an image");
  				}
  			});
  
  		repoActions.commit();
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
      
      env.console.printFileStatusLines(files, longFormat ? Console.LineFormat.LONG : Console.LineFormat.SHORT);
    }
  }
}
