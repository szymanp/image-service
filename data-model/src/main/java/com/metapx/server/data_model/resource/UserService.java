package com.metapx.server.data_model.resource;

import static com.metapx.server.data_model.jooq.Tables.USERS;

import com.metapx.server.data_model.domain.User;
import com.metapx.server.data_model.jooq.tables.records.UsersRecord;
import com.metapx.server.data_model.resource.infrastructure.CrudError;
import com.metapx.server.data_model.resource.infrastructure.ReaderService;
import com.metapx.server.data_model.resource.infrastructure.Resource;

public class UserService implements ReaderService<User, Integer> {
  
  public Resource<User> read(Integer key, ReadParameters params) {
    final UsersRecord userRecord = params.getDslContext()
        .selectFrom(USERS)
        .where(USERS.ID.equal(key))
        .fetchOne();
    
    if (userRecord == null) throw CrudError.notFound();
    
    return new Resource<User>(new User(userRecord), params.getUrlResolver().getUrl(params.getResourceIdentifier()));
  }
}
