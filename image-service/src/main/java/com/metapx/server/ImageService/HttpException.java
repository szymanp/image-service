package com.metapx.server.ImageService;

import io.vertx.core.http.HttpServerResponse;

public class HttpException extends RuntimeException {

  private static final long serialVersionUID = -951987086704841567L;
  final private int statusCode;
  final private String statusMessage;
  
  public HttpException(int statusCode) {
    this.statusCode = statusCode;
    this.statusMessage = null;
  }
  
  public void sendResponse(HttpServerResponse response) {
    response.setStatusCode(statusCode);
    response.end();
  }

}
