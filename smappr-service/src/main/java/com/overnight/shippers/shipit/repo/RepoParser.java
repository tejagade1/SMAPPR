package com.overnight.shippers.shipit.repo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

/**
 * 
 * @author Tejaswi Gade
 *
 */
public class RepoParser {
	
	private final GHRepository ghRepository; 
	
	/**
	 * 
	 * @param ghRepository
	 */
	public RepoParser(GHRepository ghRepository){
		this.ghRepository = ghRepository;
		
	}
	
	public ArrayList<String> getFilesInRepo() throws IOException{
		System.out.println("Retrieving the files in a given repo.");
		return getDirectoryContents(ghRepository, "", new ArrayList<String>());
	}
	
	private ArrayList<String> getDirectoryContents(GHRepository ghRepository, String directoryName, ArrayList<String> fileNames) throws IOException {
		if(ghRepository == null)
			throw new RuntimeErrorException(null, "GitHub repository is null.");
		
		List<GHContent> directoryContent;
		try {
				directoryContent = ghRepository.getDirectoryContent(directoryName);
		
		
				for(GHContent content : directoryContent) {
					if(content.isFile()) {
						fileNames.add(content.getName());
					}
					else {
						String pathName = directoryName + "/" + content.getName();
						getDirectoryContents(ghRepository, pathName, fileNames);
					}
				}
			} catch (IOException e) {
				throw new IOException("Cannot get the directory contents" + e.getMessage());
			}
		return fileNames;
	}
}
