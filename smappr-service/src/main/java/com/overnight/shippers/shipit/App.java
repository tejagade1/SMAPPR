package com.overnight.shippers.shipit;

import java.io.IOException;
import java.util.List;
import javax.management.RuntimeErrorException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import com.overnight.shippers.shipit.repo.RepoParser;
import com.overnight.shippers.shipit.splunk.Splunk;


/**
 *
 * @author Tejaswi Gade
 *
 */
public class App {

  private final String gitRepoURL;

  private static String splunkApp = "";

  private static String splunkAPI = "";

  private static String userName = "";

  private static String password = "";

  private static GitHub github;

  private static GHRepository ghRepository;

  private static RepoParser repoParser;

  private static final String githubEnterpriseURL = "https://github.com/api/v3";

  public App(String gitRepoURL, String splunkApp, String splunkAPI, String userName,
      String password) {
    this.gitRepoURL = gitRepoURL;
    App.splunkApp = splunkApp;
    App.splunkAPI = splunkAPI;
    App.userName = userName;
    App.password = password;
  }

  @SuppressWarnings("deprecation")
  public void connectToGitHub() throws IOException {
    System.out.println("Connecting to GitHub");
    if (githubEnterpriseURL == null && githubEnterpriseURL.isEmpty()) {
      throw new RuntimeErrorException(null, "GitHub repo is required.");
    }
    if (userName == null && userName.isEmpty()) {
      throw new RuntimeErrorException(null, "Username is required.");
    }
    if (password == null && password.isEmpty()) {
      throw new RuntimeErrorException(null, "Password is required.");
    }

    github = GitHub.connectToEnterprise(githubEnterpriseURL, userName, password);

    if (github != null) {
      System.out.println("Successfully connected to GitHub");
    }
  }

  private void getGitHubRepository() throws IOException {
    System.out.println("Getting GitHub Repository");
    try {
      String gitRepo = gitRepoURL.replace("https://github.com/", "");
      System.out.println("GitHub Repo:" + gitRepo);
      ghRepository = github.getRepository(gitRepo);
      System.out.println("Successfully retrieved GitHub Repository");
    } catch (Exception e) {
      System.out.println("Unable to retrieve GitHub Repository details");
      System.out.println(e.getMessage());
    }
  }

  private List<String> getRepoContents() {
    System.out.println("Getting Repo Contents");
    repoParser = new RepoParser(ghRepository);
    try {
      return repoParser.getFilesInRepo();
    } catch (IOException e) {
      System.out.println("Unable to get the Repo Content" + e.getMessage());
      return null;
    }
  }

  public static void main(String[] args) {
    System.out.println("Repo URL: " + System.getenv("RepoURL"));
    System.out.println("Splunk APP: " + System.getenv("SplunkAPP"));
    System.out.println("Splunk API URL: " + System.getenv("SplunkAPI"));
    System.out.println("Username: " + System.getenv("UserName"));

    App smapprApp = new App(System.getenv("RepoURL"), System.getenv("SplunkAPP"),
        System.getenv("SplunkAPI"), System.getenv("UserName"), System.getenv("Password"));

    List<String> fileList = null;
    try {
      smapprApp.connectToGitHub();
      smapprApp.getGitHubRepository();
      fileList = smapprApp.getRepoContents();

      System.out.println("Successfully retrieved GitHub Contents");
      System.out.println(repoParser.getFilesInRepo());

      Splunk.doSplunkSearch(splunkApp, splunkAPI, userName, password, ghRepository, fileList);

    } catch (IOException e) {
      System.out.println("Unexpected Error Occured. Time to Debug." + e.getMessage());
    }
  }
}
