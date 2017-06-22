package com.metapx.local_client.resources;

public class Rels {
  public enum Link {
    PARENT("parent"),
    CHILDREN("children"),
    SELF("self");
    
    private final String rel;
    
    Link(String rel) {
      this.rel = rel;
    }
    
    public String toString() {
      return rel;
    }
  }
}
