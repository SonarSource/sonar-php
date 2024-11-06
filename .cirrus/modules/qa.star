load("github.com/SonarSource/cirrus-modules/cloud-native/actions.star@analysis/master", "default_gradle_on_failure")
load("github.com/SonarSource/cirrus-modules/cloud-native/platform.star@analysis/master", "base_image_container_builder",
     "ec2_instance_builder")
load("github.com/SonarSource/cirrus-modules/cloud-native/conditions.star@analysis/master", "is_branch_qa_eligible")
load("github.com/SonarSource/cirrus-modules/cloud-native/env.star@analysis/master", "artifactory_reader_env")
load("build.star", "profile_report_artifacts")
load(
  "github.com/SonarSource/cirrus-modules/cloud-native/cache.star@analysis/master",
  "gradle_cache",
  "cleanup_gradle_script",
  "gradle_wrapper_cache",
  "orchestrator_cache",
  "set_orchestrator_home_script",
  "mkdir_orchestrator_home_script",
)

QA_PLUGIN_GRADLE_TASK = "its:plugin:tests:integrationTest"
QA_RULING_GRADLE_TASK = "its:ruling:integrationTest"
QA_QUBE_LATEST_RELEASE = "LATEST_RELEASE"


def on_failure():
  return default_gradle_on_failure() | {
    "junit_artifacts": {
      "path": "**/test-results/**/*.xml",
      "format": "junit"
    }
  }


#
# Windows
#

def qa_win_script():
  return [
    "git config --global core.autocrlf input",
    "source cirrus-env CI",
    "./gradlew ${GRADLE_COMMON_FLAGS} test"
  ]


def qa_os_win_task():
  return {
    "qa_os_win_task": {
      "only_if": is_branch_qa_eligible(),
      "depends_on": "build",
      "ec2_instance": ec2_instance_builder(),
      "env": artifactory_reader_env(),
      "gradle_cache": gradle_cache(),
      "gradle_wrapper_cache": gradle_wrapper_cache(),
      "build_script": qa_win_script(),
      "on_success": profile_report_artifacts(),
      "on_failure": on_failure(),
    }
  }


#
# Commons
#

def qa_task(env, run_its_script):
  return {
    "only_if": is_branch_qa_eligible(),
    "depends_on": "build",
    "eks_container": base_image_container_builder(cpu=4, memory="10G"),
    "env": env,
    "gradle_cache": gradle_cache(),
    "gradle_wrapper_cache": gradle_wrapper_cache(),
    "set_orchestrator_home_script": set_orchestrator_home_script(),
    "mkdir_orchestrator_home_script": mkdir_orchestrator_home_script(),
    "orchestrator_cache": orchestrator_cache(),
    "run_its_script": run_its_script,
    "on_failure": on_failure(),
    "cleanup_gradle_script": cleanup_gradle_script(),
  }


#
# Plugin
#

def qa_plugin_env():
  return {
    "GRADLE_TASK": QA_PLUGIN_GRADLE_TASK,
    "KEEP_ORCHESTRATOR_RUNNING": "true",
    "matrix": [
      {"SQ_VERSION": QA_QUBE_LATEST_RELEASE},
      {"SQ_VERSION": "DEV"},
    ],
    "GITHUB_TOKEN": "VAULT[development/github/token/licenses-ro token]",
  }


def qa_plugin_script():
  return [
    "git submodule update --init --depth 1",
    "source cirrus-env QA",
    "source .cirrus/use-gradle-wrapper.sh",
    "./gradlew \"${GRADLE_TASK}\" \"-Dsonar.runtimeVersion=${SQ_VERSION}\" --info --build-cache --console plain --no-daemon"
  ]


def qa_plugin_task():
  return {
    "qa_plugin_task": qa_task(qa_plugin_env(), qa_plugin_script())
  }


#
# Ruling
#

def qa_ruling_env():
  return {
    "GRADLE_TASK": QA_RULING_GRADLE_TASK,
    "SQ_VERSION": QA_QUBE_LATEST_RELEASE,
    "KEEP_ORCHESTRATOR_RUNNING": "true",
    "matrix": [
      {"PHP_PROJECT": "Flysystem"},
      {"PHP_PROJECT": "Monica"},
      {"PHP_PROJECT": "PhpCodeSniffer"},
      {"PHP_PROJECT": "PhpMailer"},
      {"PHP_PROJECT": "Psysh"},
      {"PHP_PROJECT": "PhpWord"},
      {"PHP_PROJECT": "RubixML"},
      {"PHP_PROJECT": "PhpSpreadsheet"},
    ],
    "GITHUB_TOKEN": "VAULT[development/github/token/licenses-ro token]",
  }


def qa_ruling_script():
  return [
    "git submodule update --init --depth 1",
    "source cirrus-env QA",
    "source .cirrus/use-gradle-wrapper.sh",
    "./gradlew \"${GRADLE_TASK}\" \"-Dsonar.runtimeVersion=${SQ_VERSION}\" --tests \"PhpGeneralRulingTest.test${PHP_PROJECT}\" --info --build-cache --console plain --no-daemon"
  ]


def qa_ruling_task():
  return {
    "qa_ruling_task": qa_task(qa_ruling_env(), qa_ruling_script())
  }


#
# PR Analysis
#

def qa_pr_analysis_env():
  return {
    "GRADLE_TASK": QA_RULING_GRADLE_TASK,
    "SQ_VERSION": QA_QUBE_LATEST_RELEASE,
    "KEEP_ORCHESTRATOR_RUNNING": "true",
    "GITHUB_TOKEN": "VAULT[development/github/token/licenses-ro token]",
  }


def qa_pr_analysis_script():
  return [
    "git submodule update --init --depth 1",
    "source cirrus-env QA",
    "source .cirrus/use-gradle-wrapper.sh",
    "./gradlew \"${GRADLE_TASK}\" \"-Dsonar.runtimeVersion=${SQ_VERSION}\" --tests \"PhpPrAnalysisTest\" --info --build-cache --console plain --no-daemon"
  ]


def qa_pr_analysis_task():
  return {
    "qa_pr_analysis_task": qa_task(qa_pr_analysis_env(), qa_pr_analysis_script())
  }
