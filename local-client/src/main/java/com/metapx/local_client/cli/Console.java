package com.metapx.local_client.cli;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.metapx.git_metadata.groups.Group;
import com.metapx.local_client.combined_repo.RepositoryStatusFileInformation;
import com.metapx.local_client.combined_repo.TrackedFileGroup;
import com.metapx.local_client.combined_repo.TrackedFileInformation;
import com.metapx.local_client.commands.ItemException;
import com.metapx.local_client.commands.PictureGroup;
import com.metapx.local_client.commands.PictureGroup.MaterializedPicture;
import com.metapx.local_picture_repo.FileInformation;

import rx.Observable;

public interface Console {
  public enum ListingFormat { SHORT, LONG };

  void setListingFormat(ListingFormat format);
  
  void info(String message);
  void error(String message);
  void error(Throwable error);
  void reportFiles(Observable<File> files, Function<File, FileInformation> processor);
  void reportFiles(Observable<RepositoryStatusFileInformation> files);
  void reportFileGroups(Observable<TrackedFileGroup> fileGroups);
  void reportGroups(Observable<Group> groups);
  
  void reportMaterializedPictures(Observable<PictureGroup.MaterializedPicture> files);
  
  /**
   * A default implementation of the Console interface.
   */
  public class DefaultConsole implements Console {
    public enum HashFormat { SHORT, LONG };
    
    final Configuration conf;
    private HashFormat hashFormat = HashFormat.SHORT;
    private ListingFormat listingFormat = ListingFormat.SHORT;

    DefaultConsole(Configuration conf) {
      this.conf = conf;
    }

    public void setHashFormat(HashFormat format) {
      hashFormat = format;
    }
    
    @Override
    public void setListingFormat(ListingFormat format) {
      listingFormat = format;
    }
    
    /**
     * Print an informational message on the console.
     * This message is not necessary to properly interpret the console output.
     */
    @Override
    public void info(String message) {
      System.out.println(message);
    }
    
    /**
     * Sends a processing error to the console.
     */
    @Override
    public void error(String message) {
      System.err.println(message);
    }

    @Override
    public void error(Throwable error) {
      System.err.println(error.getMessage());
    }

    @Override
    public void reportFiles(Observable<File> files, Function<File, FileInformation> processor) {
      files.forEach(file -> {
        System.out.print(relativize(file.getAbsoluteFile()));

        try {
          final FileInformation fileInformation = processor.apply(file);
          System.out.println(" " + hash(fileInformation.getHash()));
        } catch (final ItemException e) {
          System.out.println(": " + e.getMessage());
        }
      });
    }
    
    @Override
    public void reportGroups(Observable<Group> groups) {
      switch (listingFormat) {
      case SHORT:
        groups.forEach(group -> System.out.print(group.getName() + "  "));
        System.out.println();
        break;
      case LONG:
        groups.forEach(group ->
          System.out.println(String.format(
              "%1$-30s %2$-15s %3$s", 
              group.getName(),
              "(" + group.getType() + ")",
              hash(group.getId())
            ))
        );
        break;
      }
    }

    @Override
    public void reportFiles(Observable<RepositoryStatusFileInformation> files) {
      switch (listingFormat) {
      case SHORT:
        printGroupedByDir(
          files, 
          (f) -> f.getFile().getParentFile(), 
          (f) -> {
            final String name = f.getFile().getName();
            if (f.isTracked()) return "[" + name + "]";
            else if (f.isKnown()) return "(" + name + ")";
            else return name;
          },
          false
        );
        break;
      case LONG:
        printGroupedByDir(
          files, 
          (f) -> f.getFile().getParentFile(),
          (f) -> String.format(
            "%1$-7s %2$-7s %3$-4s %4$-4s %5$s %6$s",
            f.isKnown() ? hash(f.getHash()) : "-",
            hash(f.getFileGroup().map((fg) -> fg.getFileRecord().getPictureId()).orElse("")),
            f.getWidth(),
            f.getHeight(),
            f.isTracked() ? "T" : "-",
            f.getFile().getName()
          ),
          true
        );
        break;
      }
    }
    
    @Override
    public void reportFileGroups(Observable<TrackedFileGroup> fileGroups) {
      fileGroups.forEach(fileGroup -> {
        final Optional<TrackedFileInformation> trackedFile = fileGroup.getValidFile();
        
        if (trackedFile.isPresent()) {
          System.out.println(trackedFile.get().getFile());
        } else {
          System.out.println(fileGroup.getHash());
        }
      });
    }
    
    @Override
    public void reportMaterializedPictures(Observable<MaterializedPicture> files) {
      files.subscribe(file -> {
        System.out.println(hash(file.getPictureHash(), HashFormat.LONG) + " " + relativize(file.getFile().getAbsoluteFile()));
      });
    }
    
    private <T> void printGroupedByDir(Observable<T> stream, Function<T, File> classifier, Function<T, String> printer, boolean longFormat) {
      final Map<File, List<T>> dirs = stream.toList().toBlocking().first().stream().collect(Collectors.groupingBy(classifier, Collectors.toList()));
      final Consumer<T> valuePrinter = longFormat ?
        (value) -> { System.out.println(printer.apply(value)); }
        : (value) -> { System.out.print(printer.apply((value)) + "  "); };
      final boolean first = true;
      for(final File dir : dirs.keySet()) {
        if (!first) {
          System.out.println("");
        }
        System.out.println(relativize(dir) + ":");
        dirs.get(dir).stream().forEach(valuePrinter);
      }
    }
    
    private String relativize(File file) {
      final Path workingDirectory = conf.getWorkingDirectory().toPath();
      final Path path = file.toPath();
      
      if (workingDirectory.getRoot().equals(path.getRoot())) {
        final String relative = workingDirectory.relativize(path).toString();
        if (relative.startsWith(".." + File.separator + ".." + File.separator + ".." + File.separator)) {
          return file.toString();
        } else {
          return relative;
        }
      } else {
        // Cannot relativize as they reside on different roots.
        return file.toString();
      }
    }

    private String hash(String hash) {
      return hash(hash, hashFormat);
    }
    
    private String hash(String hash, HashFormat format) {
      if (hash.equals("")) {
        return "-";
      }

      switch (format) {
      case LONG:
        return hash;
      case SHORT:
      default:
        return hash.substring(0, 7);
      }
    }
  }
}
