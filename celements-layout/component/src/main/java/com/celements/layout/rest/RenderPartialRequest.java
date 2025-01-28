package com.celements.layout.rest;

public class RenderPartialRequest {

  public String contextDocSpace;
  public String contextDocName;
  public String layoutSpace;
  public String startNodeName;
  public String language;

  public void setContextDocSpace(String contextDocSpace) {
    this.contextDocSpace = contextDocSpace;
  }

  public void setContextDocName(String contextDocName) {
    this.contextDocName = contextDocName;
  }

  public void setLayoutSpace(String layoutSpace) {
    this.layoutSpace = layoutSpace;
  }

  public void setStartNodeName(String startNodeName) {
    this.startNodeName = startNodeName;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  @Override
  public String toString() {
    return "RenderPartialRequest ["
        + "contextDocSpace=" + contextDocSpace + ", "
        + "contextDocName=" + contextDocName + ", "
        + "layoutSpace=" + layoutSpace + ", "
        + "startNodeName=" + startNodeName + ", "
        + "language=" + language + "]";
  }

}
