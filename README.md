# SMAPPR

SMAPPR is a service used to map the Splunk issues back to GitHub.

## What can SMAPPR do?

SMAPPR does two main things-
1. For each GitHub repository, looks if there are any Splunk issues related to the files in the repository. If so, it logs an issue in that repository.
2. When a Pull Request is made, looks if there are any Splunk issues for the files that are modified in the PR. If so, it adds a comment on the PR with the issue number.

## Configuring and running SMAPPR

SMAPPR can be run for a GitHub repository.

## Configuring the pipeline step

To configure the pipeline step that adds the comment to a pull request, you will need to ensure the following for any Jenkins box that the pipeline job executes on:

1. Python is installed on the box
2. PyGithub has been installed on the box (required PIP)
3. The Credentials Binding Plugin has been installed on Jenkins, and a Github Access Token has been added with the `cerner-github` id.

Currently, the code that adds the comment to pull requests is hard coded into a CCL pipeline script. It will need to be extracted into its own functions that will then allow users to add the pipeline library to any Jenkinsfile and have them use the resource anywhere in their own pipelines.