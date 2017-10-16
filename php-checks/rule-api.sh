#!/usr/bin/env bash

set -e
set -o pipefail

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

declare RULE_CTX_DIR="$DIR/src/main/resources/org/sonar/l10n/php/rules/php"
declare RULE_CTX_LANG="php"
declare RULE_CTX_PROFILE="${RULE_CTX_DIR}/Sonar_way_profile.json"

declare RULE_API_GROUP_ID="com.sonarsource.rule-api"
declare RULE_API_ARTIFACT_ID="rule-api"
declare RULE_API_VERSION="1.16.0.954"

declare RULE_API_ARTIFACT="${RULE_API_GROUP_ID}:${RULE_API_ARTIFACT_ID}:${RULE_API_VERSION}"
declare RULE_API_JAR_DIR="${DIR}/target/dependency"
declare RULE_API_JAR="${RULE_API_JAR_DIR}/${RULE_API_ARTIFACT_ID}-${RULE_API_VERSION}.jar"

function show_usage {
  echo "Usage:"
  echo "  ./rule-api.sh generate -rule S1234 # Generate html and json description for one rule"
  echo "  ./rule-api.sh update               # Update html and json description files"
  echo "  ./rule-api.sh diff                 # Generates a diff report into target/reports/outdated"
}

function print_and_exec {
  printf "%q " "$@"
  printf "\n"
  "$@"
}

function rule_api {
  local RULE_CMD="$1"
  shift
  if [[ ! -e ${RULE_API_JAR} ]]; then
    mkdir -p "${RULE_API_JAR_DIR}"
    print_and_exec mvn --quiet org.apache.maven.plugins:maven-dependency-plugin:2.10:copy -Dartifact="${RULE_API_ARTIFACT}" -DoutputDirectory="${RULE_API_JAR_DIR}"
  fi
  # when updating, first delete the profile before execution to ensure a clean generation and get rid of deleted rule keys.
  if [[ ${RULE_CMD} == 'update' ]] && [[ -e "${RULE_CTX_PROFILE}" ]]; then
    print_and_exec rm "${RULE_CTX_PROFILE}"
  fi
  print_and_exec java -Djava.awt.headless=true -jar "${RULE_API_JAR}" "${RULE_CMD}" -preserve-filenames -no-language-in-filenames -language "${RULE_CTX_LANG}" -directory "${RULE_CTX_DIR}" "$@"
}

if [[ -z $1 ]]; then
  show_usage
else
  rule_api "$@"
fi
