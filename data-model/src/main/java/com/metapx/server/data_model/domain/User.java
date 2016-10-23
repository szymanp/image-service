package com.metapx.server.data_model.domain;

import java.util.Arrays;

import org.jooq.DSLContext;
import org.jooq.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.metapx.server.data_model.jooq.tables.records.UsersRecord;

public class User {
  private UsersRecord record;
  
  private String password;

  public User() {
    record = new UsersRecord();
  }

  public User(UsersRecord record) {
    this.record = record;
  }
  
  public User(UsersRecord originalRecord, User updatedValues) {
    this.record = originalRecord;

    Arrays.stream(updatedValues.record.fields())
    .filter((field) -> updatedValues.record.changed(field))
    .forEach((field) -> {
      @SuppressWarnings("unchecked")
      Field<Object> _field = (Field<Object>) field;
      record.set(_field, updatedValues.record.get(field));
    });
  }

  public Integer getId() { return record.getId(); }

  public String getHandle() { return record.getHandle(); }
  public void setHandle(String handle) { record.setHandle(handle); }

  public String getDisplayName() { return record.getDisplayName(); }
  public void setDisplayName(String displayName) { record.setDisplayName(displayName); }

  public String getEmailAddress() { return record.getEmailAddress(); }
  public void setEmailAddress(String emailAddress) { record.setEmailAddress(emailAddress); }
  
  @JsonIgnore
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

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
  
  public UsersRecord record() {
    return this.record;
  }
  
  @Override
  public String toString() {
    return "[" + this.getClass().getSimpleName() + ": " + getHandle() + "]";
  }
}
