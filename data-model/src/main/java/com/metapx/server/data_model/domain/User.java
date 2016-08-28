package com.metapx.server.data_model.domain;

import org.jooq.DSLContext;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.metapx.server.data_model.jooq.tables.records.UsersRecord;

public class User {
  private UsersRecord record;

  public User() {
    record = new UsersRecord();
  }

  public User(UsersRecord record) {
    this.record = record;
  }

  public Integer getId() { return record.getId(); }
  public void setId(Integer id) { record.setId(id); }

  public String getHandle() { return record.getHandle(); }
  public void setHandle(String handle) { record.setHandle(handle); }

  public String getDisplayName() { return record.getDisplayName(); }
  public void setDisplayName(String displayName) { record.setDisplayName(displayName); }

  public String getEmailAddress() { return record.getEmailAddress(); }
  public void setEmailAddress(String emailAddress) { record.setEmailAddress(emailAddress); }

  @JsonIgnore
  public boolean isNew() {
    return (record.key().get(0) == null);
  }

  public void save(DSLContext dslContext) {
    record.attach(dslContext.configuration());
    record.store();
  }
  
  public void delete(DSLContext dslContext) {
    record.attach(dslContext.configuration());
    record.delete();
  }

  @Override
  public String toString() {
    return "[" + this.getClass().getSimpleName() + ": " + getHandle() + "]";
  }
}
