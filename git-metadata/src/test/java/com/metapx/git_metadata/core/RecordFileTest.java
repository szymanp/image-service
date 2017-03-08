package com.metapx.git_metadata.core;

import static java.nio.file.StandardOpenOption.APPEND;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;

public class RecordFileTest {
  File file;
  RecordFile<StringRecord> rf;

  @Before
  public void setUp() throws IOException {
    file = File.createTempFile("recordfile", "test");
    rf = new RecordFile<StringRecord>(file, new StringRecord.Reader());
  }

  @After
  public void tearDown() {
    if (file != null && file.exists()) {
      file.delete();
    }
  }

  @Test
  public void testAppend() throws IOException {
    final StringRecord record = new StringRecord();
    record.fields.add("hello");
    record.fields.add("world");
    rf.append(record);
    rf.commit();

    final String contents = new String(Files.readAllBytes(file.toPath()));
    Assert.assertEquals("hello\tworld" + System.lineSeparator(), contents);
  }

  @Test
  public void testFind() throws IOException {
    Files.write(file.toPath(), "hello\tworld\nfrom\there\n".getBytes(), APPEND);
    Optional<StringRecord> record = rf.findWithKey("from");

    Assert.assertTrue(record.isPresent());
    Assert.assertEquals("from", record.get().fields.get(0));
    Assert.assertEquals("here", record.get().fields.get(1));

    record = rf.findWithKey("missing");
    Assert.assertFalse(record.isPresent());
  }
}