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
import com.metapx.local_client.picture_repo.FileInformation;
import com.metapx.local_client.combined_repo.RepositoryStatusFileInformation;

public interface Console {
  public enum LineFormat { SHORT, LONG };
  public enum HashFormat { SHORT, LONG };

  ProcessedFileStatus startProcessingFile(File file);
  
  void printGroupLines(Stream<Group> groups, LineFormat format);
  void printFileStatusLines(Stream<RepositoryStatusFileInformation> files, LineFormat format);

  public interface ProcessedFileStatus {
    void success(FileInformation file);
    void fail(String message);
  }

  /**
   * A default implementation of the Console interface.
   */
  public class DefaultConsole implements Console {
    final Configuration conf;

    DefaultConsole(Configuration conf) {
      this.conf = conf;
    }

    public ProcessedFileStatus startProcessingFile(File file) {
      System.out.print(relativize(file.getAbsoluteFile()));

      return new ProcessedFileStatus() {
        public void success(FileInformation file) {
          try {
            System.out.println(" " + hash(file.getHash()));
          } catch (Exception e) {
            System.out.println(" " + e.getMessage());
          }
        }

        public void fail(String message) {
          System.out.println(": " + message);
        }
      };
    }
    
    public void printGroupLines(Stream<Group> groups, LineFormat format) {
      switch (format) {
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

    public void printFileStatusLines(Stream<RepositoryStatusFileInformation> files, LineFormat format) {
      switch (format) {
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
      return hash(hash, HashFormat.SHORT);
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
