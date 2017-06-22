package com.metapx.local_client.resources;

public class ExceptionResource extends Resource {
  public ExceptionResource(Throwable e) {
    super("resource:exception");
    
    data.put("message", e.getMessage());
    data.put("exceptionClass", e.getClass().getName());
  }
}
