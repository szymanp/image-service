package com.metapx.git_metadata.core;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.junit.Assert;

public class MetadataRepositoryTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testCreate() throws Exception {
    final MetadataRepositoryHelper helper = new MetadataRepositoryHelper(folder.getRoot());
    Assert.assertFalse(helper.exists());

    final MetadataRepository repo = helper.create();
    Assert.assertNotNull(repo);
    Assert.assertTrue("Repository does not exist", helper.exists());
    Assert.assertSame(repo.getRoot(), folder.getRoot());

    final File files = new File(folder.getRoot(), "files");
    Assert.assertTrue("'files' directory does not exist", files.exists());
    Assert.assertTrue("'files' is not a directory", files.isDirectory());
  }
}
