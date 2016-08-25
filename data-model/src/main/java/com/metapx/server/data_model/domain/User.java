package com.metapx.server.data_model.domain;

import org.jooq.DSLContext;

import com.metapx.server.data_model.jooq.Tables;
import com.metapx.server.data_model.jooq.tables.records.UsersRecord;

public class User {
  private Integer id;
  private String handle;
  private String displayName;
  private String emailAddress;

  public User() {}

  public User(UsersRecord record) {
    id = record.getId();
    handle = record.getHandle();
    displayName = record.getDisplayName();
    emailAddress = record.getEmailAddress();
  }
  
  public Integer getId() { return id; }

  public String getHandle() { return handle; }
  public void setHandle(String handle) { this.handle = handle; }

  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }

  public String getEmailAddress() { return emailAddress; }
  public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
  
  public void update(DSLContext dslContext) {
    UsersRecord record = dslContext.newRecord(Tables.USERS, this);
    dslContext.executeUpdate(record);
  }

  public void create(DSLContext dslContext) {
    UsersRecord record = dslContext.newRecord(Tables.USERS, this);
    dslContext.executeInsert(record);
    this.id = record.getId();
  }

  @Override
  public String toString() {
    return "[" + User.class.getSimpleName() + ": " + handle + "]";
  }
}
