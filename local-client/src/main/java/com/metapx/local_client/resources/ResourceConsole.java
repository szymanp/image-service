package com.metapx.local_client.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.metapx.git_metadata.groups.Group;
import com.metapx.local_client.cli.Console;
import com.metapx.local_client.combined_repo.RepositoryStatusFileInformation;
import com.metapx.local_client.combined_repo.TrackedFileGroup;
import com.metapx.local_picture_repo.FileInformation;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ResourceConsole implements Console {
  private Stream<TrackedFileGroup> fileGroups;
  private Stream<Group> groups;
  
  private List<RuntimeException> errors = new ArrayList<RuntimeException>();

  @Override
  public void setListingFormat(ListingFormat format) {
    // todo
  }

  @Override
  public void info(String message) {
    // intentionally empty
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
    this.fileGroups = fileGroups;
  }

  @Override
  public void reportGroups(Stream<Group> groups) {
    this.groups = groups;
  }
  
  public JsonObject getResult() {
    final JsonObject result = new JsonObject();
    result.put("type", "result");
    result.put("resources", getResources());
    result.put("errors", getErrors());
    
    return result;
  }
  
  private JsonArray getResources() {
    return new JsonArray(resourceStream().collect(Collectors.toList()));
  }
  
  private JsonArray getErrors() {
    final JsonArray result = new JsonArray();
    
    errors.forEach(error -> result.add(new ExceptionResource(error).build()));
    
    return result;
  }
  
  private Stream<JsonObject> resourceStream() {
    return groupStream();
  }
  
  private Stream<JsonObject> groupStream() {
    if (groups == null) {
      return Stream.empty();
    } else {
      return groups.map(group -> new GroupResource(group).build());
    }
  }
}
