package com.metapx.local_picture_repo.scaling;

import io.vertx.core.json.JsonObject;

public interface Dimension {
  public int getWidth();

  public int getHeight();
  
  public boolean isApproximate();
  
  public default JsonObject toJson() {
    return new JsonObject()
      .put("width", getWidth())
      .put("height", getHeight())
      .put("approximate", isApproximate());
  }
  
  public interface NamedDimension extends Dimension {
    public String getName();

    public default JsonObject toJson() {
      return new JsonObject()
        .put("width", getWidth())
        .put("height", getHeight())
        .put("approximate", isApproximate())
        .put("name", getName());
    }
  }
  
  public static Dimension fromJson(JsonObject json) {
    return new Default(json.getInteger("width"), json.getInteger("height"));
  }

  public static class Default implements Dimension {
    final int width;
    final int height;
    
    public Default(int width, int height) {
      this.width = width;
      this.height = height;
    }
    
    public int getWidth() {
      return width;
    }
  
    public int getHeight() {
      return height;
    }

    public boolean isApproximate() {
      return false;
    }
  }
}
