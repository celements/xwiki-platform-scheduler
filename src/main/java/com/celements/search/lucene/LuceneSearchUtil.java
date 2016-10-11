package com.celements.search.lucene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.google.common.base.Function;
import com.xpn.xwiki.web.Utils;

public class LuceneSearchUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneSearchUtil.class);

  public static String exactify(String str) {
    if (StringUtils.isNotBlank(str)) {
      return "\"" + str + "\"";
    } else {
      return "";
    }
  }

  public static List<String> exactify(List<String> strs) {
    List<String> ret = new ArrayList<>();
    if (strs != null) {
      for (String str : strs) {
        ret.add(exactify(str));
      }
    }
    return ret;
  }

  public static IQueryRestriction buildRestrictionGroup(String valuesStr, Type type,
      Function<String, IQueryRestriction> getRestrFunc) {
    return buildRestrictionGroup(Arrays.asList(valuesStr.split("[,;\\| ]+")), type, getRestrFunc);
  }

  public static IQueryRestriction buildRestrictionGroup(List<String> values, Type type,
      Function<String, IQueryRestriction> getRestrFunc) {
    QueryRestrictionGroup grp = Utils.getComponent(
        ILuceneSearchService.class).createRestrictionGroup(type);
    for (String str : values) {
      if (StringUtils.isNotBlank(str)) {
        try {
          grp.add(getRestrFunc.apply(str));
        } catch (IllegalArgumentException iae) {
          LOGGER.warn("building restriction failed for value '{}' ", str);
        }
      }
    }
    return grp;
  }

}
