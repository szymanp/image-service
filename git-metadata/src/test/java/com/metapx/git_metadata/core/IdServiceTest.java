package com.metapx.git_metadata.core;

import java.io.File;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;

public class IdServiceTest {
  File file;
  IdService idService;
  TransactionElement transactionElement;

  @Before
  public void setUp() throws Exception {
    file = File.createTempFile("repo", "file");
    idService = new IdService(file, (tx) -> { transactionElement = tx; });
  }

  @After
  public void tearDown() {
    if (file != null && file.exists()) {
      file.delete();
    }
  }

  @Test
  public void testCreateId() throws Exception {
    final String id1 = idService.createId("image");
    final String id2 = idService.createId("image");
    Assert.assertNotEquals("", id1);
    Assert.assertNotEquals("", id2);
    Assert.assertNotEquals(id1, id2);

    transactionElement.commit();

    final String contents = new String(Files.readAllBytes(file.toPath()));
    Assert.assertEquals(
      id1 + "\timage" + System.lineSeparator()
      + id2 + "\timage" + System.lineSeparator(),
      contents);
  }

 @Test
  public void testIdFormat() throws Exception {
    final String id = idService.createId("image");

    Assert.assertEquals(32, id.length());
    Assert.assertFalse(id, id.contains("-"));
  }
}
