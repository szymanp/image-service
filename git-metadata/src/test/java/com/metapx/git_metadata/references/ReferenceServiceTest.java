package com.metapx.git_metadata.references;

import org.junit.Test;

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
  public void testReceive() throws Exception {
    final List<Message> messages = new ArrayList<Message>();
    refService.register(String.class, Operation.REFERENCE, message -> messages.add(message));

    refService.emit(new Message(Reference.create(String.class, "123"), Operation.REFERENCE));
    refService.emit(new Message(Reference.create(String.class, "234"), Operation.UNREFERENCE));

    Assert.assertEquals(1, messages.size());
    Assert.assertEquals("123", messages.get(0).getOrigin().getObjectId());
  }

  @Test
  public void testMessage() throws Exception {
    final Message m = ReferenceService
      .newMessageBuilder(Reference.create(String.class, "123"), Operation.REFERENCE)
      .references(new StringReference(Integer.class, "123"))
      .references(new StringReference(Integer.class, "234"))
      .references(Reference.create(Long.class, 123))
      .build();

    Assert.assertEquals(Operation.REFERENCE, m.getOperation());
    Assert.assertEquals(String.class, m.getOrigin().getObjectClass());
    Assert.assertEquals("123", m.getOrigin().getObjectId());
    
    Assert.assertEquals(2, m.getReferences(StringReference.class).count());
    Assert.assertEquals(1, m.getReferences(Reference.class).count());
  }

  private static class StringReference extends Reference<String> {
    StringReference(Class<?> clazz, String id) {
      super(clazz, id);
    }
  }
}
