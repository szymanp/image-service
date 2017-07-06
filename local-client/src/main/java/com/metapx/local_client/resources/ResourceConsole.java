package com.metapx.local_client.resources;

import java.io.File;
import java.util.function.Function;

import com.metapx.git_metadata.groups.Group;
import com.metapx.local_client.cli.Console;
import com.metapx.local_client.combined_repo.RepositoryStatusFileInformation;
import com.metapx.local_client.combined_repo.TrackedFileGroup;
import com.metapx.local_client.commands.ItemException;
import com.metapx.local_client.commands.PictureGroup.MaterializedPicture;
import com.metapx.local_picture_repo.FileInformation;

import rx.Observable;
import rx.subjects.Subject;

public class ResourceConsole implements Console {
  private final Subject<Resource, Resource> output;

  public ResourceConsole(Subject<Resource, Resource> output) {
    this.output = output;
  }
  
  
  @Override
  public void setListingFormat(ListingFormat format) {
    // todo
  }

  @Override
  public void info(String message) {
    // intentionally empty
  }
  
  @Override
  public void error(String message) {
    error(new ItemException(message));
  }

  @Override
  public void error(Throwable error) {
    output.onNext(new ExceptionResource(error));
  }

  @Override
  public void reportFiles(Observable<File> files, Function<File, FileInformation> processor) {
    
  }

  @Override
  public void reportFiles(Observable<RepositoryStatusFileInformation> files) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void reportFileGroups(Observable<TrackedFileGroup> fileGroups) {
    // TODO
  }

  @Override
  public void reportGroups(Observable<Group> groups) {
    groups
    .map(group -> new GroupResource(group))
    .subscribe(resource -> output.onNext(resource));
  }
  
  @Override
  public void reportMaterializedPictures(Observable<MaterializedPicture> files) {
    // TODO Auto-generated method stub
    
  }
}
