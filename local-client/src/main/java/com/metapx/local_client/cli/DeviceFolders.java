package com.metapx.local_client.cli;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.groups.Device;

public class DeviceFolders {
  final private MetadataRepository metadata;
  final private Configuration conf;

  private Device device;
  
  public DeviceFolders(Configuration conf, MetadataRepository metadataRepository) {
    this.metadata = metadataRepository;
    this.conf = conf;
  }
 
  public Device getDeviceGroup() {
    if (device == null) {
      final String deviceName = conf.getDeviceName();
      device = metadata.groups(Device.class).stream()
        .filter(device -> device.getName().equalsIgnoreCase(deviceName))
        .findFirst()
        .orElseGet(() -> {
          final Device device = metadata.groupApi().create(Device.class, deviceName);
          metadata.groups().append(device);
          return device;
        });
    }
    return device;
  }
}
