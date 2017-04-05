package com.metapx.git_metadata.files;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.metapx.git_metadata.core.TransactionElement;
import com.metapx.git_metadata.core.collections.Collection;
import com.metapx.git_metadata.pictures.PictureReference;
import com.metapx.git_metadata.references.ReferenceService;
import com.metapx.git_metadata.references.ReferenceService.Message;

import org.junit.Assert;
import org.junit.Before;

public class FileServiceTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  public FileService fileService;
  public Collection<FileRecord> files;
  public ReferenceService refService;
  public List<TransactionElement> transactions;

  @Before
  public void setUp() throws Exception {
    transactions = new ArrayList<TransactionElement>();
    refService = new ReferenceService();
    fileService = new FileService(folder.getRoot(), txel -> transactions.add(txel), refService);
    files = fileService.files();
  }

  @Test
  public void testCreate() throws Exception {
    final FileRecord record = newFileRecord();
    files.append(record);

    for(TransactionElement txel : transactions) txel.commit();

    final File expectedFile = new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d");
    Assert.assertTrue(expectedFile.exists());
    final String contents = new String(Files.readAllBytes(expectedFile.toPath()));
    Assert.assertEquals("123456\tjpeg\t1024\t768\tmy_file.jpg\t7da9b60d0194d61562fedab48092e1b7" + System.lineSeparator(), contents);
  }

  @Test
  public void testUpdate() throws Exception {
    final FileRecord record = newFileRecord();
    files.append(record);
    for(TransactionElement txel : transactions) txel.commit();

    record.setDefaultFilename("my_other_file.jpg");
    files.update(record);
    for(TransactionElement txel : transactions) txel.commit();

    final File expectedFile = new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d");
    Assert.assertTrue(expectedFile.exists());
    final String contents = new String(Files.readAllBytes(expectedFile.toPath()));
    Assert.assertEquals("123456\tjpeg\t1024\t768\tmy_other_file.jpg\t7da9b60d0194d61562fedab48092e1b7" + System.lineSeparator(), contents);
  }

  @Test
  public void testReferencePicture() throws Exception {
    final FileRecord record = newFileRecord();
    record.setPictureId("");
    files.append(record);
    for(TransactionElement txel : transactions) txel.commit();

    final Message message = ReferenceService.newMessageBuilder(
        new PictureReference("8a1d540f8aeb1df7dbf34ce60309e935"),
        ReferenceService.Operation.REFERENCE
      )
      .references(new FileReference("75e8694ba0bce5bc36d74216e80b08f4f4734e1d"))
      .build();
    refService.emit(message);
    for(TransactionElement txel : transactions) txel.commit();

    final File expectedFile = new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d");
    Assert.assertTrue(expectedFile.exists());
    final String contents = new String(Files.readAllBytes(expectedFile.toPath()));
    Assert.assertEquals("123456\tjpeg\t1024\t768\tmy_file.jpg\t8a1d540f8aeb1df7dbf34ce60309e935" + System.lineSeparator(), contents);
  }

  @Test
  public void testUnreferencePicture() throws Exception {
    final FileRecord record = newFileRecord();
    files.append(record);
    for(TransactionElement txel : transactions) txel.commit();

    final Message message = ReferenceService.newMessageBuilder(
        new PictureReference("7da9b60d0194d61562fedab48092e1b7"),
        ReferenceService.Operation.UNREFERENCE
      )
      .references(new FileReference("75e8694ba0bce5bc36d74216e80b08f4f4734e1d"))
      .build();
    refService.emit(message);
    for(TransactionElement txel : transactions) txel.commit();

    final File expectedFile = new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d");
    Assert.assertTrue(expectedFile.exists());
    final String contents = new String(Files.readAllBytes(expectedFile.toPath()));
    Assert.assertEquals("123456\tjpeg\t1024\t768\tmy_file.jpg\t" + System.lineSeparator(), contents);
  }

  FileRecord newFileRecord() {
    final FileRecord record = new FileRecord();
    record.setHash("75e8694ba0bce5bc36d74216e80b08f4f4734e1d");
    record.setDefaultFilename("my_file.jpg");
    record.setFiletype("jpeg");
    record.setHeight(768);
    record.setWidth(1024);
    record.setSize(123456);
    record.setPictureId("7da9b60d0194d61562fedab48092e1b7");
    return record;
  }
}
