package com.metapx.git_metadata.references;

import java.util.Stack;
import java.util.concurrent.Callable;

public class Zone {
  private static Zone root;
  private static Stack<Zone> currentContext;
  
  static {
    root = new Zone();
    currentContext = new Stack<Zone>();
    currentContext.push(root);
  }
  
  public static Zone getCurrent() {
    return currentContext.peek();
  }
  
  final String name;
  final Zone parent;
  
  private Zone() {
    this.name = "root-zone";
    this.parent = null;
  }
  
  private Zone(Zone parent, String name) {
    this.parent = parent;
    this.name = name;
  }
  
  public Zone fork(String name) {
    return new Zone(this, name);
  }
  
  public boolean inZone(String name) {
    Zone z = this;
    while (z != null) {
      if (z.name.equals(name)) {
        return true;
      }
      z = z.parent;
    }
    return false;
  }
  
  public <T> T run(Callable<T> task) throws Exception {
    Zone.currentContext.push(this);
    try {
      return task.call();
    } finally {
      Zone.currentContext.pop();
    }
  }

  public void run(Runnable task) {
    Zone.currentContext.push(this);
    try {
      task.run();
    } finally {
      Zone.currentContext.pop();
    }
  }

}
