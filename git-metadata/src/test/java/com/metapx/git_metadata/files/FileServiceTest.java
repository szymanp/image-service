package com.metapx.git_metadata.files;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.metapx.git_metadata.core.TransactionElement;

import org.junit.Assert;
import org.junit.Before;

public class FileServiceTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  public FileService fileService;
  public List<TransactionElement> transactions;

  @Before
  public void setUp() throws Exception {
    transactions = new ArrayList<TransactionElement>();
    fileService = new FileService(folder.getRoot(), txel -> transactions.add(txel));
  }

  @Test
  public void testCreate() throws Exception {
    final FileRecord record = newFileRecord();
    fileService.create(record);

    for(TransactionElement txel : transactions) txel.commit();

    final File expectedFile = new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d");
    Assert.assertTrue(expectedFile.exists());
    final String contents = new String(Files.readAllBytes(expectedFile.toPath()));
    Assert.assertEquals("123456\tjpeg\t1024\t768\tmy_file.jpg" + System.lineSeparator(), contents);
  }

  @Test
  public void testUpdate() throws Exception {
    final FileRecord record = newFileRecord();
    fileService.create(record);
    for(TransactionElement txel : transactions) txel.commit();

    record.setDefaultFilename("my_other_file.jpg");
    fileService.update(record);
    for(TransactionElement txel : transactions) txel.commit();

    final File expectedFile = new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d");
    Assert.assertTrue(expectedFile.exists());
    final String contents = new String(Files.readAllBytes(expectedFile.toPath()));
    Assert.assertEquals("123456\tjpeg\t1024\t768\tmy_other_file.jpg" + System.lineSeparator(), contents);
  }

  FileRecord newFileRecord() {
    final FileRecord record = new FileRecord();
    record.setHash("75e8694ba0bce5bc36d74216e80b08f4f4734e1d");
    record.setDefaultFilename("my_file.jpg");
    record.setFiletype("jpeg");
    record.setHeight(1024);
    record.setWidth(768);
    record.setSize(123456);
    return record;
  }
}
