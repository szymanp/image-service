package com.metapx.local_picture_repo;

import java.io.File;

public class SamplePictures {
  private static File samples;
  
  static {
    final File projectDir = new File(System.getProperty("user.dir"));
    samples = new File(projectDir.getParentFile(), "sample-images");
  }
  
  public static File getFile(String name) {
    final File target = new File(samples, name);
    if (!target.exists()) {
      throw new RuntimeException(target + " does not exist");
    }
    return target;
  }
}
