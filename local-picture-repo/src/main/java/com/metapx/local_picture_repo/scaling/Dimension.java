package com.metapx.local_picture_repo.scaling;

public interface Dimension {
  public int getWidth();

  public int getHeight();
  
  public boolean isApproximate();

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
