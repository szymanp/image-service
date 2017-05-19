package com.metapx.local_picture_repo.scaling;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import com.google.common.io.Files;

public class ExifMetadata {
  JpegImageMetadata metadata;
  TiffOutputSet outputSet;
  
  public void readMetadata(File imageFile) throws ImageReadException, IOException, ImageWriteException {
    metadata = (JpegImageMetadata) Imaging.getMetadata(imageFile);
    if (metadata != null) {
      final TiffImageMetadata exif = metadata.getExif();

      if (exif != null) {
        outputSet = exif.getOutputSet();
      }
    }
  }
  
  public Optional<TiffField> getValue(TagInfo field) {
    if (metadata == null) {
      return Optional.empty();
    } else {
      return Optional.ofNullable(metadata.findEXIFValueWithExactMatch(field));
    }
  }
  
  public void setValue(TagInfoAscii field, String value) throws ImageWriteException {
    if (outputSet == null) outputSet = new TiffOutputSet();
    final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
    exifDirectory.removeField(field);
    exifDirectory.add(field, value);
  }
  
  public void writeMetadata(File imageFile, File outputFile) throws IOException, ImageReadException, ImageWriteException {
    if (outputSet == null) {
      Files.copy(imageFile, outputFile);
    } else {
      try (FileOutputStream fos = new FileOutputStream(outputFile);
           OutputStream os = new BufferedOutputStream(fos)) {
        new ExifRewriter().updateExifMetadataLossless(imageFile, os, outputSet);
      }
    }
  }
}
