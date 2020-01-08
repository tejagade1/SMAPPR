package com.company.overnightshippers.jenkins

def isPullRequest() {
  env.CHANGE_ID != null
}

def shellExecute(command) {
  if (isUnix()) {
    sh command
  } else {
    bat command
  }
}

def GitHubBotCaller(accessToken, org, project, pullRequestId) {
  def githubCommentOnCommitScriptPath = libraryResource 'com/company/overnightshippers/python/gitHubBot.py'
  shellExecute(script: "python -c '${githubCommentOnCommitScriptPath}' ${accessToken} ${pullRequestId} ${org + '/' + project}", returnStdout: true)
}

def filesChangedBetweenCommits(commit1, commit2) {
  shellExecute(script: "git diff --name-only ${commit1} ${commit2}", returnStdout: true).trim().split("\n").toList()
}