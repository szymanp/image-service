package com.metapx.local_repo_server.metadata_repo;

import com.metapx.git_metadata.groups.Group;
import com.metapx.local_repo_server.Endpoint;
import com.metapx.local_repo_server.util.HttpStatusError;
import com.metapx.local_repo_server.metadata_repo.resources.GroupListResource;
import com.metapx.local_repo_server.metadata_repo.resources.GroupResource;

import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import rx.Single;

public class GroupEndpoint extends Endpoint {
  final MetadataRepositoryContext repoContext;
  
  public GroupEndpoint(Vertx vertx, MetadataRepositoryContext repoContext) {
    super(vertx);
    this.repoContext = repoContext;
  }
  
  @Override
  public void register(Router router) {
    router.route(HttpMethod.GET, "/group").handler(this::list);
    router.route(HttpMethod.GET, "/group/:id").handler(this::read);
    router.route(HttpMethod.GET, "/group/:id/subgroups").handler(this::listSubgroups);
  }
  
  private void list(RoutingContext routingContext) {
    final MultiMap params = routingContext.request().params();
    
    if (params.contains("path")) {
      // todo
    } else {
      repoContext.getMetadataRepository()
      .map(repo -> repo.groups().stream().filter(group -> !group.hasParent()))
      .subscribe(groups -> {
        final GroupListResource resource = new GroupListResource(routingContext, groups);
        resource.addSelfLink();
        resource.send(routingContext);
      });
    }
  }

  private void listSubgroups(RoutingContext routingContext) {
    group(routingContext)
    .subscribe(group -> {
      final GroupListResource resource = new GroupListResource(routingContext, group.subgroups().stream());
      resource.addSelfLink();
      resource.send(routingContext);
    });
  }
  
  private void read(RoutingContext routingContext) {
    group(routingContext)
    .subscribe(group -> {
        final GroupResource resource = new GroupResource(routingContext, group);
        resource.addSelfLink();
        resource.send(routingContext);
      });
  }
  
  private Single<Group> group(RoutingContext routingContext) {
    return repoContext.getMetadataRepository()
      .map(repo -> repo.groups().findWithKey(routingContext.request().getParam("id")))
      .flatMap(repoOpt -> {
        if (repoOpt.isPresent()) {
          return Single.just(repoOpt.get());
        } else {
          return Single.error(new HttpStatusError(404));
        }
      });
  }

  @Override
  public void destroy() {
    // do nothing
  }

}
