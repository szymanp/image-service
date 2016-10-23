package com.metapx.server.data_model.resource.infrastructure;

import org.jooq.DSLContext;

import com.metapx.server.data_model.resource.infrastructure.ReaderService.ReadParameters;
import com.metapx.server.data_model.resource.infrastructure.WriterService.CreateParameters;
import com.metapx.server.data_model.resource.infrastructure.WriterService.UpdateParameters;

import io.vertx.core.json.Json;

/**
 * @param <T> representation class
 * @param <K> key class, e.g. Integer
 */
public class ServiceRunner<T, K> {
  
  private final ResourceService<T, K> service;
  private final UrlResolver urlResolver;
  
  public static <T, K> ServiceRunner<T, K> create(ResourceService<T, K> service, UrlResolver urlResolver) {
    return new ServiceRunner<T, K>(service, urlResolver);
  }
  
  public ServiceRunner(ResourceService<T, K> service, UrlResolver urlResolver) {
    this.service = service;
    this.urlResolver = urlResolver;
  }
  
  public Resource<T> read(String key, DSLContext dslContext) throws CrudError {
    final Key<K> keyObject = service.createKey(key);
    if (keyObject.isValid()) {
      final ReadParametersImpl parameters = new ReadParametersImpl();
      parameters.dslContext = dslContext;
      parameters.urlResolver = this.urlResolver;
      parameters.resourceIdentifier = new ResourceIdentifier(service.getRepresentationClass(), keyObject);
      
      return reader().read(keyObject.getValue(), parameters);
    } else {
      throw CrudError.notFound(keyObject);
    }
  }
  
  public Resource<T> create(String jsonString, DSLContext dslContext) throws CrudError {
    final CreateParametersImpl parameters = new CreateParametersImpl();
    parameters.dslContext = dslContext;
    parameters.urlResolver = this.urlResolver;
    
    final T representation = Json.decodeValue(jsonString, service.getRepresentationClass());
    
    return writer().create(representation, parameters);
  }
  
  public Resource<T> update(String key, String jsonString, DSLContext dslContext) throws CrudError {
    final Key<K> keyObject = service.createKey(key);
    if (keyObject.isValid()) {
      final UpdateParametersImpl parameters = new UpdateParametersImpl();
      parameters.dslContext = dslContext;
      parameters.urlResolver = this.urlResolver;
      parameters.resourceIdentifier = new ResourceIdentifier(service.getRepresentationClass(), keyObject);
      
      final T representation = Json.decodeValue(jsonString, service.getRepresentationClass());
      return writer().update(keyObject.getValue(), representation, parameters);
    } else {
      throw CrudError.notFound(keyObject);
    }
  }
  
  private ReaderService<T, K> reader() {
    if (this.service instanceof ReaderService) {
      return (ReaderService<T, K>) this.service;
    }
    throw new RuntimeException("Resources of this type cannot be read");
  }
  
  private WriterService<T, K> writer() {
    if (this.service instanceof WriterService) {
      return (WriterService<T, K>) this.service;
    }
    throw new RuntimeException("Resources of this type are not writable");
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

  private static class UpdateParametersImpl extends RequestParametersImpl implements UpdateParameters {
    public ResourceIdentifier resourceIdentifier;

    @Override
    public ResourceIdentifier getResourceIdentifier() {
      return resourceIdentifier;
    }
  }
}
