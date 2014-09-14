package com.celements.search.lucene.query;

import org.junit.Test;

import com.celements.search.lucene.query.QueryRestrictionGroup.Type;

public class QueryRestrictionGroupTest {
  
  @Test
  public void test() {
    QueryRestrictionGroup orRestrGrp1 = new QueryRestrictionGroup(Type.OR);
    orRestrGrp1.add(new QueryRestriction("field1", "val1"));
    orRestrGrp1.add(new QueryRestriction("field2", "val2"));
    QueryRestrictionGroup orRestrGrp2 = new QueryRestrictionGroup(Type.OR);
    orRestrGrp2.add(new QueryRestriction("field3", "val3"));
    orRestrGrp2.add(new QueryRestriction("field4", "val4"));
    QueryRestrictionGroup andRestrGrp = new QueryRestrictionGroup(Type.AND);    
    andRestrGrp.add(orRestrGrp1);
    andRestrGrp.add(orRestrGrp2);
    andRestrGrp.add(new QueryRestriction("field5", "val5"));
    System.out.println(andRestrGrp.getQueryString());
  }

}
