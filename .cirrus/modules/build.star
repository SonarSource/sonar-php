load(
    "github.com/SonarSource/cirrus-modules/cloud-native/env.star@analysis/master",
    "pgp_signing_env",
    "next_env",
    "whitesource_api_env"
)

load(
    "github.com/SonarSource/cirrus-modules/cloud-native/conditions.star@analysis/master",
    "is_main_branch",
    "is_branch_qa_eligible"
)
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/platform.star@analysis/master",
"custom_image_container_builder"
)
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/cache.star@analysis/master",
    "gradle_cache",
    "cleanup_gradle_script",
    "gradle_wrapper_cache",
    "project_version_cache",
    "store_project_version_script"
)


#
# Common
#

def profile_report_artifacts():
    return {
        "profile_report_artifacts": {
            "path": "build/reports/profile/profile-*.html"
        }
    }


#
# Build
#

def build_script():
    return [
        "source cirrus-env BUILD-PRIVATE",
        "source .cirrus/use-gradle-wrapper.sh",
        "regular_gradle_build_deploy_analyze ${BUILD_ARGUMENTS}",
        "source set_gradle_build_version ${BUILD_NUMBER}",
        "echo export PROJECT_VERSION=${PROJECT_VERSION} >> ~/.profile"
    ]


def build_env():
    env = pgp_signing_env()
    env |= next_env()
    env |= {
        "DEPLOY_PULL_REQUEST": "true",
        "BUILD_ARGUMENTS": "-DtrafficInspection=false --parallel --profile -x test -x sonar"
    }
    return env


def build_task():
    return {
        "build_task": {
            "env": build_env(),
            "eks_container": custom_image_container_builder(cpu=2, memory="3G"),
            "project_version_cache": project_version_cache(),
            "gradle_cache": gradle_cache(),
            "gradle_wrapper_cache": gradle_wrapper_cache(),
            "build_script": build_script(),
            "cleanup_gradle_script": cleanup_gradle_script(),
            "on_success": profile_report_artifacts(),
            "store_project_version_script": store_project_version_script()
        }
    }


def build_test_env():
    env = pgp_signing_env()
    env |= next_env()
    env |= {
        "DEPLOY_PULL_REQUEST": "false",
        "BUILD_ARGUMENTS": "-x artifactoryPublish"
    }
    return env


def whitesource_script():
    return [
        "source cirrus-env QA",
        "source .cirrus/use-gradle-wrapper.sh",
        "source ${PROJECT_VERSION_CACHE_DIR}/evaluated_project_version.txt",
        "GRADLE_OPTS=\"-Xmx64m -Dorg.gradle.jvmargs='-Xmx3G' -Dorg.gradle.daemon=false\" ./gradlew ${GRADLE_COMMON_FLAGS} :sonar-php-plugin:processResources -Pkotlin.compiler.execution.strategy=in-process",
        "source ws_scan.sh"
    ]


def sca_scan_task():
    return {
        "sca_scan_task": {
            "only_if": is_main_branch(),
            "depends_on": "build",
            "env": whitesource_api_env(),
            "eks_container": custom_image_container_builder(),
            "gradle_cache": gradle_cache(),
            "gradle_wrapper_cache": gradle_wrapper_cache(),
            "project_version_cache": project_version_cache(),
            "whitesource_script": whitesource_script(),
            "cleanup_gradle_script": cleanup_gradle_script(),
            "allow_failures": "true",
            "always": {
                "ws_artifacts": {
                    "path": "whitesource/**/*"
                }
            },
            "on_success": profile_report_artifacts(),
        }
    }
