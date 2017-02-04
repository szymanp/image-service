package com.metapx.local_client.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class WildcardMatcher {
  public List<File> files;

  public WildcardMatcher(List<String> paths) {
    // c:/path/to/dir - add all files in directory and subdirectories
    // c:/path/to/dir/*.jpg - add all jpg files in directory
    // c:/path/to/dir/file.jpg - add specific file

    files = paths.stream().map(path -> {
      final File pathFile = new File(path);
      if (pathFile.getName().contains("*")) {
        return new Object[] { pathFile.getParentFile(), pathFile.getName() };
      } else {
        return new Object[] { pathFile, "" };
      }
    }).map(pair -> {
      final File path = (File) pair[0];
      final String pattern = (String) pair[1];
      final List<File> result = new ArrayList<File>();

      if (path.isDirectory()) {
        // The path references a directory.
        try {
          if (pattern.equals("")) {
            walkDirectoryRecursively(path, result);
          } else {
            walkDirectoryWithPattern(path, result, pattern);
          }
        } catch (IOException e) {
          // Do nothing. Ignore the files.
        }
      } else if (path.isFile() && pattern.equals("")) {
        // The path references a file
        result.add(path);
      }
      return result;
    })
    .flatMap(Collection::stream)
    .collect(Collectors.toList());
  }

  private void walkDirectoryRecursively(File rootDir, List<File> accumulator) throws IOException {
    try(DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir.toPath())) {
      for (Path path : stream) {
        if (path.toFile().isDirectory()) {
          walkDirectoryRecursively(path.toFile(), accumulator);
        } else if (path.toFile().isFile()) {
          accumulator.add(path.toAbsolutePath().toFile());
        }
      }
    }
  }

  private void walkDirectoryWithPattern(File rootDir, List<File> accumulator, String pattern) throws IOException {
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

    try(DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir.toPath())) {
      for (Path path : stream) {
        if (path.toFile().isFile() && matcher.matches(path.getFileName())) {
          accumulator.add(path.toAbsolutePath().toFile());
        }
      }
    }
  }
}