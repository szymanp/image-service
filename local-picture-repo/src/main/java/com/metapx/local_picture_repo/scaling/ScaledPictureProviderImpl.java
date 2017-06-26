package com.metapx.local_picture_repo.scaling;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.imgscalr.Scalr;

import com.metapx.git_metadata.core.HashPath;
import com.metapx.git_metadata.core.HashPath.Target;

public class ScaledPictureProviderImpl implements ScaledPictureProvider {
  final private HashPath hashPath;
  
  public ScaledPictureProviderImpl(File storageDir) {
    this.hashPath = new HashPath(storageDir);
  }
  
  /**
   * @param original
   * @param dim
   * @return a scaled image file if it already exists, otherwise an empty optional.
   */
  public Optional<File> getScaledImageIfExists(FileWithHash original, Dimension dim) {
    return getTargetFile(original, dim);
  }
  
  /**
   * @return the status of the particular scaled image.
   */
  public Status getScaledImageStatus(FileWithHash original, Dimension dim) {
    final Target target = hashPath.getTarget(original.getHash());
    final File targetFile = getDimensionFile(target.getFile(), dim);

    if (targetFile.exists()) {
      return Status.EXISTS;
    }

    final File semaphoreFile = getDimensionFile(target.getFile(), dim, "-lock");
    if (semaphoreFile.exists()) {
      return Status.IN_PROGRESS;
    }
    
    return Status.MISSING;
  }
  
  /**
   * Returns a scaled image file, creating it if necessary.
   * 
   * If another process or thread is already creating the same scaled file, then this method
   * will put the thread to sleep until the file is created.
   * 
   * @param original
   * @param dim
   * @return the scaled file.
   * @throws IOException
   * @throws InterruptedException
   */
  public File getScaledImage(FileWithHash original, Dimension dim) throws IOException, InterruptedException {
    final Target target = hashPath.getTarget(original.getHash());
    final File targetFile = getDimensionFile(target.getFile(), dim);

    if (targetFile.exists()) {
      return targetFile;
    }

    // Create the target directory if it doesn't exist.
    target.prepare();
    targetFile.getParentFile().mkdir();

    final File semaphoreFile = getDimensionFile(target.getFile(), dim, "-lock");
    final File temporaryFile1 = getDimensionFile(target.getFile(), dim, "-tmp-1");
    final File temporaryFile2 = getDimensionFile(target.getFile(), dim, "-tmp-2");
    if (semaphoreFile.createNewFile()) {
      try {
        final BufferedImage originalImage = ImageIO.read(original.getFile());
        final BufferedImage resizedImage = Scalr.resize(originalImage, Scalr.Mode.AUTOMATIC, dim.getWidth(), dim.getHeight());
        ImageIO.write(resizedImage, "JPG", temporaryFile1);

        originalImage.flush();
        resizedImage.flush();
        
        final ExifMetadata exif = new ExifMetadata();
        exif.readMetadata(original.getFile());
        exif.setValue(ExifTagConstants.EXIF_TAG_PROCESSING_SOFTWARE, "metapx");
        exif.setValue(ExifTagConstants.EXIF_TAG_RAW_FILE, original.getHash());
        exif.writeMetadata(temporaryFile1, temporaryFile2);

        temporaryFile1.delete();
        temporaryFile2.renameTo(targetFile);

      } catch (IOException e) {
        throw e;
      } catch (Exception e) {
        throw new IOException(e);
      } finally {
        semaphoreFile.delete();
      }
    } else {
      // The semaphore file already exists.
      final WatchService watcher = FileSystems.getDefault().newWatchService();
      try {
        final WatchKey key = targetFile.getParentFile().toPath().register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        for(;;) {
          watcher.take();
          
          key.pollEvents();
          
          if (targetFile.exists()) {
            break;
          }
        }
      } finally {
        watcher.close();
      }
    }
    return targetFile;
  }

  private Optional<File> getTargetFile(FileWithHash original, Dimension dim) {
    return hashPath.getTargetIfExists(original.getHash())
      .map(target -> getDimensionFile(target.getFile(), dim))
      .flatMap(target -> target.exists() ? Optional.of(target) : Optional.empty());
  }

  private File getDimensionFile(File hashDir, Dimension dim, String suffix) {
    return new File(hashDir, dim.getWidth() + "x" + dim.getHeight() + suffix);
  }

  private File getDimensionFile(File hashDir, Dimension dim) {
    return getDimensionFile(hashDir, dim, "");
  }
}
