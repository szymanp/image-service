package com.metapx.local_client.cli;

import java.io.File;
import java.nio.file.Path;

import com.metapx.local_client.picture_repo.FileInformation;

public interface Console {

  ProcessedFileStatus startProcessingFile(File file);

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
