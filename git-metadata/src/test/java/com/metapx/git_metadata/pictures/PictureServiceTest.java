package com.metapx.git_metadata.pictures;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.metapx.git_metadata.core.MockIdService;
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
  public MockIdService idService;
  public ReferenceService refService;
  public List<TransactionElement> transactions;
  public List<ReferenceService.Message> messages;

  @Before
  public void setUp() throws Exception {
    transactions = new ArrayList<TransactionElement>();
    messages = new ArrayList<ReferenceService.Message>();
    idService = new MockIdService(new File(folder.getRoot(), "ids"), txel -> transactions.add(txel));
    idService.nextId = "75e8694ba0bce5bc36d74216e80b08f4f4734e1d";
    refService = new ReferenceService();
    pictureService = new PictureService(folder.getRoot(), txel -> transactions.add(txel), idService, refService);

    refService.register(Picture.class, Operation.UNREFERENCE, message -> messages.add(message));
    refService.register(Picture.class, Operation.REFERENCE, message -> messages.add(message));
  }

  @Test
  public void testCreateWithFiles() throws Exception {
    final Picture picture = pictureService.create();
    picture.files().append(new MemberFile("abcdef", Role.ROOT));
    picture.files().append(new MemberFile("qwerty", Role.THUMBNAIL));
    picture.files().append(new MemberFile("123456", Role.THUMBNAIL));

    for(TransactionElement txel : transactions) txel.commit();

    final File expectedFile = new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d/files");
    Assert.assertTrue(expectedFile.exists());
    final String contents = new String(Files.readAllBytes(expectedFile.toPath()));
    Assert.assertEquals("abcdef\troot" + System.lineSeparator()
                       +"qwerty\tthumbnail" + System.lineSeparator()
                       +"123456\tthumbnail" + System.lineSeparator(), contents);
  }

  @Test
  public void testReferencesOnCreate() throws Exception {
    newPicture();
    for(TransactionElement txel : transactions) txel.commit();

    Assert.assertEquals(3, messages.size());
    Assert.assertEquals(Operation.REFERENCE, messages.get(0).getOperation());
    Assert.assertEquals(Operation.REFERENCE, messages.get(1).getOperation());
    Assert.assertEquals(Operation.REFERENCE, messages.get(2).getOperation());

    final List<FileReference> refs = Stream.concat(
        messages.get(0).getReferences(FileReference.class),
        Stream.concat(
          messages.get(1).getReferences(FileReference.class),
          messages.get(2).getReferences(FileReference.class)
        )
    ).collect(Collectors.toList());
    Assert.assertEquals(3, refs.size());
    Assert.assertEquals("abcdef", refs.get(0).getObjectId());
    Assert.assertEquals("qwerty", refs.get(1).getObjectId());
    Assert.assertEquals("123456", refs.get(2).getObjectId());
  }

  @Test
  public void testReferencesOnUpdate() throws Exception {
    final Picture picture = newPicture();
    for(TransactionElement txel : transactions) txel.commit();

    messages.clear();

    // Update the picture
    picture.files().remove(new MemberFile("abcdef", Role.ROOT));
    picture.files().append(new MemberFile("987654", Role.THUMBNAIL));

    Assert.assertEquals("qwerty 123456 987654",
      picture.files().stream().map(file -> file.getFileHash()).collect(Collectors.joining(" "))
    );

    Assert.assertEquals(2, messages.size());
    Assert.assertEquals(Operation.UNREFERENCE, messages.get(0).getOperation());
    Assert.assertEquals(Operation.REFERENCE, messages.get(1).getOperation());

    final List<FileReference> unref = messages.get(0).getReferences(FileReference.class).collect(Collectors.toList());
    final List<FileReference> ref = messages.get(1).getReferences(FileReference.class).collect(Collectors.toList());

    Assert.assertEquals(1, unref.size());
    Assert.assertEquals(1, ref.size());

    Assert.assertEquals("abcdef", unref.get(0).getObjectId());
    Assert.assertEquals("987654", ref.get(0).getObjectId());
  }

  private Picture newPicture() {
    final Picture picture = pictureService.create();
    pictureService.pictures().append(picture);
    picture.files().append(new MemberFile("abcdef", Role.ROOT));
    picture.files().append(new MemberFile("qwerty", Role.THUMBNAIL));
    picture.files().append(new MemberFile("123456", Role.THUMBNAIL));
    return picture;
  }
}
