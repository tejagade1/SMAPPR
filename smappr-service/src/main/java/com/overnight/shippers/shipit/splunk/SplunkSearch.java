package com.overnight.shippers.shipit.splunk;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.github.GHRepository;
import com.overnight.shippers.shipit.common.Constants;
import com.overnight.shippers.shipit.github.GitHubIssue;

/**
 * Provides a runnable method for performing a Splunk search.
 */
public class SplunkSearch implements Runnable {

  private String username;
  private String password;
  private String fileName;
  private String splunkApp;
  private String splunkAPIURL;
  private String searchQuery;

  private boolean isCCL = false;

  private GHRepository ghRepository;

  public SplunkSearch(String splunkApp, String splunkAPIURL, String username, String password,
      String fileName, GHRepository ghRepository) {
    this.splunkApp = splunkApp;
    this.splunkAPIURL = splunkAPIURL;
    this.username = username;
    this.password = password;
    this.fileName = fileName;

    this.ghRepository = ghRepository;
  }

  @Override
  public void run() {
    String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
    String fileExt = FilenameUtils.getExtension(fileName);

    if (fileExt.equals("prg") || fileExt.equals("inc")) {
      isCCL = true;
      searchQuery =
          "search (domain=*) ccl_error_string=\"*" + fileNameWithOutExt.toUpperCase() + "*\"";
    } else if (fileExt.equals("java")) {
      searchQuery = "search (domain=*) (*Exception OR *Error OR ERROR OR SEVERE OR FATAL OR FAIL*) "
          + fileName;
    } else {
      searchQuery = "search (domain=*) " + fileName;
    }
    System.out.println("Running Thread for Search:" + searchQuery);

    if (splunkAPIURL.substring(splunkAPIURL.length() - 1) != Constants.FORWARD_SLASH) {
      splunkAPIURL = splunkAPIURL + Constants.FORWARD_SLASH;
    }

    String postURL =
        splunkAPIURL + username + Constants.FORWARD_SLASH + splunkApp + Constants.FORWARD_SLASH
            + SplunkAPIConstants.SEARCH_JOBS_URL + SplunkAPIConstants.OUTPUT_JSON;

    StringWriter writer = new StringWriter();

    // Create the HTTP Client for the Search Job
    HttpClient httpSearchClient = HttpClients.createDefault();
    HttpPost httpSearchPost = new HttpPost(postURL);
    httpSearchPost.addHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_FORM_URLENCODED);
    httpSearchPost.addHeader(Constants.AUTH_HEADER, Constants.AUTH_BASIC
        + Base64.getEncoder().encodeToString((username + Constants.COLON + password).getBytes()));

    // Search Job Parameters
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair(SplunkAPIConstants.SEARCH_PARAM, searchQuery));
    params.add(new BasicNameValuePair("adhoc_search_level", "fast"));
    params.add(new BasicNameValuePair("earliest_time", ZonedDateTime.now().minusDays(2L)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))));

    try {
      httpSearchPost.setEntity(new UrlEncodedFormEntity(params));
      HttpResponse searchResponse = httpSearchClient.execute(httpSearchPost);
      HttpEntity searchEntity = searchResponse.getEntity();

      // Read the response & get the search SID
      IOUtils.copy(searchEntity.getContent(), writer);
      JSONObject searchIDJSONObj = new JSONObject(writer.toString());
      String sID = searchIDJSONObj.getString("sid");
      System.out.println(fileNameWithOutExt + ": " + sID);

      // Loop till the search job finishes
      boolean isRunning = true;
      while (isRunning) {
        Thread.sleep(1500);

        String searchCheckURL = splunkAPIURL + username + Constants.FORWARD_SLASH + splunkApp
            + Constants.FORWARD_SLASH + SplunkAPIConstants.SEARCH_JOBS_URL + Constants.FORWARD_SLASH
            + sID + SplunkAPIConstants.OUTPUT_JSON;

        HttpClient httpSearchCheckClient = HttpClients.createDefault();
        HttpGet httpSearchCheckGet = new HttpGet(searchCheckURL);

        httpSearchCheckGet.addHeader(Constants.CONTENT_TYPE,
            Constants.CONTENT_TYPE_FORM_URLENCODED);
        httpSearchCheckGet.addHeader(Constants.AUTH_HEADER, Constants.AUTH_BASIC + Base64
            .getEncoder().encodeToString((username + Constants.COLON + password).getBytes()));

        HttpResponse searchCheckResponse = httpSearchCheckClient.execute(httpSearchCheckGet);
        HttpEntity searchCheckEntity = searchCheckResponse.getEntity();

        writer = new StringWriter();
        IOUtils.copy(searchCheckEntity.getContent(), writer);
        // Translate the response into JSON & check "isDone"
        JSONObject searchCheckJSONObj = new JSONObject(writer.toString());
        boolean isDone = searchCheckJSONObj.getJSONArray("entry").getJSONObject(0)
            .getJSONObject("content").getBoolean("isDone");

        if (isDone) {
          isRunning = false;
        }
      }

      String eventResultURL = splunkAPIURL + username + Constants.FORWARD_SLASH + splunkApp
          + Constants.FORWARD_SLASH + SplunkAPIConstants.SEARCH_JOBS_URL + Constants.FORWARD_SLASH
          + sID + "/events?output_mode=json";
      System.out.println(eventResultURL);

      // HTTP Client to get the event results for the search job
      HttpClient httpResultClient = HttpClients.createDefault();
      HttpGet httpResultGet = new HttpGet(eventResultURL);

      httpResultGet.addHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_FORM_URLENCODED);
      httpResultGet.addHeader(Constants.AUTH_HEADER, Constants.AUTH_BASIC
          + Base64.getEncoder().encodeToString((username + Constants.COLON + password).getBytes()));

      HttpResponse resultResponse = httpResultClient.execute(httpResultGet);
      HttpEntity resultEntity = resultResponse.getEntity();

      writer = new StringWriter();
      IOUtils.copy(resultEntity.getContent(), writer);
      JSONObject eventJSONObj = new JSONObject(writer.toString());
      System.out.println(writer.toString());
      JSONArray eventJSONArr = eventJSONObj.getJSONArray("results");

      if (eventJSONArr.length() > 0) {
        int numOccurrences = 0;
        List<String> domainList = new ArrayList<String>();
        for (int resIdx = 0; resIdx < eventJSONArr.length(); resIdx++) {
          JSONObject resJSONArrObj = (JSONObject) eventJSONArr.get(resIdx);
          if (!domainList.contains(resJSONArrObj.getString("domain"))) {
            domainList.add(resJSONArrObj.getString("domain"));
          }
          numOccurrences++;
        }
        JSONObject resJSONObj = (JSONObject) eventJSONArr.get(0);

        String issueHeader = null;
        if (isCCL) {
          issueHeader = resJSONObj.getString("ccl_error_string");
        }

        GitHubIssue ghIssue = new GitHubIssue(ghRepository);
        ghIssue.prepareIssue(fileName, domainList, resJSONObj.getString("_raw"),
            searchQuery.substring(7), resJSONObj.getString("sourcetype"),
            resJSONObj.getString("source"), issueHeader, numOccurrences);
        ghIssue.createIssue();
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
