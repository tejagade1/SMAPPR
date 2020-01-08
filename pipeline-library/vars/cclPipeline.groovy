import com.company.overnightshippers.jenkins.Utils
import groovy.json.JsonSlurper

def call(Map config = [:]) {
  def utils =  new Utils()
  pipeline {
    agent {
      label config.label ?: ''
    }
    tools {
      maven '3.5'
      jdk '1.8'
    }
    options {
      buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
    }
    stages {
      stage ('Test') {
        steps{
          echo "Testing..."
        }
      }
      stage ('Deploy to SVN') {
        steps {
          echo "Deploying..."
        }
      }
      stage('Find Other Issues') {
        when {
          expression { utils.isPullRequest() && ! currentBuild.rawBuild.getPreviousSuccessfulBuild() } 
          }
        steps {
          script {
            echo "Finding other issues"
            withCredentials([string(credentialsId: "company-github", variable: "TOKEN")]) {
              def url = utils.shellExecute(script: "git config --get remote.origin.url", returnStdout: true)
                .trim()
                .replace("https://", "")
                .replace("git://", "")
                .replace(".git", "")
              def repoParts = url.split("/")
              def curlCmd = "curl -u ${TOKEN}:x-oauth-basic https://github.company.com/api/v3/repos/${repoParts[1]}/${repoParts[2]}/git/refs/heads/master"
              def curlResponse = utils.shellExecute(script: curlCmd, returnStdout: true).trim()
              def masterSha = new JsonSlurper().parseText(curlResponse).object.sha
              def currentCommitSha = utils.shellExecute(script: 'git rev-parse HEAD', returnStdout: true).trim()
              def fileList = utils.filesChangedBetweenCommits(masterSha, currentCommitSha)
              
              //Converting relative file names to absolute file names
              newFileList = []
              for(x in fileList) {
                if(x.matches("(.*).(.*)") == true) 
                  x = x.split('[.]')[0]
                if(x.matches("(.*)/(.*)") == true) 
                  x = x.substring(x.lastIndexOf("/") + 1, x.length()) 
                newFileList.add(x)
              }

              writeFile(file: 'file-diffs.txt', text: newFileList.join(' '))
              echo utils.GitHubBotCaller(TOKEN, repoParts[1], repoParts[2], env.CHANGE_ID)
            }
          }
        }
      }
    }
  }
}