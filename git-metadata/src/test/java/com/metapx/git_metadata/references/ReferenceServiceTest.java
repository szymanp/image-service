package com.metapx.git_metadata.references;

import org.junit.Test;

import com.metapx.git_metadata.groups.GroupReference;
import com.metapx.git_metadata.pictures.PictureReference;
import com.metapx.git_metadata.references.ReferenceService.Message;
import com.metapx.git_metadata.references.ReferenceService.Operation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;

public class ReferenceServiceTest {
  public ReferenceService refService;

  @Before
  public void setUp() throws Exception {
    refService = new ReferenceService();
  }

  @Test
  public void testReceiveAll() throws Exception {
    final List<Message<?, ?>> messages = new ArrayList<Message<?, ?>>();
    refService.messages().subscribe(message -> messages.add(message));

    refService.emit(Message.create(Reference.create(String.class, "123"), Operation.REFERENCE, Reference.create(String.class, "234")));
    refService.emit(Message.create(Reference.create(Integer.class, 123), Operation.REFERENCE, Reference.create(String.class, "234")));

    Assert.assertEquals(2, messages.size());
    Assert.assertEquals("123", messages.get(0).source().getObjectId());
    Assert.assertEquals(123, messages.get(1).source().getObjectId());
  }

  @Test
  public void testReceiveTyped() throws Exception {
    final List<Message<PictureReference, GroupReference>> messages = new ArrayList<Message<PictureReference, GroupReference>>();
    refService.messages(PictureReference.class, GroupReference.class).subscribe(message -> messages.add(message));

    refService.emit(Message.create(new PictureReference("123"), Operation.REFERENCE, new PictureReference("234")));
    refService.emit(Message.create(new PictureReference("123"), Operation.REFERENCE, new GroupReference("234")));
    refService.emit(Message.create(new PictureReference("123"), Operation.UNREFERENCE, new GroupReference("345")));

    Assert.assertEquals(2, messages.size());
    Assert.assertEquals("123", messages.get(0).source().getObjectId());
    Assert.assertEquals("123", messages.get(1).source().getObjectId());

    Assert.assertEquals("234", messages.get(0).target().getObjectId());
    Assert.assertEquals("345", messages.get(1).target().getObjectId());

    Assert.assertEquals(Operation.REFERENCE, messages.get(0).operation());
    Assert.assertEquals(Operation.UNREFERENCE, messages.get(1).operation());
  }
}
