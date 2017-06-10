package com.metapx.local_client.daemon;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.Arguments;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;

/**
 * Taken from:
 * https://gist.github.com/Stwissel/a7f8ce79785afd49eb2ced69b56335de#file-asyncinputstream-java
 * @author stw
 */
public class AsyncInputStream implements ReadStream<Buffer> {

  public static final int           DEFAULT_READ_BUFFER_SIZE = 8;
  private static final Logger       log                      = LoggerFactory.getLogger(AsyncInputStream.class);

  // Based on the inputStream with the real data
  private final ReadableByteChannel ch;
  private final Vertx               vertx;
  private final Context             context;

  private boolean                   closed;
  private boolean                   paused;
  private boolean                   readInProgress;

  private Handler<Buffer>           dataHandler;
  private Handler<Void>             endHandler;
  private Handler<Throwable>        exceptionHandler;

  private int                       readBufferSize           = DEFAULT_READ_BUFFER_SIZE;
  private long                      readPos;

  /**
   * Create a new Async InputStream that can we used with a Pump
   * 
   * @param in
   */
  public AsyncInputStream(Vertx vertx, Context context, InputStream in) {
    this.vertx = vertx;
    this.context = context;
    this.ch = Channels.newChannel(in);
  }

  public void close() {
    closeInternal(null);
  }

  public void close(Handler<AsyncResult<Void>> handler) {
    closeInternal(handler);
  }

  /*
   * (non-Javadoc)
   * @see io.vertx.core.streams.ReadStream#endHandler(io.vertx.core.Handler)
   */
  @Override
  public synchronized AsyncInputStream endHandler(Handler<Void> endHandler) {
    check();
    this.endHandler = endHandler;
    return this;
  }

  /*
   * (non-Javadoc)
   * @see
   * io.vertx.core.streams.ReadStream#exceptionHandler(io.vertx.core.Handler)
   */
  @Override
  public synchronized AsyncInputStream exceptionHandler(Handler<Throwable> exceptionHandler) {
    check();
    this.exceptionHandler = exceptionHandler;
    return this;
  }

  /*
   * (non-Javadoc)
   * @see io.vertx.core.streams.ReadStream#handler(io.vertx.core.Handler)
   */
  @Override
  public synchronized AsyncInputStream handler(Handler<Buffer> handler) {
    check();
    this.dataHandler = handler;
    if (this.dataHandler != null && !this.paused && !this.closed) {
      this.doRead();
    }
    return this;
  }

  /*
   * (non-Javadoc)
   * @see io.vertx.core.streams.ReadStream#pause()
   */
  @Override
  public synchronized AsyncInputStream pause() {
    check();
    this.paused = true;
    return this;
  }

  public synchronized AsyncInputStream read(Buffer buffer, int offset, long position, int length,
      Handler<AsyncResult<Buffer>> handler) {
    Objects.requireNonNull(buffer, "buffer");
    Objects.requireNonNull(handler, "handler");
    Arguments.require(offset >= 0, "offset must be >= 0");
    Arguments.require(position >= 0, "position must be >= 0");
    Arguments.require(length >= 0, "length must be >= 0");
    check();
    ByteBuffer bb = ByteBuffer.allocate(length);
    doRead(buffer, offset, bb, position, handler);
    return this;
  }

  /*
   * (non-Javadoc)
   * @see io.vertx.core.streams.ReadStream#resume()
   */
  @Override
  public synchronized AsyncInputStream resume() {
    check();
    if (this.paused && !this.closed) {
      this.paused = false;
      if (this.dataHandler != null) {
        this.doRead();
      }
    }
    return this;
  }

  private void check() {
    if (this.closed) {
      throw new IllegalStateException("Inputstream is closed");
    }
  }

  private void checkContext() {
    if (!vertx.getOrCreateContext().equals(context)) {
      throw new IllegalStateException("AsyncInputStream must only be used in the context that created it, expected: " + this.context
          + " actual " + vertx.getOrCreateContext());
    }
  }

  private synchronized void closeInternal(Handler<AsyncResult<Void>> handler) {
    check();
    closed = true;
    doClose(handler);
  }

  private void doClose(Handler<AsyncResult<Void>> handler) {
    Future<Void> res = Future.future();
    try {
      ch.close();
      res.complete(null);
    } catch (IOException e) {
      res.fail(e);
    }
    if (handler != null) {
      this.vertx.runOnContext(v -> handler.handle(res));
    }
  }

  private synchronized void doRead() {
    if (!readInProgress) {
      readInProgress = true;
      Buffer buff = Buffer.buffer(readBufferSize);
      read(buff, 0, readPos, readBufferSize, ar -> {
        if (ar.succeeded()) {
          readInProgress = false;
          Buffer buffer = ar.result();
          if (buffer.length() == 0) {
            // Empty buffer represents end of file
            handleEnd();
          } else {
            readPos += buffer.length();
            handleData(buffer);
            if (!paused && dataHandler != null) {
              doRead();
            }
          }
        } else {
          handleException(ar.cause());
        }
      });
    }
  }

  private void doRead(Buffer writeBuff, int offset, ByteBuffer buff, long position, Handler<AsyncResult<Buffer>> handler) {

    // ReadableByteChannel doesn't have a completion handler, so we wrap it into
    // an executeBlocking and use the future there
    vertx.executeBlocking(future -> {
      try {
        Integer bytesRead = ch.read(buff);
        future.complete(bytesRead);
      } catch (Exception e) {
       log.error(e);
       future.fail(e);
      }
      
    } , res -> {
      if (res.failed()) {
        context.runOnContext((v) -> handler.handle(Future.failedFuture(res.cause())));
      } else {
        // Do the completed check
        Integer bytesRead = (Integer) res.result();
        if (buff.hasRemaining()) {
          long pos = position;
          pos += bytesRead;
          // resubmit
          doRead(writeBuff, offset, buff, pos, handler);
        } else {
          // We are done

          context.runOnContext((v) -> {
            buff.flip();
            writeBuff.setBytes(offset, buff);
            handler.handle(Future.succeededFuture(writeBuff));
          });
        }
      }
    });
  }

  private synchronized void handleData(Buffer buffer) {
    if (dataHandler != null) {
      checkContext();
      dataHandler.handle(buffer);
    }
  }

  private synchronized void handleEnd() {
    if (endHandler != null) {
      checkContext();
      endHandler.handle(null);
    }
  }

  private void handleException(Throwable t) {
    if (exceptionHandler != null && t instanceof Exception) {
      exceptionHandler.handle(t);
    } else {
      log.error("Unhandled exception", t);

    }
  }

}