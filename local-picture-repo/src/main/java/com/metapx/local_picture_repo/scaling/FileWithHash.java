package com.metapx.local_picture_repo.scaling;

import java.io.File;

import com.metapx.local_picture_repo.ResolvedFile;

import io.vertx.core.json.JsonObject;

public interface FileWithHash {
  /**
   * @return the file being the target of this object.
   */
  public File getFile();
    
  /**
   * @return the hash of the referenced file.
   */
  public String getHash();

  /**
   * @return a <code>FileWithHash</code> corresponding to the given <code>ResolvedFile</code>
   */
  public static FileWithHash fromResolvedFile(ResolvedFile rf) {
    return new FileWithHashImpl(rf.getFile(), rf.getHash());
  }

  /**
   * Default implementation of the FileWithHash interface.
   */
  public static class FileWithHashImpl implements FileWithHash {
    private File file;
    private String hash;
    
    FileWithHashImpl(File file, String hash) {
      this.file = file;
      this.hash = hash;
    }

    @Override
    public File getFile() {
      return file;
    }

    @Override
    public String getHash() {
      return hash;
    }
  }

  public static JsonObject toJson(FileWithHash fileWithHash) {
    return new JsonObject()
      .put("file", fileWithHash.getFile().getAbsolutePath())
      .put("hash", fileWithHash.getHash());
  }
  
  public static FileWithHash fromJson(JsonObject json) {
    return new FileWithHashImpl(
      new File(json.getString("file")),
      json.getString("hash")
      );
  }
}
