package com.overnight.shippers.shipit.splunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Event Result Model
 *
 */
public class SplunkResult {

  private int numOccurrences;

  private String source;
  private String fileName;
  private String sourceType;
  private String stackTrace;
  private String issueHeader;

  private List<String> domainList;

  public SplunkResult() {
    setNumOccurrences(0);
    setSource("");
    setFileName("");
    setSourceType("");
    setStackTrace("");
    setIssueHeader("");
    setDomainList(new ArrayList<String>());
  }

  public int getNumOccurrences() {
    return numOccurrences;
  }

  public void setNumOccurrences(int numOccurrences) {
    this.numOccurrences = numOccurrences;
  }

  public String getSource() {
    return source;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }

  public String getIssueHeader() {
    return issueHeader;
  }

  public void setIssueHeader(String cclErrorString) {
    issueHeader = cclErrorString;
  }

  public List<String> getDomainList() {
    return domainList;
  }

  public void setDomainList(List<String> domainList) {
    this.domainList = domainList;
  }

}
