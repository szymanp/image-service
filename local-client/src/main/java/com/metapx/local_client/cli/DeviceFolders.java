package com.metapx.local_client.cli;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.groups.Device;
import com.metapx.git_metadata.groups.DeviceFolder;
import com.metapx.git_metadata.groups.Group;

public class DeviceFolders {
  final private MetadataRepository metadata;
  final private Configuration conf;

  private Device device;
  private Map<Path, DeviceFolder> folders = new HashMap<Path, DeviceFolder>();

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

  public DeviceFolder getFolder(File path) {
    return getFolder(path.toPath());
  }

  public DeviceFolder getFolder(Path path) {
    final Path normalizedPath = path.normalize();

    if (folders.containsKey(normalizedPath)) {
      return folders.get(normalizedPath);
    } else {
      Group parent = getDeviceGroup();
      DeviceFolder current = null;
      for(Path pathElement : decompose(normalizedPath)) {
        current = getFolderOrCreate(pathElement, parent);
        parent = current;
      }
      return current;
    }
  }

  private DeviceFolder getFolderOrCreate(Path path, Group parent) {
    if (folders.containsKey(path)) {
      return folders.get(path);
    } else {
      final String name = path.getNameCount() > 0 ? 
        path.getFileName().toString()
        : removeTrailingSlash(path.toString());
      final DeviceFolder folder = parent.subgroups().stream()
        .filter(subgroup -> subgroup.getName().equals(name))
        .filter(subgroup -> subgroup instanceof DeviceFolder)
        .map(subgroup -> (DeviceFolder) subgroup)
        .findFirst()
        .orElseGet(() -> {
          final DeviceFolder result = metadata.groupApi().create(DeviceFolder.class, name);
          parent.subgroups().append(result);
          return result;
        });
      folders.put(path, folder);
      return folder;
    }
  }

  private List<Path> decompose(Path path) {
    List<Path> parts = new ArrayList<Path>();
    Path current = path;
    while (current != null) {
      parts.add(0, current);
      current = current.getParent();
    }

    // Find the root Path component, if it exists. On Windows this would be a drive letter.
    final Path root = path.getRoot();
    if (root != null) {
      parts.add(0, root);
    }

    return parts;
  }

  private String removeTrailingSlash(String drive) {
    if (drive.endsWith("\\")) {
      return drive.substring(0, drive.length() - 1);
    } else {
      return drive;
    }
  }
}
