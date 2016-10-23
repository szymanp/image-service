package com.metapx.server.data_model.resource;

import static com.metapx.server.data_model.jooq.Tables.USERS;

import com.metapx.server.data_model.domain.User;
import com.metapx.server.data_model.jooq.tables.records.UsersRecord;
import com.metapx.server.data_model.resource.PasswordManager.Default;
import com.metapx.server.data_model.resource.infrastructure.CrudError;
import com.metapx.server.data_model.resource.infrastructure.Key;
import com.metapx.server.data_model.resource.infrastructure.ReaderService;
import com.metapx.server.data_model.resource.infrastructure.Resource;
import com.metapx.server.data_model.resource.infrastructure.ResourceIdentifier;
import com.metapx.server.data_model.resource.infrastructure.WriterService;

public class UserService implements ReaderService<User, Integer>, WriterService<User, Integer> {
  
  public Class<User> getRepresentationClass() {
    return User.class;
  }
  
  public Key<Integer> createKey(String key) {
    return new UserKey(key);
  }
  
  public Resource<User> read(Integer key, ReadParameters params) {
    final UsersRecord userRecord = params.getDslContext()
        .selectFrom(USERS)
        .where(USERS.ID.equal(key))
        .fetchOne();
    
    if (userRecord == null) throw CrudError.notFound();
    
    return new Resource<User>(new User(userRecord), params.getUrlResolver().getUrl(params.getResourceIdentifier()));
  }
  
  public Resource<User> create(User user, CreateParameters params) {
    // What should it do?
    // - Validate that all required fields are filled in.
    // - Validate that the user with the given handle or email does not already exist.
    // - Create the user - mark as inactive
    // - Return a result containing:
    //   - a Location Header for the new user resource
    //   - the object itself in the body
    final UsersRecord existing = params.getDslContext().selectFrom(USERS)
    .where(
      USERS.HANDLE.equal(user.getHandle())
      .or(USERS.EMAIL_ADDRESS.equal(user.getEmailAddress()))
    ).fetchOne();

    if (existing != null) {
      if (existing.getEmailAddress() != null && existing.getEmailAddress().equals(user.getEmailAddress())) {
        throw CrudError.validationFailure(
            User.class, 
            String.format("A user with email address \"%s\" already exists", user.getEmailAddress()));
      } else if (existing.getHandle().equals(user.getHandle())) {
        throw CrudError.validationFailure(
            User.class,
            String.format("A user with handle \"%s\" already exists", user.getHandle()));
      } else {
        throw new AssertionError();
      }
    }
    
    // Initial setup for a user.
    // TODO
    user.record().setSalt("my-salt");
    user.record().setActive(true);
    
    this.modify(user);
    
    user.save(params.getDslContext());
    
    final String url = params.getUrlResolver().getUrl(new ResourceIdentifier(User.class, new UserKey(user.getId())));
    return new Resource<User>(user, url);
  }
  
  public Resource<User> update(Integer key, User user, UpdateParameters params) {
    final UsersRecord userRecord = params.getDslContext().selectFrom(USERS)
      .where(USERS.ID.equal(key))
      .forUpdate()
      .fetchOne();
    
    if (userRecord == null) throw CrudError.notFound();
    
    final User updatedUser = new User(userRecord, user);
    this.modify(updatedUser);
    updatedUser.save(params.getDslContext());
    
    return new Resource<User>(updatedUser, params.getUrlResolver().getUrl(params.getResourceIdentifier())); 
  }
  
  public void delete(Integer key, DeleteParameters params) {
    // TODO
  }
  
  protected void modify(User user) {
    final UsersRecord record = user.record();
    
    if (user.record().changed(USERS.PASSWORD)) {
      Default passwordManager = new PasswordManager.Default();
      record.setPassword(passwordManager.computeHash(record.getPassword(), record.getSalt()));
    }
  }
}
