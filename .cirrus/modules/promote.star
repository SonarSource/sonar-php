load("github.com/SonarSource/cirrus-modules/cloud-native/env.star@analysis/master", "promotion_env")
load("github.com/SonarSource/cirrus-modules/cloud-native/platform.star@analysis/master", "base_image_container_builder")
load("github.com/SonarSource/cirrus-modules/cloud-native/cache.star@analysis/master", "project_version_cache")
load("github.com/SonarSource/cirrus-modules/cloud-native/conditions.star@analysis/master", "is_branch_qa_eligible")


def promote_script():
  return [
    "source cirrus-env PROMOTE",
    "cirrus_jfrog_promote",
    "source ${PROJECT_VERSION_CACHE_DIR}/evaluated_project_version.txt",
    "github-notify-promotion",
  ]


def promote_task():
  return {
    "promote_task": {
      "only_if": is_branch_qa_eligible(),
      "depends_on": [
        "build",
        "qa_os_win",
        "qa_ruling",
        "qa_plugin",
        "qa_pr_analysis"
      ],
      "env": promotion_env(),
      "eks_container": base_image_container_builder(cpu=1, memory="2G"),
      "project_version_cache": project_version_cache(),
      "script": promote_script()
    }
  }
