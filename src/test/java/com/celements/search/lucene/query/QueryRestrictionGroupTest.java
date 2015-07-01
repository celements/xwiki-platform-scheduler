package com.celements.search.lucene.query;

import static org.junit.Assert.*;

import org.junit.Test;

import com.celements.search.lucene.query.QueryRestrictionGroup.Type;

public class QueryRestrictionGroupTest {
  
  @Test
  public void testGetType() {
    assertSame(Type.AND, (new QueryRestrictionGroup(Type.AND)).getType());
    assertSame(Type.OR, (new QueryRestrictionGroup(Type.OR)).getType());
  }

  @Test
  public void testGetQueryString_empty() {
    QueryRestrictionGroup restrGrp = new QueryRestrictionGroup(Type.AND);
    assertEquals("", restrGrp.getQueryString());
  }

  @Test
  public void testGetQueryString_empty_multiple() {
    QueryRestrictionGroup restrGrp = new QueryRestrictionGroup(Type.AND);
    restrGrp.add(new QueryRestriction("", ""));
    restrGrp.add(new QueryRestriction("", ""));
    restrGrp.add(new QueryRestrictionGroup(Type.AND));
    restrGrp.add(new QueryRestrictionGroup(Type.AND));
    assertEquals("", restrGrp.getQueryString());
  }

  @Test
  public void testGetQueryString_single() {
    QueryRestrictionGroup restrGrp = new QueryRestrictionGroup(Type.AND);
    restrGrp.add(new QueryRestriction("field", "value"));
    assertEquals("field:(+value*)", restrGrp.getQueryString());
  }

  @Test
  public void testGetQueryString_duplicate_same() {
    QueryRestrictionGroup restrGrp = new QueryRestrictionGroup(Type.AND);
    QueryRestriction restr = new QueryRestriction("field", "value");
    restrGrp.add(restr);
    restrGrp.add(restr);
    assertEquals("field:(+value*)", restrGrp.getQueryString());
  }

  @Test
  public void testGetQueryString_duplicate_equal() {
    QueryRestrictionGroup restrGrp = new QueryRestrictionGroup(Type.AND);
    restrGrp.add(new QueryRestriction("field", "value"));
    restrGrp.add(new QueryRestriction("field", "value"));
    assertEquals("field:(+value*)", restrGrp.getQueryString());
  }

  @Test
  public void testGetQueryString_multiple_and() {
    QueryRestrictionGroup restrGrp = new QueryRestrictionGroup(Type.AND);
    restrGrp.add(new QueryRestriction("field1", "value1"));
    restrGrp.add(new QueryRestriction("field2", "value2.1 value2.2"));
    restrGrp.add(new QueryRestriction("field3", "+value3.1 -value3.2"));
    assertEquals("(field1:(+value1*) AND field2:(+value2.1* +value2.2*) "
        + "AND field3:(+value3.1* -value3.2*))", restrGrp.getQueryString());
  }

  @Test
  public void testGetQueryString_multiple_or() {
    QueryRestrictionGroup restrGrp = new QueryRestrictionGroup(Type.OR);
    restrGrp.add(new QueryRestriction("field1", "value1"));
    restrGrp.add(new QueryRestriction("field2", "value2.1 value2.2"));
    restrGrp.add(new QueryRestriction("field3", "+value3.1 -value3.2"));
    assertEquals("(field1:(+value1*) OR field2:(+value2.1* +value2.2*) "
        + "OR field3:(+value3.1* -value3.2*))", restrGrp.getQueryString());
  }
  
  @Test
  public void testGetQueryString_groups_and() {
    QueryRestrictionGroup restrGrp = getNewFilledRestrGrp(Type.AND);
    assertEquals("((field1:(+value1*) OR field2:(+value2*)) AND (field3:(+value3*) "
        + "OR field4:(+value4*)) AND field5:(+value5*))", restrGrp.getQueryString());
  }

  @Test
  public void testGetQueryString_groups_or() {
    QueryRestrictionGroup restrGrp = getNewFilledRestrGrp(Type.OR);
    assertEquals("((field1:(+value1*) AND field2:(+value2*)) OR (field3:(+value3*) "
        + "AND field4:(+value4*)) OR field5:(+value5*))", restrGrp.getQueryString());
  }
  
  @Test
  public void testGetQueryString_not() {
    QueryRestrictionGroup restrGrp = getNewFilledRestrGrp(Type.AND);
    restrGrp.setNegate(true);
    restrGrp.get(1).setNegate(true);
    assertEquals("NOT ((field1:(+value1*) OR field2:(+value2*)) AND NOT (field3:(+value3*) "
        + "OR field4:(+value4*)) AND field5:(+value5*))", restrGrp.getQueryString());
  }
  
  @Test
  public void testGetQueryString_not_single() {
    QueryRestrictionGroup restrGrp = new QueryRestrictionGroup(Type.AND);
    restrGrp.add(new QueryRestriction("field", "value"));
    restrGrp.setNegate(true);
    assertEquals("NOT field:(+value*)", restrGrp.getQueryString());
  }

  @Test
  public void testCopy() {
    QueryRestrictionGroup restrGrp = getNewFilledRestrGrp(Type.AND);
    restrGrp.setNegate(true);
    QueryRestrictionGroup restrGrpCopy = restrGrp.copy();
    assertNotSame(restrGrp, restrGrpCopy);
    assertEquals(restrGrp, restrGrpCopy);
  }

  @Test
  public void testHashCode() {
    QueryRestrictionGroup restrGrp1 = getNewFilledRestrGrp(Type.AND);
    QueryRestrictionGroup restrGrp2 = getNewFilledRestrGrp(Type.AND);
    assertTrue(restrGrp1.hashCode() == restrGrp2.hashCode());
    restrGrp1.remove(restrGrp1.size() - 1);
    restrGrp1.add(new QueryRestriction("field5", "valueOther"));
    assertFalse(restrGrp1.hashCode() == restrGrp2.hashCode());
  }

  @Test
  public void testHashCode_Negate() {
    QueryRestrictionGroup restrGrp1 = getNewFilledRestrGrp(Type.AND);
    QueryRestrictionGroup restrGrp2 = getNewFilledRestrGrp(Type.AND);
    assertTrue(restrGrp1.hashCode() == restrGrp2.hashCode());
    restrGrp1.setNegate(true);
    assertFalse(restrGrp1.hashCode() == restrGrp2.hashCode());
  }

  @Test
  public void testHashCode_Type() {
    QueryRestrictionGroup restrGrp1 = new QueryRestrictionGroup(Type.AND);
    QueryRestrictionGroup restrGrp2 = new QueryRestrictionGroup(Type.OR);
    assertFalse(restrGrp1.hashCode() == restrGrp2.hashCode());
  }

  @Test
  public void testEquals() {
    QueryRestrictionGroup restrGrp1 = getNewFilledRestrGrp(Type.AND);
    QueryRestrictionGroup restrGrp2 = getNewFilledRestrGrp(Type.AND);
    assertTrue(restrGrp1.equals(restrGrp2));
    restrGrp1.remove(restrGrp1.size() - 1);
    restrGrp1.add(new QueryRestriction("field5", "valueOther"));
    assertFalse(restrGrp1.equals(restrGrp2));
  }

  @Test
  public void testEquals_negate() {
    QueryRestrictionGroup restrGrp1 = getNewFilledRestrGrp(Type.AND);
    QueryRestrictionGroup restrGrp2 = getNewFilledRestrGrp(Type.AND);
    assertTrue(restrGrp1.equals(restrGrp2));
    restrGrp1.setNegate(true);
    assertFalse(restrGrp1.equals(restrGrp2));
  }

  @Test
  public void testEquals_Type() {
    QueryRestrictionGroup restrGrp1 = new QueryRestrictionGroup(Type.AND);
    QueryRestrictionGroup restrGrp2 = new QueryRestrictionGroup(Type.OR);
    assertFalse(restrGrp1.equals(restrGrp2));
  }
  
  private QueryRestrictionGroup getNewFilledRestrGrp(Type type) {
    Type otherType = type == Type.AND ? Type.OR : Type.AND;
    QueryRestrictionGroup restrGrp = new QueryRestrictionGroup(type);
    QueryRestrictionGroup restrGrp1 = new QueryRestrictionGroup(otherType);
    restrGrp1.add(new QueryRestriction("field1", "value1"));
    restrGrp1.add(new QueryRestriction("field2", "value2"));
    restrGrp.add(restrGrp1);
    QueryRestrictionGroup restrGrp2 = new QueryRestrictionGroup(otherType);
    restrGrp2.add(new QueryRestriction("field3", "value3"));
    restrGrp2.add(new QueryRestriction("field4", "value4"));
    restrGrp.add(restrGrp2);
    restrGrp.add(new QueryRestriction("field5", "value5"));
    return restrGrp;
  }

}
