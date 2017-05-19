package com.metapx.local_picture_repo.scaling;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.metapx.local_picture_repo.impl.DiskFileInformation;

public class ScaledPictureProviderTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  
  public ScaledPictureProvider provider;
  
  @Before
  public void setUp() {
    provider = new ScaledPictureProvider(folder.getRoot());
  }

  @Test
  public void testGetScaledImageWhenMissing() {
    final SampleResolvedFile input = new SampleResolvedFile("IMG_4395.JPG");
    final Optional<File> result = provider.getScaledImage(input, Dimensions.MEDIUM);
    Assert.assertFalse(result.isPresent());
  }

  @Test
  public void testScaleSync() throws IOException, ImageReadException, ImageWriteException {
    final SampleResolvedFile input = new SampleResolvedFile("IMG_4395.JPG");
    final File result = provider.getScaledImageSync(input, Dimensions.MEDIUM);
    
    Assert.assertTrue(result.exists());
    final DiskFileInformation resultImage = new DiskFileInformation(result);

    Assert.assertTrue(resultImage.isImage());
    Assert.assertEquals(640, resultImage.getWidth());
    Assert.assertEquals(427, resultImage.getHeight());
    
    ExifMetadata exif = new ExifMetadata();
    exif.readMetadata(result);
    final Optional<TiffField> value = exif.getValue(ExifTagConstants.EXIF_TAG_RAW_FILE);
    
    Assert.assertTrue(value.isPresent());
    Assert.assertEquals("8e5fe460e5fc7eb2af35ab5880855a80f91a7b3730492e9a7b4cec37aa1113ec", value.get().getStringValue());
  }
}
