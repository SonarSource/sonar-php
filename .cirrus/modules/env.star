load(
  "github.com/SonarSource/cirrus-modules/cloud-native/env.star@analysis/master",
  "artifactory_env",
  "cirrus_env",
  "gradle_signing_env",
  "next_env",
  "gradle_env",
)


def project_version_env():
  return {
    "PROJECT_VERSION_CACHE_DIR": "project-version",
  }


def env():
  vars = artifactory_env()
  vars |= cirrus_env(depth=1)
  vars |= gradle_env()
  vars |= gradle_signing_env()
  vars |= next_env()
  vars |= project_version_env()
  return {"env": vars}
