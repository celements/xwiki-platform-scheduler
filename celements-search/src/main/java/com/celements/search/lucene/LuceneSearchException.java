package com.celements.search.lucene;

public class LuceneSearchException extends Exception {

  private static final long serialVersionUID = 2544834269893594688L;

  public LuceneSearchException() {
    super();
  }

  public LuceneSearchException(String msg) {
    super(msg);
  }

  public LuceneSearchException(Throwable cause) {
    super(cause);
  }

  public LuceneSearchException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
