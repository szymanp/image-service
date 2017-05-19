package com.metapx.local_picture_repo.scaling;

public enum Dimensions implements Dimension {
  THUMBNAIL(160, 120),
  SMALL(320, 240),
  MEDIUM(640, 480),
  LARGE(1024, 768),
  XLARGE(2400, 1800);

  final int width;
  final int height;

  Dimensions(int width, int height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }
  
  @Override
  public boolean isApproximate() {
    return true;
  }
}
