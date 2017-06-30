package com.celements.search.web.packages;

import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.INavigationClassConfig;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexFields;

@Component(MenuWebSearchPackage.NAME)
public class MenuWebSearchPackage implements WebSearchPackage {

  public static final String NAME = "menu";

  public static final String CFGSRC_PROP_BOOST = "celements.search.web." + NAME + ".boost";

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private ConfigurationSource cfgSrc;

  @Requirement
  private INavigationClassConfig classConf;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public boolean isRequired(XWikiDocument cfgDoc) {
    return false;
  }

  @Override
  public Set<LuceneDocType> getDocTypes() {
    return ImmutableSet.of(LuceneDocType.DOC);
  }

  @Override
  public IQueryRestriction getQueryRestriction(XWikiDocument cfgDoc, String searchTerm) {
    float boost = cfgSrc.getProperty(CFGSRC_PROP_BOOST, 30f);
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    grp.add(searchService.createFieldRestriction(classConf.getMenuNameClassRef(),
        INavigationClassConfig.MENU_NAME_FIELD, searchTerm).setBoost(boost));
    grp.add(searchService.createRestriction(IndexFields.DOCUMENT_TITLE, searchTerm).setBoost(
        boost));
    return grp;
  }

  @Override
  public Optional<DocumentReference> getLinkedClassRef() {
    return Optional.of(classConf.getMenuItemClassRef());
  }

}
