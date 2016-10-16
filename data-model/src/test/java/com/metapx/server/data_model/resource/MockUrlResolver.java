package com.metapx.server.data_model.resource;

public class MockUrlResolver implements UrlResolver {
  @Override
  public String getUrl(ResourceIdentifier resourceId) {
    return "http://example.org/" 
        + resourceId.getResourceClass().getSimpleName().toLowerCase() + "/"
        + resourceId.getKey().toUrlString();
  }
}
