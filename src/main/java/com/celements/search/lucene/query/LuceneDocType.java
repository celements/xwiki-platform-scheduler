package com.celements.search.lucene.query;

import com.xpn.xwiki.plugin.lucene.LucenePlugin;

public enum LuceneDocType {

  NONE("none"),
  DOC(LucenePlugin.DOCTYPE_WIKIPAGE),
  ATT(LucenePlugin.DOCTYPE_ATTACHMENT);

  public final String key;

  private LuceneDocType(String key) {
    this.key = key;
  }

  @Override
  public String toString() {
    return key;
  }

  public static LuceneDocType parseEnum(String str) {
    for (LuceneDocType type : values()) {
      if (type.key.equals(str)) {
        return type;
      }
    }
    throw new IllegalArgumentException("LuceneDocType not exists: " + str);
  }

}
