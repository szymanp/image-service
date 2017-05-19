package com.metapx.local_picture_repo.scaling;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.imgscalr.Scalr;

import com.metapx.git_metadata.core.HashPath;
import com.metapx.git_metadata.core.HashPath.Target;
import com.metapx.local_picture_repo.ResolvedFile;

public class ScaledPictureProvider {
  final private HashPath hashPath;
  
  public ScaledPictureProvider(File storageDir) {
    this.hashPath = new HashPath(storageDir);
  }
  
  /**
   * @param original
   * @param dim
   * @return a scaled image file if it already exists, otherwise an empty optional.
   */
  public Optional<File> getScaledImage(ResolvedFile original, Dimension dim) {
    return getTargetFile(original, dim);
  }
  
  public File getScaledImageSync(ResolvedFile original, Dimension dim) throws IOException {
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
      throw new IOException("Another process is already creating the image file.");
    }
    
    return targetFile;
  }
  
  public Future<File> getScaledImageAsync(ResolvedFile original, Dimension dim) {
    return null;
  }

  private Optional<File> getTargetFile(ResolvedFile original, Dimension dim) {
    return hashPath.getTargetIfExists(original.getHash())
      .map(target -> getDimensionFile(target.getFile(), dim));
  }

  private File getDimensionFile(File hashDir, Dimension dim, String suffix) {
    return new File(hashDir, dim.getWidth() + "x" + dim.getHeight() + suffix);
  }

  private File getDimensionFile(File hashDir, Dimension dim) {
    return getDimensionFile(hashDir, dim, "");
  }
}
