package com.overnight.shippers.shipit.splunk;

import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.kohsuke.github.GHRepository;

/**
 * Manages the Splunk instance.
 *
 */
public class Splunk {
  public static void doSplunkSearch(String splunkApp, String splunkAPIURL, String username,
      String password, GHRepository ghRepository, List<String> fileList) {
    for (String fileName : fileList) {
      String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
      String fileExt = FilenameUtils.getExtension(fileName);
      if (fileNameWithOutExt.length() >= 4) {
        String last4 = fileNameWithOutExt.substring(fileNameWithOutExt.length() - 4);
        if (last4.equals("Test")) {
          continue;
        }
      }

      if (fileExt.equals("prg") || fileExt.equals("inc") || fileExt.equals("java")
          || fileExt.equals("py") || fileExt.equals("rb") || fileExt.equals("scala")) {
        Thread splunkSearchThread = new Thread(
            new SplunkSearch(splunkApp, splunkAPIURL, username, password, fileName, ghRepository));
        splunkSearchThread.start();
      }
    }
  }

}
