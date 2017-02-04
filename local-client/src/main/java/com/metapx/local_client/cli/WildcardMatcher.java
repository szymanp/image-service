package com.metapx.local_client.cli;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.codehaus.plexus.util.DirectoryScanner;

public class WildcardMatcher {
  public List<File> files;

  public WildcardMatcher(List<String> patterns) {
    // ../somedir/*.jpg
    // d:\somedir\hello\*.jpg
    // somedir/*.jpg

    files = patterns.stream().map(pattern -> {
      final int wildcard1 = pattern.indexOf('*');
      final int wildcard2 = pattern.indexOf('?');
      final int wildcard = wildcard1 >= 0 && wildcard2 >= 0 ?
        Math.min(wildcard1, wildcard2) : (wildcard1 >= 0 ? wildcard1 : wildcard2);

      if (wildcard < 0) {
        return new String[] { pattern, "" };
      } else {
        String directory = pattern.substring(0, wildcard);
        int lastSeparator = getLastSeparatorIndex(directory);
        if (lastSeparator < 0) {
          return new String[] { "", pattern };
        } else {
          directory = directory.substring(0, lastSeparator+1);
          return new String[] { directory, pattern.substring(directory.length()) };
        }
      }
    }).map(pair -> {
      System.out.println(pair[0] + ";" + pair[1]);
      if (pair[1].equals("")) {
        final List<File> result = new ArrayList<File>();
        final File file = new File(pair[0]).getAbsoluteFile();
        if (file.isFile()) {
          result.add(file);
        }
        return result;
      } else {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[] { pair[1] });
        scanner.setBasedir(new File(pair[0].equals("") ? "." : pair[0]));
        scanner.setCaseSensitive(false);
        System.out.println("base=" + scanner.getBasedir());
        scanner.scan();

        return Arrays.stream(scanner.getIncludedFiles())
          .map(file -> new File(scanner.getBasedir(), file).getAbsoluteFile())
          .collect(Collectors.toList());
      }
    })
    .flatMap(Collection::stream)
    .collect(Collectors.toList());
  }

  private int getLastSeparatorIndex(String path) {
    String unified = path.replace(File.separatorChar, '/');
    return unified.lastIndexOf('/');
  }
}