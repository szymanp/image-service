package com.metapx.git_metadata.pictures;

import java.io.File;

import com.metapx.git_metadata.core.TransactionSubject;
import com.metapx.git_metadata.core.HashPath.Target;
import com.metapx.git_metadata.core.collections.KeyedCollection;

public class Picture {
  public enum Role { 
    ROOT,
    THUMBNAIL;
  }

  private final PictureService service;
  private final String hash;
  private Subject subject;
  private MemberFileCollection files;

  public Picture(PictureService service, String hash) {
    this.service = service;
    this.hash = hash;
  }

  public Picture(PictureService service, Target target) {
    this.service = service;
    this.hash = target.getHash();
    this.subject = new Subject(target);
  }

  public String getHash() { return hash; }

  public KeyedCollection<String, MemberFile> files() {
    if (files == null) {
      final Subject subject = service.coll.attach(this).getSubject();
      final File source = new File(subject.target.getFile(), "files");
      files = new MemberFileCollection(hash, source, subject, service.refService);
    }
    return files;
  }

  // Transaction Control
  void detach() { subject = null; }
  boolean isDetached() { return subject == null; }
  Subject getSubject() { 
    if (subject == null) throw new RuntimeException("This is a detached picture");
    return subject;
  }

  class Subject extends TransactionSubject {
    final Target target;

    Subject(Target target) {
      super();
      this.target = target;
    }

    Picture getPicture() {
      return Picture.this; 
    }
    
    public void commit() throws Exception {
      final File dir = target.getFile();
      if (!dir.exists()) {
        dir.mkdir();
      }
      super.commit();
    }
  }
}
