package com.metapx.server.data_model;

import static org.junit.Assert.*;

import org.junit.Test;

import com.metapx.server.data_model.domain.User;
import com.metapx.server.data_model.jooq.Tables;
import com.metapx.server.data_model.jooq.tables.records.UsersRecord;

public class ModelTest extends BaseDatabaseTest {

  @Test
  public void testCreateUser() {
    User user = new User();
    user.setHandle("jqt");
    user.setDisplayName("John Q. Test");
    user.save(dslContext);
    
    assertNotNull(user.getId());
    
    User fetchedUser = dslContext.select()
      .from(Tables.USERS)
      .where(Tables.USERS.HANDLE.eq("jqt"))
      .fetchOne(r -> new User((UsersRecord)r));
    
    assertNotNull(fetchedUser);
    assertEquals(user.getId(), fetchedUser.getId());
    assertEquals(user.getDisplayName(), fetchedUser.getDisplayName());
  }
  
  @Test
  public void testDeleteUser() {
    User fetchedUser = dslContext.select()
      .from(Tables.USERS)
      .where(Tables.USERS.HANDLE.eq("Administrator"))
      .fetchOne(r -> new User((UsersRecord)r));
    
    assertNotNull(fetchedUser);
    fetchedUser.delete(dslContext);
    
    int count = dslContext.fetchCount(
        dslContext.select()
        .from(Tables.USERS)
        .where(Tables.USERS.HANDLE.eq("Administrator")));
    
    assertEquals(0, count);
  }
}
