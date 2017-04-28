package com.metapx.git_metadata.pictures;

import java.io.File;

import com.metapx.git_metadata.core.TransactionSubject;
import com.metapx.git_metadata.core.HashPath.Target;
import com.metapx.git_metadata.core.collections.Collection;
import com.metapx.git_metadata.core.collections.KeyedCollection;
import com.metapx.git_metadata.groups.Group;
import com.metapx.git_metadata.groups.GroupReference;

public class Picture {
  public enum Role { 
    ROOT,
    THUMBNAIL;
  }

  private final PictureReference ownReference;
  private final PictureService service;
  private final String hash;
  private Subject subject;
  private MemberFileCollection files;
  private MemberGroupCollection groups;

  public Picture(PictureService service, String hash) {
    this.service = service;
    this.hash = hash;
    this.ownReference = new PictureReference(hash);
  }

  public Picture(PictureService service, Target target) {
    this.service = service;
    this.hash = target.getHash();
    this.ownReference = new PictureReference(hash);
    this.subject = new Subject(target);
  }

  public String getHash() { return hash; }
  public PictureReference getReference() { return ownReference; }

  public KeyedCollection<String, MemberFile> files() {
    if (files == null) {
      final Picture attached = service.coll.attach(this);
      if (attached.files != null) {
        files = attached.files;
      } else {
        final Subject subject = service.coll.attach(this).getSubject();
        final File source = new File(subject.target.getFile(), "files");
        files = attached.files = new MemberFileCollection(source, subject, service.refService, ownReference);
      }
    }
    return files;
  }

  public Collection<GroupReference> groups() {
    if (groups == null) {
      final Picture attached = service.coll.attach(this);
      if (attached.groups != null) {
        groups = attached.groups;
      } else {
        final Subject subject = attached.getSubject();
        final File source = new File(subject.target.getFile(), "groups");
        groups = attached.groups = new MemberGroupCollection(source, service.refService, ownReference);
        subject.addElementToTransaction(groups);
      }
    }
    return groups;
  }

  // Transaction Control
  void detach() { subject = null; }
  boolean isDetached() { return subject == null; }
  Subject getSubject() { 
    if (subject == null) throw new RuntimeException("This is a detached picture");
    return subject;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((hash == null) ? 0 : hash.hashCode());
    result = prime * result + ((service == null) ? 0 : service.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other != null && other instanceof Picture) {
      Picture pic = (Picture) other;
      return hash.equals(pic.hash) && service == pic.service;
    } else {
      return false;
    }    
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
