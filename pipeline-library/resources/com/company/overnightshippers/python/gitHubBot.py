from github import Github
import sys
import os

accessToken = sys.argv[1]
orgRepoName = sys.argv[3]
pullReqId = int(sys.argv[2])

orgName = orgRepoName.split("/")[0]
repoName = orgRepoName.split("/")[1]


# Get Repo
g = Github(base_url="https://github.com/api/v3", login_or_token=accessToken)
repo = g.get_organization(orgName).get_repo(repoName)

# Get all Issues
label = repo.get_label(name="Smappr")
open_issues = repo.get_issues(state="open", labels=[label])

# Get file diffs
diffs = open("file-diffs.txt", "r").readline()

comment = "There are existing issue(s) related to your file changes: Want to fix them?<br>"

diffs = diffs.split(" ")

filesQualifiedCount = 0
for eachFile in diffs:
  issuesRelatedPerFile = 0
  fileComment = ""
  for issue in open_issues:
    print str(issue.title)
    if str(eachFile).lower() in str(issue.title).lower():
      if(issuesRelatedPerFile == 0):
        fileComment = eachFile + ":"
        filesQualifiedCount += 1
      elif(issuesRelatedPerFile > 0):
        fileComment += ","
      fileComment += " #[" + str(issue.number) + "](" + issue.html_url + ")"
      issuesRelatedPerFile += 1
  comment += fileComment
  comment += "<br>"


if filesQualifiedCount > 0:
  # Comment on the Issue
  pull = repo.get_pull(pullReqId)
  pull.create_issue_comment(comment)
