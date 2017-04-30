package com.metapx.git_metadata.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.metapx.git_metadata.core.collections.Collection;
import com.metapx.git_metadata.groups.Device;
import com.metapx.git_metadata.groups.DeviceFolder;
import com.metapx.git_metadata.groups.Group;
import com.metapx.git_metadata.pictures.Picture;
import com.metapx.git_metadata.references.Reference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;

public class PictureWorkflowTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  public MetadataRepository repo;

  @Before
  public void setUp() throws Exception {
    final MetadataRepositoryHelper helper = new MetadataRepositoryHelper(folder.getRoot());
    repo = helper.create();
  }

  @Test
  public void testCreatePictureWithDeviceLocation() throws Exception {
    final Picture picture = repo.pictureApi().create();
    repo.pictures().append(picture);

    final Device hdx = repo.groupApi().create(Device.class, "HDX");
    final DeviceFolder dDrive = repo.groupApi().create(DeviceFolder.class, "d:");
    final DeviceFolder work = repo.groupApi().create(DeviceFolder.class, "work");
    work.setParent(dDrive);
    dDrive.setParent(hdx);

    final Collection<Group> groups = repo.groups();
    groups.append(hdx);
    groups.append(dDrive);
    groups.append(work);

    picture.groups().append(hdx.getReference());
    picture.groups().append(work.getReference());

    repo.commit();
    
    Assert.assertEquals(
      join(hdx.getReference().getObjectId(), work.getReference().getObjectId()),
      readFile("pictures", picture.getReference(), Optional.of("groups")));
  }
  
  private String readFile(String filename) throws IOException {
    final File target = new File(repo.getRoot(), filename);
    return new String(Files.readAllBytes(target.toPath()));
  }

  private String readFile(String prefix, Reference<?> ref, Optional<String> suffix) throws IOException {
    File target = new File(repo.getRoot(), prefix);
    final String id = (String) ref.getObjectId();
    target = new File(target, id.substring(0, 2) + "/" + id.substring(2, 4) + "/" + id.substring(4));
    
    if (suffix.isPresent()) {
      target = new File(target, suffix.get());
    }
    
    return new String(Files.readAllBytes(target.toPath()));
  }
  
  private static String join(String... lines) {
    StringBuilder b = new StringBuilder();
    for(int i=0;i<lines.length;i++) b.append(lines[i] + System.lineSeparator());
    return b.toString();
  }

}
