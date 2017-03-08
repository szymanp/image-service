package com.metapx.git_metadata.core;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.junit.Assert;
import org.junit.Before;

public class HashPathTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  public HashPath hashPath;

  @Before
  public void setUp() throws Exception {
    hashPath = new HashPath(folder.getRoot(), 2, 2);
  }

  @Test
  public void testGetTarget() throws Exception {
    final String hash = "75e8694ba0bce5bc36d74216e80b08f4f4734e1d";
    final File target = hashPath.getTarget(hash);
    final File folder1 = target.getParentFile();
    final File folder0 = folder1.getParentFile();

    Assert.assertEquals(folder0.getName(), "75");
    Assert.assertEquals(folder1.getName(), "e8");
    Assert.assertEquals(target.getName(), "694ba0bce5bc36d74216e80b08f4f4734e1d");

    Assert.assertTrue(folder0.exists());
    Assert.assertTrue(folder1.exists());
    Assert.assertFalse(target.exists());
  }

  @Test
  public void testGetTargetIfExists() throws Exception {
    final String hash = "75e8694ba0bce5bc36d74216e80b08f4f4734e1d";
    Assert.assertFalse(hashPath.getTargetIfExists(hash).isPresent());

    final File target = hashPath.getTarget(hash);
    target.createNewFile();

    Assert.assertTrue(hashPath.getTargetIfExists(hash).isPresent());
  }
}
