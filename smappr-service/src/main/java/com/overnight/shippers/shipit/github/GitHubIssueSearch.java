package com.overnight.shippers.shipit.github;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;

/**
 * 
 * @author Tejaswi Gade
 *
 */
public class GitHubIssueSearch {
	
	private final GHRepository ghRepository;
	
	private boolean issueAlreadyExists;
	/**
	 * 
	 * @param ghRepository
	 */
	public GitHubIssueSearch(GHRepository ghRepository) {
		this.ghRepository = ghRepository;
		this.setIssueAlreadyExists(false);
	}
	
	public void performSearch(String searchString) throws IOException {
		System.out.println("Searching for existing similar issues in the repo...");
		List<GHIssue> issues = ghRepository.getIssues(GHIssueState.OPEN);
		
		for(GHIssue issue : issues) {
			if(issue.getTitle().toLowerCase().contains(searchString.toLowerCase()) && !issue.isPullRequest()) {
				setIssueAlreadyExists(true);
			}
		}
		System.out.println("Successfully completed the search for issues.");
	}
	
	public boolean isIssueAlreadyExists() {
		return issueAlreadyExists;
	}
	
	public void setIssueAlreadyExists(boolean issueAlreadyExists) {
		this.issueAlreadyExists = issueAlreadyExists;
	}
	
	

}
