package com.metapx.git_metadata.groups;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.metapx.git_metadata.core.MockIdService;
import com.metapx.git_metadata.core.TransactionSubject;
import com.metapx.git_metadata.core.collections.Collection;
import com.metapx.git_metadata.pictures.PictureReference;

import org.junit.Assert;
import org.junit.Before;

public class GroupServiceTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  public GroupService groupService;
  public MockIdService idService;
  public TransactionSubject transactions;

  @Before
  public void setUp() throws Exception {
    transactions = new TransactionSubject();
    idService = new MockIdService(new File(folder.getRoot(), "ids"), txel -> transactions.addElementToTransaction(txel));
    idService.nextId = "75e8694ba0bce5bc36d74216e80b08f4f4734e1d";
    groupService = new GroupService(folder.getRoot(), transactions, idService);
  }

  @Test
  public void testAppendGroupsAndCheckInMemory() {
    idService.nextId = "75e8694ba0bce5bc36d74216e80b08f4f4734e1d";
    final Tag winter = groupService.create(Tag.class, "winter");
    idService.nextId = "99e8694ba0bce53c36d742a6e80b08f4f4734e11";
    final Tag summer = groupService.create(Tag.class, "summer");

    final Collection<Group> groups = groupService.groups();
    groups.append(winter);
    groups.append(summer);

    List<Group> actual = groups.list();
    Assert.assertEquals(2, actual.size());
    Assert.assertEquals(winter, actual.get(0));
    Assert.assertEquals(summer, actual.get(1));
  }

  @Test
  public void testAppendGroupsAndWrite() throws Exception {
    idService.nextId = "75e8694ba0bce5bc36d74216e80b08f4f4734e1d";
    final Tag winter = groupService.create(Tag.class, "winter");
    idService.nextId = "99e8694ba0bce53c36d742a6e80b08f4f4734e11";
    final Tag summer = groupService.create(Tag.class, "summer");

    final Collection<Group> groups = groupService.groups();
    groups.append(winter);
    groups.append(summer);

    transactions.commit();

    final String contents = new String(Files.readAllBytes(new File(folder.getRoot(), "tree").toPath()));
    Assert.assertEquals("000000000000000000000000000000000000\t75e8694ba0bce5bc36d74216e80b08f4f4734e1d\ttag\twinter" + System.lineSeparator()
                       +"000000000000000000000000000000000000\t99e8694ba0bce53c36d742a6e80b08f4f4734e11\ttag\tsummer" + System.lineSeparator(), contents);
  }

  @Test
  public void testReadGroups() throws Exception {
    final File file = new File(folder.getRoot(), "tree");
    Files.write(
      file.toPath(),
      ("000000000000000000000000000000000000\t75e8694ba0bce5bc36d74216e80b08f4f4734e1d\ttag\twinter" + System.lineSeparator()
       +"000000000000000000000000000000000000\t99e8694ba0bce53c36d742a6e80b08f4f4734e11\ttag\tsummer" + System.lineSeparator())
      .getBytes()
    );

    final Collection<Tag> tags = groupService.groups(Tag.class);
    final List<Tag> actual = tags.list();
    Assert.assertEquals(2, actual.size());
    Assert.assertEquals("winter", actual.get(0).getName());
    Assert.assertEquals("summer", actual.get(1).getName());
  }

  @Test
  public void testAssignPicturesAndCommit() throws Exception {
    idService.nextId = "75e8694ba0bce5bc36d74216e80b08f4f4734e1d";
    final Tag winter = groupService.create(Tag.class, "winter");

    groupService.groups().append(winter);
    winter.pictures().append(new PictureReference("2132130"));
    winter.pictures().append(new PictureReference("3123120"));

    transactions.commit();

    String contents = new String(Files.readAllBytes(new File(folder.getRoot(), "tree").toPath()));
    Assert.assertEquals("000000000000000000000000000000000000\t75e8694ba0bce5bc36d74216e80b08f4f4734e1d\ttag\twinter" + System.lineSeparator(), contents);

    contents = new String(Files.readAllBytes(new File(folder.getRoot(), "75/e8/694ba0bce5bc36d74216e80b08f4f4734e1d").toPath()));
    Assert.assertEquals("2132130" + System.lineSeparator() + "3123120" + System.lineSeparator(), contents);
  }
}
