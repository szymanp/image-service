package com.metapx.git_metadata.core;

import java.util.ArrayList;

/**
 * An object that is both an element of a transaction and a controller for the elements it contains.
 */
public class TransactionSubject implements TransactionElement, TransactionControl {
  private ArrayList<TransactionElement> elements = new ArrayList<TransactionElement>();

  public void addElementToTransaction(TransactionElement element) {
    if (!elements.contains(element)) {
      elements.add(element);
    }
  }

  public void commit() throws Exception {
    for(TransactionElement tx : elements) {
      tx.commit();
    }
  }

  public void rollback() {
    elements.forEach(element -> element.rollback());
  }
}
