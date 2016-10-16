package com.metapx.server.data_model.resource.infrastructure;

import org.jooq.DSLContext;

import com.metapx.server.data_model.domain.User;
import com.metapx.server.data_model.resource.UserKey;
import com.metapx.server.data_model.resource.UserService;
import com.metapx.server.data_model.resource.infrastructure.ReaderService.ReadParameters;
import com.metapx.server.data_model.resource.infrastructure.WriterService.CreateParameters;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * @param <T> representation class
 * @param <K> key class, e.g. Integer
 */
public class ServiceRunner {
  
  private final UserService reader;
  private final UserService writer;
  private final UrlResolver urlResolver;
  
  public ServiceRunner(UrlResolver urlResolver) {
    reader = new UserService();
    writer = reader;
    this.urlResolver = urlResolver;
  }
  
  public Resource<User> read(String key, DSLContext dslContext) throws CrudError {
    final Key<Integer> keyObject = new UserKey(key);
    if (keyObject.isValid()) {
      final ReadParametersImpl parameters = new ReadParametersImpl();
      parameters.dslContext = dslContext;
      parameters.urlResolver = this.urlResolver;
      parameters.resourceIdentifier = new ResourceIdentifier(User.class, keyObject);
      
      return reader.read(keyObject.getValue(), parameters);
    } else {
      throw CrudError.notFound(keyObject);
    }
  }
  
  public Resource<User> create(String jsonString, DSLContext dslContext) throws CrudError {
    final CreateParametersImpl parameters = new CreateParametersImpl();
    parameters.dslContext = dslContext;
    parameters.urlResolver = this.urlResolver;
    
    final User user = Json.decodeValue(jsonString, User.class);
    
    return writer.create(user, parameters);
  }
  
  private static class RequestParametersImpl implements RequestParameters {
    public DSLContext dslContext;
    public UrlResolver urlResolver;

    @Override
    public DSLContext getDslContext() {
      return dslContext;
    }

    @Override
    public UrlResolver getUrlResolver() {
      return urlResolver;
    }
  }
  
  private static class ReadParametersImpl extends RequestParametersImpl implements ReadParameters {
    public ResourceIdentifier resourceIdentifier;

    @Override
    public ResourceIdentifier getResourceIdentifier() {
      return resourceIdentifier;
    }
  }
  
  private static class CreateParametersImpl extends RequestParametersImpl implements CreateParameters {
  }
}
