package com.metapx.local_repo_server.metadata_repo;

import java.util.Optional;

import com.metapx.git_metadata.groups.Group;
import com.metapx.local_repo_server.Endpoint;
import com.metapx.local_repo_server.errors.HttpStatusError;
import com.metapx.local_repo_server.metadata_repo.resources.GroupResource;

import io.reactivex.Observable;
import io.vertx.core.http.HttpMethod;
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
    router.route(HttpMethod.GET, "/group/:id").handler(this::read);
  }
  
  private void read(RoutingContext routingContext) {
    group(routingContext)
    .subscribe(group -> {
        final GroupResource resource = new GroupResource(group);
        resource.setSelfLink(routingContext.request().absoluteURI());
        resource.send(routingContext);
      });
  }
  
  private Single<Group> group(RoutingContext routingContext) {
    return repoContext.getMetadataRepository()
      .map(repo -> repo.groups().findWithKey(routingContext.request().params().get("id")))
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
