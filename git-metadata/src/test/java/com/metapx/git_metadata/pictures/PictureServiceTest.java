package com.metapx.git_metadata.pictures;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.metapx.git_metadata.core.IdService;
import com.metapx.git_metadata.core.TransactionElement;
import com.metapx.git_metadata.files.FileReference;
import com.metapx.git_metadata.pictures.Picture.Role;
import com.metapx.git_metadata.references.ReferenceService;
import com.metapx.git_metadata.references.ReferenceService.Operation;

import org.junit.Assert;
import org.junit.Before;

public class PictureServiceTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  public PictureService pictureService;
  public IdService idService;
  public ReferenceService refService;
  public List<TransactionElement> transactions;
  public List<ReferenceService.Message> messages;

  @Before
  public void setUp() throws Exception {
    transactions = new ArrayList<TransactionElement>();
    messages = new ArrayList<ReferenceService.Message>();
    idService = new IdService(new File(folder.getRoot(), "ids"), txel -> transactions.add(txel));
    refService = new ReferenceService();
    pictureService = new PictureService(folder.getRoot(), txel -> transactions.add(txel), idService, refService);

    refService.register(Picture.class, Operation.REFERENCE, message -> messages.add(message));
  }

  @Test
  public void testCreate() throws Exception {
    final Picture picture = pictureService.create();
    picture.setHash("75e8694ba0bce5bc36d74216e80b08f4f4734e1d");
    picture.getFiles().add(new Picture.FileLine("abcdef", Role.ROOT));
    picture.getFiles().add(new Picture.FileLine("qwerty", Role.THUMBNAIL));
    picture.getFiles().add(new Picture.FileLine("123456", Role.THUMBNAIL));
    pictureService.update(picture);

    for(TransactionElement txel : transactions) txel.commit();

    final File expectedFile = new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d");
    Assert.assertTrue(expectedFile.exists());
    final String contents = new String(Files.readAllBytes(expectedFile.toPath()));
    Assert.assertEquals("abcdef\troot" + System.lineSeparator()
                       +"qwerty\tthumbnail" + System.lineSeparator()
                       +"123456\tthumbnail" + System.lineSeparator(), contents);
  }

  @Test
  public void testReferencesOnCreate() throws Exception {
    final Picture picture = pictureService.create();
    picture.setHash("75e8694ba0bce5bc36d74216e80b08f4f4734e1d");
    picture.getFiles().add(new Picture.FileLine("abcdef", Role.ROOT));
    picture.getFiles().add(new Picture.FileLine("qwerty", Role.THUMBNAIL));
    picture.getFiles().add(new Picture.FileLine("123456", Role.THUMBNAIL));
    pictureService.update(picture);

    for(TransactionElement txel : transactions) txel.commit();

    Assert.assertEquals(1, messages.size());
    final List<FileReference> refs = messages.get(0).getReferences(FileReference.class).collect(Collectors.toList());
    Assert.assertEquals(3, refs.size());
    Assert.assertEquals("abcdef", refs.get(0).getObjectId());
    Assert.assertEquals("qwerty", refs.get(1).getObjectId());
    Assert.assertEquals("123456", refs.get(2).getObjectId());
  }
}
