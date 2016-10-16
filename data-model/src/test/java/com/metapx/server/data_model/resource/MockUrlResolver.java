package com.metapx.server.data_model.resource;

import com.metapx.server.data_model.resource.infrastructure.ResourceIdentifier;
import com.metapx.server.data_model.resource.infrastructure.UrlResolver;

public class MockUrlResolver implements UrlResolver {
  @Override
  public String getUrl(ResourceIdentifier resourceId) {
    return "http://example.org/" 
        + resourceId.getResourceClass().getSimpleName().toLowerCase() + "/"
        + resourceId.getKey().toUrlString();
  }
}
