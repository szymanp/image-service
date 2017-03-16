package com.metapx.git_metadata.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.metapx.git_metadata.core.HashPath.Target;

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
    final Target target = hashPath.getTarget(hash);
    final File folder1 = target.getFile().getParentFile();
    final File folder0 = folder1.getParentFile();
    target.prepare();

    Assert.assertEquals(folder0.getName(), "75");
    Assert.assertEquals(folder1.getName(), "e8");
    Assert.assertEquals(target.getFile().getName(), "694ba0bce5bc36d74216e80b08f4f4734e1d");

    Assert.assertTrue(folder0.exists());
    Assert.assertTrue(folder1.exists());
    Assert.assertFalse(target.getFile().exists());
  }

  @Test
  public void testGetTargetIfExists() throws Exception {
    final String hash = "75e8694ba0bce5bc36d74216e80b08f4f4734e1d";
    Assert.assertFalse(hashPath.getTargetIfExists(hash).isPresent());

    final Target target = hashPath.getTarget(hash);
    target.prepare();
    target.getFile().createNewFile();

    Assert.assertTrue(hashPath.getTargetIfExists(hash).isPresent());
  }

  @Test
  public void testGetAllTargets() throws Exception {
    final List<Target> targets = new ArrayList<Target>();
    targets.add(hashPath.getTarget("13abfc493b319610ca32dcb2e7a31c49640dc2bd6169c43f8bd55c586b8fa6a2"));
    targets.add(hashPath.getTarget("2582e0bc61fa7bbb019c7fc2305e65ce7e9dcff48319cc4f702519749cbd14a7"));
    targets.forEach(target -> {
      target.prepare();
      try {
        target.getFile().createNewFile();
      } catch (Exception e) {}
    });

    Object[] actual = hashPath.getAllTargets()
      .map(target -> folder.getRoot().toPath().relativize(target.getFile().toPath()).toString())
      .collect(Collectors.toList()).toArray();

    Assert.assertArrayEquals(new String[] { 
      "13\\ab\\fc493b319610ca32dcb2e7a31c49640dc2bd6169c43f8bd55c586b8fa6a2",
      "25\\82\\e0bc61fa7bbb019c7fc2305e65ce7e9dcff48319cc4f702519749cbd14a7"
    }, actual);
  }
}
