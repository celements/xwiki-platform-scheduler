package com.celements.search.lucene.query;

import static com.celements.search.lucene.LuceneUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.ObjectUtils;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.context.ModelContext;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.plugin.lucene.IndexFields;
import com.xpn.xwiki.web.Utils;

public class LuceneQuery extends QueryRestrictionGroup {

  private static final long serialVersionUID = 20140913181251L;

  private List<LuceneDocType> docTypes = ImmutableList.of();
  private List<WikiReference> wikis = ImmutableList.of();

  public LuceneQuery() {
    super(Type.AND);
  }

  /**
   * @deprecated instead use {{@link #LuceneQuery()} with {@link #setDocTypes(List)} and
   *             {@link #setWikis(List)}
   */
  @Deprecated
  public LuceneQuery(List<String> docTypes) {
    super(Type.AND);
    setDocTypes(docTypes);
    setWiki(Utils.getComponent(ModelContext.class).getWikiRef());
  }

  public List<LuceneDocType> getDocTypes() {
    return docTypes;
  }

  public void setDocTypes(@NotNull Collection<LuceneDocType> docTypes) {
    this.docTypes = ImmutableList.copyOf(docTypes);
  }

  /**
   * @deprecated instead use {@link #setDocTypes(Collection)}
   */
  @Deprecated
  public void setDocTypes(@NotNull List<String> docTypeKeys) {
    List<LuceneDocType> docTypes = new ArrayList<>();
    for (String type : docTypeKeys) {
      docTypes.add(LuceneDocType.parseEnum(type));
    }
    setDocTypes(docTypes);
  }

  /**
   * @deprecated instead use {@link #getWiki()}
   */
  @Deprecated
  public String getDatabase() {
    String database = null;
    WikiReference wikiRef = getWiki();
    if (wikiRef != null) {
      database = wikiRef.getName();
    }
    return database;
  }

  public WikiReference getWiki() {
    WikiReference ret = null;
    if (!getWikis().isEmpty()) {
      ret = getWikis().get(0);
    }
    return ret;
  }

  public List<WikiReference> getWikis() {
    return wikis;
  }

  /**
   * @deprecated instead use {@link #setWiki()}
   */
  @Deprecated
  public void setDatabase(String database) {
    if (StringUtils.isNotBlank(database)) {
      setWiki(new WikiReference(database));
    }
  }

  public void setWiki(WikiReference wikiRef) {
    setWikis(Arrays.asList(wikiRef));
  }

  public void setWikis(List<WikiReference> wikiRefs) {
    if ((wikiRefs == null) || wikiRefs.isEmpty()) {
      wikis = Collections.emptyList();
    } else {
      wikis = Collections.unmodifiableList(new ArrayList<>(wikiRefs));
    }
  }

  @Override
  public String getQueryString() {
    QueryRestrictionGroup restrGrp = super.copy();
    restrGrp.add(0, getAsRestrGrp(wikis));
    restrGrp.add(0, getAsRestrGrp(docTypes));
    return restrGrp.getQueryString();
  }

  @Override
  public LuceneQuery copy() {
    LuceneQuery copy = new LuceneQuery();
    copy.docTypes = docTypes;
    copy.wikis = wikis;
    copy.addAll(super.copy());
    return copy;
  }

  /**
   * @deprecated use {@link #add} directly
   * @param restriction
   * @return
   */
  @Deprecated
  public LuceneQuery addRestriction(IQueryRestriction restriction) {
    if (restriction != null) {
      this.add(restriction);
    }
    return this;
  }

  /**
   * @deprecated use {@link addAll} directly
   * @param restriction
   * @return
   */
  @Deprecated
  public LuceneQuery addRestrictionList(List<? extends IQueryRestriction> restrictionList) {
    if (restrictionList != null) {
      addAll(restrictionList);
    }
    return this;
  }

  /**
   * @deprecated use {@link QueryRestrictionGroup}
   * @param restriction
   * @return
   */
  @Deprecated
  public LuceneQuery addOrRestrictionList(List<? extends IQueryRestriction> restrictionList) {
    if (restrictionList != null) {
      QueryRestrictionGroup orRestrGrp = new QueryRestrictionGroup(Type.OR);
      orRestrGrp.addAll(restrictionList);
      this.add(orRestrGrp);
    }
    return this;
  }

  private QueryRestrictionGroup getAsRestrGrp(List<?> list) {
    QueryRestrictionGroup ret = new QueryRestrictionGroup(Type.OR);
    for (Object elem : list) {
      IQueryRestriction restr;
      if (elem instanceof WikiReference) {
        restr = getWikiRestr((WikiReference) elem);
      } else {
        restr = getDocTypeRestr((LuceneDocType) elem);
      }
      ret.add(restr);
    }
    return ret;
  }

  private IQueryRestriction getDocTypeRestr(LuceneDocType type) {
    return new QueryRestriction(IndexFields.DOCUMENT_TYPE, exactify(type.key));
  }

  private IQueryRestriction getWikiRestr(WikiReference wikiRef) {
    return new QueryRestriction(IndexFields.DOCUMENT_WIKI, exactify(wikiRef.getName()));
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(super.hashCode()).append(docTypes).append(wikis).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LuceneQuery) {
      LuceneQuery other = (LuceneQuery) obj;
      return super.equals(obj) && ObjectUtils.equals(docTypes, other.docTypes)
          && ObjectUtils.equals(wikis, other.wikis);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "LuceneQuery [docTypes=" + docTypes + ", wikis=" + wikis + ", queryString()="
        + getQueryString() + "]";
  }

}
