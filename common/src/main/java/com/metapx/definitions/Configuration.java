package com.metapx.definitions;

public interface Configuration {
  /**
   * Returns the friendly name of this device.
   * This name is used for informing the user which device is associated with certain pictures.
   * 
   * @return the name of this device.
   */
  public String getDeviceName();
  
  /**
   * @return a collection of known dimension classes.
   */
  public DimensionClassGroup getDimensionClasses();
}
