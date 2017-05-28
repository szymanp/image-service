package com.metapx.local_repo_server.metadata_repo;

import java.io.File;

import com.metapx.local_repo_server.Endpoint;

import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;

public class MetadataRepositoryEndpoint extends Endpoint {
  final MetadataRepositoryContext repoContext;
  
  final GroupEndpoint groupEndpoint;
  
  public MetadataRepositoryEndpoint(Vertx vertx) {
    super(vertx);
    repoContext = new MetadataRepositoryContext(new File("C:/Users/Piotrek/.metapx/default.repo"));
    
    groupEndpoint = new GroupEndpoint(vertx, repoContext);
  }

  @Override
  public void register(Router router) {
    groupEndpoint.register(router);
  }

  @Override
  public void destroy() {
    groupEndpoint.destroy();
  }

}
