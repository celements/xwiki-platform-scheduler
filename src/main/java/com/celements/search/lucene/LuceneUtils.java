package com.celements.search.lucene;

import org.python.google.common.base.Strings;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.util.ModelUtils;
import com.google.common.base.Function;
import com.xpn.xwiki.web.Utils;

public class LuceneUtils {

  private static final String QUOTE = "\"";

  public static final Function<String, String> FUNC_EXACTIFY = new Function<String, String>() {

    @Override
    public String apply(String str) {
      return exactify(str);
    }
  };

  public static String exactify(String str) {
    str = Strings.nullToEmpty(str);
    if (!str.isEmpty()) {
      if (!str.startsWith(QUOTE)) {
        str = QUOTE + str;
      }
      if (!str.endsWith(QUOTE)) {
        str = str + QUOTE;
      }
    }
    return str;
  }

  public static String asFieldName(DocumentReference classRef, String field) {
    return serialize(classRef) + "." + field;
  }

  public static String exactify(EntityReference ref) {
    return exactify(ref, true);
  }

  public static String exactify(EntityReference ref, boolean local) {
    String ret = serialize(ref, local);
    // XXX workaround issue CELDEV-35
    EntityReference spaceRef = ref.extractReference(EntityType.SPACE);
    if ((spaceRef == null) || !Character.isDigit(spaceRef.getName().charAt(
        spaceRef.getName().length() - 1))) {
      ret = exactify(ret);
    }
    return ret;
  }

  public static String serialize(EntityReference ref) {
    return serialize(ref, true);
  }

  public static String serialize(EntityReference ref, boolean local) {
    if (ref != null) {
      return local ? getModelUtils().serializeRefLocal(ref) : getModelUtils().serializeRef(ref);
    } else {
      return "";
    }
  }

  private static ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
