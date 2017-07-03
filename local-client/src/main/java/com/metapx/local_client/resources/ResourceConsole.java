package com.metapx.local_client.resources;

import java.io.File;
import java.util.function.Function;
import java.util.stream.Stream;

import com.metapx.git_metadata.groups.Group;
import com.metapx.local_client.cli.Console;
import com.metapx.local_client.combined_repo.RepositoryStatusFileInformation;
import com.metapx.local_client.combined_repo.TrackedFileGroup;
import com.metapx.local_client.commands.ItemException;
import com.metapx.local_picture_repo.FileInformation;

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
  public void reportFiles(Stream<File> files, Function<File, FileInformation> processor) {
    
  }

  @Override
  public void reportFiles(Stream<RepositoryStatusFileInformation> files) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void reportFileGroups(Stream<TrackedFileGroup> fileGroups) {
    // TODO
  }

  @Override
  public void reportGroups(Stream<Group> groups) {
    groups
    .map(group -> new GroupResource(group))
    .forEach(resource -> output.onNext(resource));
  }
}
