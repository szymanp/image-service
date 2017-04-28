package com.metapx.git_metadata.pictures;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.metapx.git_metadata.core.MockIdService;
import com.metapx.git_metadata.core.TransactionElement;
import com.metapx.git_metadata.groups.GroupReference;
import com.metapx.git_metadata.pictures.Picture.Role;
import com.metapx.git_metadata.references.ReferenceService;
import com.metapx.git_metadata.references.ReferenceService.Message;
import com.metapx.git_metadata.references.ReferenceService.Operation;

import io.reactivex.subjects.PublishSubject;

import org.junit.Assert;
import org.junit.Before;

public class PictureServiceTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  public PictureService pictureService;
  public MockIdService idService;
  public ReferenceService refService;
  public List<TransactionElement> transactions;

  @Before
  public void setUp() throws Exception {
    transactions = new ArrayList<TransactionElement>();
    idService = new MockIdService(new File(folder.getRoot(), "ids"), txel -> transactions.add(txel));
    idService.nextId = "75e8694ba0bce5bc36d74216e80b08f4f4734e1d";
    refService = new ReferenceService();
    pictureService = new PictureService(folder.getRoot(), txel -> transactions.add(txel), idService, refService);
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
  public void testUpdateOfFiles() throws Exception {
    final Picture picture = pictureService.create();
    picture.files().append(new MemberFile("abcdef", Role.ROOT));
    picture.files().append(new MemberFile("qwerty", Role.THUMBNAIL));
    picture.files().append(new MemberFile("123456", Role.THUMBNAIL));
    for(TransactionElement txel : transactions) txel.commit();

    picture.files().remove(picture.files().findWithKey("qwerty").get());
    for(TransactionElement txel : transactions) txel.commit();

    final File expectedFile = new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d/files");
    Assert.assertTrue(expectedFile.exists());
    final String contents = new String(Files.readAllBytes(expectedFile.toPath()));
    Assert.assertEquals("abcdef\troot" + System.lineSeparator()
                       +"123456\tthumbnail" + System.lineSeparator(), contents);
  }

  @Test
  public void testList() throws Exception {
    idService.nextId = "0100000000001";
    pictureService.pictures().append(pictureService.create());
    idService.nextId = "0200000000002";
    pictureService.pictures().append(pictureService.create());

    for(TransactionElement txel : transactions) txel.commit();

    final List<Picture> pictures = pictureService.pictures().list();

    Assert.assertEquals(2, pictures.size());
    Assert.assertEquals("0100000000001", pictures.get(0).getHash());
    Assert.assertEquals("0200000000002", pictures.get(1).getHash());
  }

  @Test
  public void testReferencesOnCreate() throws Exception {
    final PublishSubject<Object> done = PublishSubject.create();
    refService.messages()
      .takeUntil(done)
      .toList()
      .subscribe(messages -> {
        Assert.assertEquals(3, messages.size());
        Assert.assertArrayEquals(
          new Operation[] { Operation.REFERENCE, Operation.REFERENCE, Operation.REFERENCE }, 
          messages.stream().map(m -> m.operation()).toArray()
        );
        Assert.assertArrayEquals(
          new String[] { "abcdef", "qwerty", "123456"}, 
          messages.stream().map(m -> m.target().getObjectId()).toArray()
        );
      });

    newPicture();
    for(TransactionElement txel : transactions) txel.commit();
    done.onNext(0);
  }

  @Test
  public void testReferencesOnUpdate() throws Exception {
    final Picture picture = newPicture();
    for(TransactionElement txel : transactions) txel.commit();

    final PublishSubject<Object> done = PublishSubject.create();
    refService.messages()
      .takeUntil(done)
      .toList()
      .subscribe(messages -> {
        Assert.assertArrayEquals(
          new Operation[] { Operation.UNREFERENCE, Operation.REFERENCE }, 
          messages.stream().map(m -> m.operation()).toArray()
        );
        Assert.assertArrayEquals(
          new String[] { "abcdef", "987654" }, 
          messages.stream().map(m -> m.target().getObjectId()).toArray()
        );
      });

    // Update the picture
    picture.files().remove(new MemberFile("abcdef", Role.ROOT));
    picture.files().append(new MemberFile("987654", Role.THUMBNAIL));
    done.onNext(0);

    Assert.assertEquals("qwerty 123456 987654",
      picture.files().stream().map(file -> file.getFileHash()).collect(Collectors.joining(" "))
    );
  }

  @Test
  public void testAssignGroups() throws Exception {
    final Picture picture = pictureService.create();
    pictureService.pictures().append(picture);
    picture.groups().append(new GroupReference("21302130"));
    picture.groups().append(new GroupReference("31403130"));

    for(TransactionElement txel : transactions) txel.commit();

    final File expectedFile = new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d/groups");
    Assert.assertTrue(expectedFile.exists());
    final String contents = new String(Files.readAllBytes(expectedFile.toPath()));
    Assert.assertEquals("21302130" + System.lineSeparator()
                       +"31403130" + System.lineSeparator(), contents);
  }

  @Test
  public void testReadGroupsFromFile() throws Exception {
    final Picture picture = pictureService.create();
    pictureService.pictures().append(picture);

    for(TransactionElement txel : transactions) txel.commit();

    final File groupsFile = new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d/groups");
    Files.write(groupsFile.toPath(), ("21302130" + System.lineSeparator() + "31403140" + System.lineSeparator()).getBytes());

    final List<GroupReference> groups = picture.groups().list();
    Assert.assertEquals(2, groups.size());
    Assert.assertEquals("21302130", groups.get(0).getObjectId());
    Assert.assertEquals("31403140", groups.get(1).getObjectId());
  }

  @Test
  public void testAssignGroupsViaReference() throws Exception {
    final Picture picture = pictureService.create();
    pictureService.pictures().append(picture);
    picture.groups().append(new GroupReference("21302130"));
    picture.groups().append(new GroupReference("31403130"));

    refService.emit(Message.create(new GroupReference("21302130"), Operation.UNREFERENCE, picture.getReference()));
    refService.emit(Message.create(new GroupReference("98709870"), Operation.REFERENCE, picture.getReference()));

    final List<GroupReference> groups = picture.groups().list();
    Assert.assertEquals(2, groups.size());
    Assert.assertEquals("31403130", groups.get(0).getObjectId());
    Assert.assertEquals("98709870", groups.get(1).getObjectId());
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
