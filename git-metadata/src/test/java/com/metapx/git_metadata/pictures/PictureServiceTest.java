package com.metapx.git_metadata.pictures;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.metapx.git_metadata.core.IdService;
import com.metapx.git_metadata.core.TransactionElement;
import com.metapx.git_metadata.pictures.Picture.Role;

import org.junit.Assert;
import org.junit.Before;

public class PictureServiceTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  public PictureService pictureService;
  public IdService idService;
  public List<TransactionElement> transactions;

  @Before
  public void setUp() throws Exception {
    transactions = new ArrayList<TransactionElement>();
    idService = new IdService(new File(folder.getRoot(), "ids"), txel -> transactions.add(txel));
    pictureService = new PictureService(folder.getRoot(), txel -> transactions.add(txel), idService);
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
}
