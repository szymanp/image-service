package com.metapx.server.data_model.resource.infrastructure;

import org.jooq.DSLContext;

import com.metapx.server.data_model.domain.User;
import com.metapx.server.data_model.resource.UserKey;
import com.metapx.server.data_model.resource.UserService;
import com.metapx.server.data_model.resource.infrastructure.ReaderService.ReadParameters;

/**
 * @param <T> representation class
 * @param <K> key class, e.g. Integer
 */
public class ServiceRunner {
  
  private final UserService reader;
  private final UrlResolver urlResolver;
  
  public ServiceRunner(UrlResolver urlResolver) {
    reader = new UserService();
    this.urlResolver = urlResolver;
  }
  
  public Resource<User> read(String key, DSLContext dslContext) throws CrudError {
    Key<Integer> keyObject = new UserKey(key);
    if (keyObject.isValid()) {
      ReadParametersImpl parameters = new ReadParametersImpl();
      parameters.dslContext = dslContext;
      parameters.urlResolver = this.urlResolver;
      parameters.resourceIdentifier = new ResourceIdentifier(User.class, keyObject);
      
      return reader.read(keyObject.getValue(), parameters);
    } else {
      throw CrudError.notFound(keyObject);
    }
  }
  
  private static class ReadParametersImpl implements ReadParameters {
    public DSLContext dslContext;
    public UrlResolver urlResolver;
    public ResourceIdentifier resourceIdentifier;

    @Override
    public DSLContext getDslContext() {
      return dslContext;
    }

    @Override
    public UrlResolver getUrlResolver() {
      return urlResolver;
    }

    @Override
    public ResourceIdentifier getResourceIdentifier() {
      return resourceIdentifier;
    }
  }
}
