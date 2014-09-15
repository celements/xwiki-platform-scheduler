package com.celements.search.lucene;

public class LuceneSearchException extends Exception {

  private static final long serialVersionUID = 2544834269893594688L;

  LuceneSearchException(String msg, Throwable cause) {
    super(msg, cause);
  }

  LuceneSearchException(Throwable cause) {
    super(cause);
  }

}
