package com.metapx.local_client.cli;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.metapx.git_metadata.groups.Group;
import com.metapx.local_client.cli.commands.ItemException;
import com.metapx.local_client.combined_repo.RepositoryStatusFileInformation;
import com.metapx.local_picture_repo.FileInformation;

public interface Console {
  public enum ListingFormat { SHORT, LONG };

  void setListingFormat(ListingFormat format);
  
  void reportFiles(Stream<File> files, Function<File, FileInformation> processor);
  void reportFiles(Stream<RepositoryStatusFileInformation> files);
  void reportGroups(Stream<Group> groups);

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
    
    public void setListingFormat(ListingFormat format) {
      listingFormat = format;
    }

    public void reportFiles(Stream<File> files, Function<File, FileInformation> processor) {
      files.forEach(file -> {
        System.out.print(relativize(file.getAbsoluteFile()));

        try {
          final FileInformation fileInformation = processor.apply(file);
          System.out.println(" " + hash(fileInformation.getHash()));
        } catch (ItemException e) {
          System.out.println(": " + e.getMessage());
        }
      });
    }
    
    public void reportGroups(Stream<Group> groups) {
      switch (listingFormat) {
      case SHORT:
        groups.forEach(group -> System.out.print(group.getName() + "  "));
        System.out.println();
        break;
      case LONG:
        groups.forEach(group ->
          System.out.println(String.format("%1$-10s %2$s", group.getType(), group.getName()))
        );
        break;
      }
    }

    public void reportFiles(Stream<RepositoryStatusFileInformation> files) {
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
            "%1$-7s %2$-7s %3$4sx%4$-4s %5$s %6$s",
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
    
    private <T> void printGroupedByDir(Stream<T> stream, Function<T, File> classifier, Function<T, String> printer, boolean longFormat) {
      final Map<File, List<T>> dirs = stream.collect(Collectors.groupingBy(classifier, Collectors.toList()));
      final Consumer<T> valuePrinter = longFormat ?
        (value) -> { System.out.println(printer.apply(value)); }
        : (value) -> { System.out.print(printer.apply((value)) + "  "); };
      boolean first = true;
      for(File dir : dirs.keySet()) {
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
