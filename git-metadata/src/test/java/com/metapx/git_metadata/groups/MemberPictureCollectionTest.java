package com.metapx.git_metadata.groups;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.metapx.git_metadata.core.TransactionSubject;
import com.metapx.git_metadata.pictures.PictureReference;
import com.metapx.git_metadata.references.ReferenceService;

import org.junit.Assert;

public class MemberPictureCollectionTest {
  MemberPictureCollection coll;
  File file;

  @Before
  public void setUp() throws IOException {
    file = File.createTempFile("recordfile", "test");
    coll = new MemberPictureCollection(file, new ReferenceService(), new GroupReference("0000deadbeef"));
  }

  @After
  public void tearDown() {
    if (file != null && file.exists()) {
      file.delete();
    }
  }

  @Test
  public void testAppendAndCheckInMemory() {
    coll.append(new PictureReference("0123456"));
    coll.append(new PictureReference("1231230"));

    Assert.assertTrue("Collection does not contain 1231230", coll.contains(new PictureReference("1231230")));
    Assert.assertFalse("Contains returned true on invalid element", coll.contains(new PictureReference("ababab")));
  }

  @Test
  public void testAppendAndStream() {
    coll.append(new PictureReference("0123456"));
    coll.append(new PictureReference("1231230"));

    Assert.assertTrue(coll.stream().anyMatch(element -> element.equals(new PictureReference("1231230"))));
  }

  @Test
  public void testAppendAndWrite() throws Exception {
    coll.append(new PictureReference("0123456"));
    coll.append(new PictureReference("1231230"));

    coll.commit();

    final String contents = new String(Files.readAllBytes(file.toPath()));
    Assert.assertEquals("0123456" + System.lineSeparator()
                       +"1231230" + System.lineSeparator(), contents);
  }

  @Test
  public void testReadAndStream() throws Exception {
    Files.write(file.toPath(), "0123456\n1231230\n2342340\n".getBytes());

    final Object[] actual = coll.stream().map(ref -> ref.getObjectId()).collect(Collectors.toList()).toArray();
    Assert.assertArrayEquals(new String[] { "0123456", "1231230", "2342340" }, actual);
  }
}
