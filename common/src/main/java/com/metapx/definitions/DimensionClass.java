package com.metapx.definitions;

import io.vertx.core.json.JsonObject;

/**
 * Represents a desired dimension for an image, which can also be approximate. 
 */
public interface DimensionClass extends Dimension {
  /**
   * @return the name of this dimension class.
   */
  public String getName();
  
  /**
   * @return whether an image can approximately match this dimension class.
   */
  public boolean isApproximate();
  
  /**
   * @return a JSON representation of this dimension.
   */
  @Override
  public default JsonObject toJson() {
    return new JsonObject()
      .put("width", getWidth())
      .put("height", getHeight())
      .put("approximate", isApproximate())
      .put("name", getName());
  }
  
  /**
   * Creates a Dimension from a Json representation. 
   * @param width
   * @param height
   */
  public static DimensionClass fromJson(JsonObject dimension) {
    return new DimensionClass() {
      @Override
      public final int getWidth() {
        return dimension.getInteger("width");
      }

      @Override
      public final int getHeight() {
        return dimension.getInteger("height");
      }

      @Override
      public String getName() {
        return dimension.getString("name");
      }

      @Override
      public boolean isApproximate() {
        return dimension.getBoolean("approximate");
      }
    };
  }
}
