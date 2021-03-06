package com.metapx.server.ImageService;

import java.net.HttpURLConnection;

import com.metapx.server.data_model.resource.infrastructure.CrudError;
import com.metapx.server.data_model.resource.infrastructure.Resource;
import com.metapx.server.data_model.resource.infrastructure.ResourceIdentifier;
import com.metapx.server.data_model.resource.infrastructure.ResourceService;
import com.metapx.server.data_model.resource.infrastructure.ServiceRunner;
import com.metapx.server.data_model.resource.infrastructure.UrlResolver;
import com.metapx.server.util.DataContext;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ResourceEndpoint {
  
  final private DataContext dataContext;
  final private ServiceRunner<?, ?> serviceRunner;
  final private String name;

  public ResourceEndpoint(DataContext dataContext, ResourceService<?, ?> service, String name) {
    this.dataContext = dataContext;
    this.serviceRunner = ServiceRunner.create(service, new LocalUrlResolver());
    this.name = name;
  }
  
  public void register(Router router) {
    if (serviceRunner.isReadable()) {
      router.route(HttpMethod.GET, "/" + name + "/:id").blockingHandler(this::readResource);
    }
    if (serviceRunner.isWritable()) {
      router.route(HttpMethod.POST, "/" + name).handler(BodyHandler.create());
      router.route(HttpMethod.POST, "/" + name).blockingHandler(this::createResource);
    }
  }
  
  protected void readResource(RoutingContext routingContext) {
    this.dataContext.getDslContext()
    .map(dslContext -> serviceRunner.read(routingContext.request().getParam("id"), dslContext))
    .subscribe(
        (resource) -> this.sendResource(resource, routingContext),
        (error) -> this.sendError(error, routingContext)
        );
  }
  
  protected void createResource(RoutingContext routingContext) {
    if (!routingContext.request().getHeader("content-type").equalsIgnoreCase("application/json")) {
      this.sendError(new HttpException(HttpURLConnection.HTTP_UNSUPPORTED_TYPE), routingContext);
      return;
    }
    
    this.dataContext.getDslContext()
    .map(dslContext -> serviceRunner.create(routingContext.getBodyAsString(), dslContext))
    .doOnSuccess(resource -> routingContext.response().putHeader("Location", resource.getCanonicalUrl()))
    .subscribe(
        (resource) -> this.sendResource(resource, routingContext),
        (error) -> this.sendError(error, routingContext)
        );
  }
  
  private void sendResource(Resource<?> resource, RoutingContext ctx) {
    ResourceResponse response = new ResourceResponse();
    response.meta = "hello world";
    response.data = resource.getRepresentation();
    
    ctx.response().putHeader("content-type", "application/json; charset=utf-8");
    ctx.response().end(Json.encodePrettily(response));
  }
  
  private void sendError(Throwable e, RoutingContext ctx) {
    if (e instanceof CrudError) {
      switch (((CrudError) e).getErrorType()) {
      case NOT_FOUND: 
        ctx.response().setStatusCode(HttpURLConnection.HTTP_NOT_FOUND);
        break;
      case VALIDATION_FAILURE:
        ctx.response().setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
        break;

      default:
        break;
      }
      ctx.response().end();
    } else if (e instanceof HttpException) {
      ((HttpException) e).sendResponse(ctx.response());
      
    } else {
      ctx.response().setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      ctx.response().end();
    }
  }
  
  private class LocalUrlResolver implements UrlResolver {
    @Override
    public String getUrl(ResourceIdentifier resourceId) {
      return "/" + name + "/" + resourceId.getKey().toUrlString();
    }
  }
  
  @SuppressWarnings("unused")
  private static class ResourceResponse {
    public String meta;
    public Object data;
  }
}
