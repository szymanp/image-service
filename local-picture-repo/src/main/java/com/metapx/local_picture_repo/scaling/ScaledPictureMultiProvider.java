package com.metapx.local_picture_repo.scaling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScaledPictureMultiProvider implements ScaledPictureProvider {
  private final List<Entry> providers = new ArrayList<Entry>();

  public void addProvider(File storageDir, String prefix) {
    providers.add(new Entry(new ScaledPictureProviderImpl(storageDir), prefix));
  }
  
  @Override
  public Status getScaledImageStatus(FileWithHash original, Dimension dim) {
    for(Entry entry : providers) {
      final Status status = entry.provider.getScaledImageStatus(original, dim);
      if (status != Status.MISSING) {
        return status;
      }
    }
    return Status.MISSING;
  }

  @Override
  public Optional<File> getScaledImageIfExists(FileWithHash original, Dimension dim) {
    for(Entry entry : providers) {
      final Optional<File> file = entry.provider.getScaledImageIfExists(original, dim);
      if (file.isPresent()) {
        return file;
      }
    }
    return Optional.empty();
  }

  @Override
  public File getScaledImage(FileWithHash original, Dimension dim) throws IOException, InterruptedException {
    final Optional<File> existing = getScaledImageIfExists(original, dim);
    if (existing.isPresent()) {
      return existing.get();
    } else {
      ScaledPictureProvider provider = providers.stream()
         .filter(entry -> entry.matches(original))
         .findFirst()
         .orElseThrow(() -> new RuntimeException("No matching provider.")).provider;

      return provider.getScaledImage(original, dim);
    }
  }

  private static class Entry {
    String prefix;
    ScaledPictureProvider provider;
    
    Entry(ScaledPictureProvider provider, String prefix) {
      this.prefix = prefix;
      this.provider = provider;
    }
    
    boolean matches(FileWithHash file) {
      try {
        return file.getFile().getCanonicalPath().startsWith(prefix);
      } catch (IOException e) {
        return false;
      }
    }
  }
}
