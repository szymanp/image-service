package com.metapx.local_client.cli;

import java.io.File;

import com.metapx.local_client.database.jooq.tables.records.FileRecord;

public interface Console {

  ProcessedFileStatus startProcessingFile(File file);

  public interface ProcessedFileStatus {
    void success(FileRecord file);
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
        public void success(FileRecord file) {
         System.out.println(" " + file.getHash().substring(0, 7) + "...");
        }

        public void fail(String message) {
          System.out.println(": " + message);
        }
      };
    }

    private String relativize(File file) {
      String relative = conf.getWorkingDirectory().toPath().relativize(file.toPath()).toString();
      if (relative.startsWith(".." + File.separator + ".." + File.separator + ".." + File.separator)) {
        return file.toString();
      } else {
        return relative;
      }
    }
  }
}
