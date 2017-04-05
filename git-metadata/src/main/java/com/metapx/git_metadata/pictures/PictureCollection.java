package com.metapx.git_metadata.pictures;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.metapx.git_metadata.core.HashPath;
import com.metapx.git_metadata.core.HashPathTransactionElement;
import com.metapx.git_metadata.core.HashPath.Target;
import com.metapx.git_metadata.core.collections.KeyedCollection;

class PictureCollection implements KeyedCollection<String, Picture> {
  final HashPathTransactionElement<Picture.Subject> pictures;

  PictureCollection(File root, PictureFactory factory) {
    this.pictures = new HashPathTransactionElement<Picture.Subject>(new HashPath(root), target -> factory.create(target).getSubject());
  }

  public void append(Picture element) {
    attach(element);
  }

  public void update(Picture element) {
    // Nothing to do as the picture does not contain any data.
    // We just make sure that the picture is attached.
    attach(element);
  }

  public void remove(Picture element) {
    throw new RuntimeException("Not implemented");
  }

  public boolean contains(Picture element) {
    return pictures.getIfExists(element.getHash()).isPresent();
  }

  public Optional<Picture> findWithKey(String key) {
    return pictures.getIfExists(key).map(subject -> subject.getPicture());
  }

  public List<Picture> list() {
    return stream().collect(Collectors.toList());
  }

  public Stream<Picture> stream() {
    return pictures.all().map(detached -> detached.get().getPicture());
  }

  Picture attach(Picture element) {
    if (element.isDetached()) {
      return pictures.get(element.getHash()).getPicture();
    } else {
      return element;
    }
  }

  interface PictureFactory {
    Picture create(Target target);
  }
}
