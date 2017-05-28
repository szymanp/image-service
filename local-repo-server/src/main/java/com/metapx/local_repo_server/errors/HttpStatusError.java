package com.metapx.local_repo_server.errors;

import io.vertx.rxjava.core.http.HttpServerResponse;

public class HttpStatusError extends Exception {
  public static void endWithError(Throwable error, HttpServerResponse response) {
    if (error instanceof HttpStatusError) {
      response.setStatusCode(((HttpStatusError) error).getStatusCode()).end();
    } else {
      error.printStackTrace();
      response.setStatusCode(500).end();
    }
  }
  
  private static final long serialVersionUID = 1L;
  
  final int statusCode;
  
  public HttpStatusError(int code) {
    super(Integer.toString(code));
    statusCode = code;
  }
  
  public int getStatusCode() {
    return statusCode;
  }
}
