package com.metapx.definitions;

import io.vertx.core.json.JsonObject;

public interface Dimension {

  /** @return the width of the image in pixels. */
  int getWidth();
  
  /** @return the height of the image in pixels. */
  int getHeight();

  /**
   * @return a JSON representation of this dimension.
   */
  public default JsonObject toJson() {
    return new JsonObject()
      .put("width", getWidth())
      .put("height", getHeight());
  }

  /**
   * Creates a new Dimension. 
   * @param width
   * @param height
   * @return a new dimension with the given width and height.
   */
  public static Dimension create(int width, int height) {
    return new Dimension() {
      @Override
      public final int getWidth() {
        return width;
      }

      @Override
      public final int getHeight() {
        return height;
      }
    };
  }

  /**
   * @return a dimension read from the Json object.
   */
  public static Dimension fromJson(JsonObject json) {
    return create(json.getInteger("width"), json.getInteger("height"));
  }
 
}
