package com.celements.search.lucene.query;

import com.xpn.xwiki.plugin.lucene.LucenePlugin;

public enum LuceneDocType {

  VOID("void"), DOC(LucenePlugin.DOCTYPE_WIKIPAGE), ATT(LucenePlugin.DOCTYPE_ATTACHMENT);

  public final String name;

  private LuceneDocType(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

}
