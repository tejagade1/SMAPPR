package com.overnight.shippers.shipit.github;

import java.io.IOException;
import java.util.List;
import javax.management.RuntimeErrorException;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;

/**
 *
 * @author Tejaswi Gade
 *
 */
public class GitHubIssue {

  private final GHRepository ghRepository;

  private GHIssueBuilder issueBuilder;

  private GitHubIssueSearch githubIssueSearch;

  private String issueTitle;

  private String smapprIssueLabel = "SMAPPR";

  private String splunkIssueLabel = "SPLUNK";

  private String fileNameIssueLabel;

  private String issueBody;

  private String fileName;

  private List<String> domainList;

  private String stackTrace;

  private String splunkSearchStr;

  private String sourceType;

  private String sourcePath;

  private String issueHeader;

  private int numOccurrences;

  public GitHubIssue(GHRepository ghRepository) {
    this.ghRepository = ghRepository;
    githubIssueSearch = new GitHubIssueSearch(ghRepository);
  }

  public void prepareIssue(String fileName, List<String> domainList, String stackTrace,
      String splunkSearchStr, String sourceType, String sourcePath, String issueHeader,
      int numOccurrences) {
    this.fileName = fileName;
    this.domainList = domainList;
    this.stackTrace = stackTrace;
    this.splunkSearchStr = splunkSearchStr;
    this.sourceType = sourceType;
    this.sourcePath = sourcePath;
    this.issueHeader = issueHeader;
    this.numOccurrences = numOccurrences;
  }

  public void createIssue() throws IOException {
    System.out.println("Creating an Issue in repository");
    if (fileName == null || fileName.isEmpty()) {
      throw new RuntimeErrorException(null, "File Name is required.");
    }

    try {
    	String searchString = null;
    	if(fileName.contains(".")) {
    		searchString = fileName.substring(0, fileName.lastIndexOf('.'));
    	}
    	else {
    		searchString = fileName;
    	}
      githubIssueSearch.performSearch(searchString);

      if (!githubIssueSearch.isIssueAlreadyExists()) {
        System.out.println("No similar issue exists on the repo. Creating a new one.");
        buildIssueTitle();
        System.out.println("Issue Title:" + issueTitle);
        issueBuilder = ghRepository.createIssue(issueTitle);
        buildLabels();
        buildIssueBody();
        System.out.println("Issue Body:" + issueBody);
        issueBuilder.create();
      } else {
        System.out.println("Similar issue exists on the repo. Not creating a new one.");
      }
    } catch (IOException e) {
      System.out.println("Unable to create an issue.");
      throw new IOException("Unable to create an issue in the repository");
    } catch (RuntimeErrorException e) {
      System.out.println("Unable to create an issue.Run time error occured.");
      throw new IOException("Unable to create an issue in the repository. Run time error occured.");
    }
  }

  private void buildIssueTitle() {
    if (fileName == null || fileName.isEmpty()) {
      throw new RuntimeErrorException(null, "File Name is required.");
    }

    if (issueHeader != null) {
      issueTitle = issueHeader;
    } else {
      issueTitle = fileName;
    }

    System.out.println("Title: " + issueTitle);
  }

  private void buildLabels() {
    if (issueBuilder == null) {
      throw new RuntimeErrorException(null, "Issue Builder is required.");
    }
    System.out.println("Adding labels");
    fileNameIssueLabel = fileName;
    issueBuilder.label(smapprIssueLabel);
    issueBuilder.label(splunkIssueLabel);
    issueBuilder.label(fileNameIssueLabel);
  }

  private void buildIssueBody() {
    if (issueBuilder == null) {
      throw new RuntimeErrorException(null, "Issue Builder is required.");
    }
    System.out.println("Building issue body");
    issueBody = "#" + " **Splunk Issue**\n"
        + "This GitHub issue has been auto-logged by SMAPPR for the Splunk issue that exists for this project.\n"
        + "### **File Name**\n" + fileName + "\n" + "### **Stack Trace**\n" + stackTrace + "\n"
        + "### **Domains**\n" + domainList + "\n" + "### **Source Type**\n" + sourceType + "\n"
        + "### **Source Log Path**\n" + sourcePath + "\n" + "### **Splunk Search String**\n"
        + splunkSearchStr + "\n" + "### **Number of Occurrences**\n" + numOccurrences;
    issueBuilder.body(issueBody);
  }
}
