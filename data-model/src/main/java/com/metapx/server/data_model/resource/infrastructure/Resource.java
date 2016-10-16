package com.metapx.server.data_model.resource.infrastructure;

import com.metapx.server.common.Maybe;

public class Resource<T> {
  
  private T representation;
  private String url;
  private Maybe<ETag> etag;
  
  public Resource(T representation, String url) {
    this.representation = representation;
    this.url = url;
    this.etag = Maybe.empty();
  }

  public String getCanonicalUrl() {
    return url;
  }
  
  public Maybe<ETag> getETag() {
    return etag;
  }
  
  public T getRepresentation() {
    return representation;
  }
  
  // TODO links
}
