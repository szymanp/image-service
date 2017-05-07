package com.metapx.local_client.cli;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.metapx.git_metadata.groups.Group;
import com.metapx.local_client.picture_repo.FileInformation;

public interface Console {
  public enum LineFormat { SHORT, LONG };

  ProcessedFileStatus startProcessingFile(File file);
  
  void printLines(Stream<Group> groups, LineFormat format);

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
            System.out.println(" " + file.getHash().substring(0, 7) + "...");
          } catch (Exception e) {
            System.out.println(" " + e.getMessage());
          }
        }

        public void fail(String message) {
          System.out.println(": " + message);
        }
      };
    }
    
    public void printLines(Stream<Group> groups, LineFormat format) {
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
  }
}
