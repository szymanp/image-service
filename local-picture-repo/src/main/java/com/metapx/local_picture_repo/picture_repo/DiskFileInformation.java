package com.metapx.local_picture_repo.picture_repo;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class DiskFileInformation implements FileInformation {
  public static HashCalculator defaultHashCalculator;

  static {
    try {
      defaultHashCalculator = new HashCalculator();
    } catch (Exception e) {
      defaultHashCalculator = null;
    }
  }

  private final File file;
  private final HashCalculator hashCalculator;
  private String hash;
  private boolean read = false;
  private String imageType;
  private int width = -1;
  private int height = -1;

  public DiskFileInformation(File file, HashCalculator hashCalculator) {
    this.file = file;
    this.hashCalculator = hashCalculator;
  }

  public DiskFileInformation(File file) {
    if (defaultHashCalculator == null) {
      throw new RuntimeException("No default hash calculator is set");
    }
    this.file = file;
    this.hashCalculator = defaultHashCalculator;
  }

  /**
   * @return the file that is described by this object
   */
  public File getFile() {
    return file;
  }

  /**
   * @return the digest of this file.
   */
  public String getHash() {
    if (hash == null) {
      try {
        hash = hashCalculator.calculateStringDigest(file);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return hash;
  }

  /**
   * @return the name of the algorithm used to calculate the digest of this file.
   */
  public String getHashAlgorithm() {
    return hashCalculator.getAlgorithm();
  }

  /**
   * @return `true` if this file is an image; otherwise, `false`.
   */
  public boolean isImage() {
    readImage();
    return imageType != null;
  }

  public String getImageType() {
    readImage();
    return imageType;
  }

  /**
   * @return the width of the image in pixels.
   */
  public int getWidth() {
    readImage();
    return width;
  }

  /**
   * @return the height of the image in pixels.
   */
  public int getHeight() {
    readImage();
    return height;
  }

  private void readImage() {
    if (!read) {
      try (ImageInputStream in = ImageIO.createImageInputStream(file)) {
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
        if (readers.hasNext()) {
          ImageReader reader = readers.next();
          try {
            reader.setInput(in);

            width = reader.getWidth(0);
            height = reader.getHeight(0);
            imageType = reader.getFormatName();

            
          } finally {
            reader.dispose();
          }
        }
      } catch (IOException e) {
        // Assume that the file is not an image.
      }
      read = true;
    }
  }
}