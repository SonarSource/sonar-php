#!/bin/bash
set -euo pipefail
echo "Running $TEST with SQ=$SQ_VERSION"

export JAVA_HOME=/opt/sonarsource/jvm/java-1.9.0-sun-x64
export PATH=$JAVA_HOME/bin:$PATH

case "$TEST" in
  plugin|ruling)  
    
  cd its/$TEST
  mvn -Dsonar.runtimeVersion="$SQ_VERSION" -Dmaven.test.redirectTestOutputToFile=false package
  ;;

  *)
  echo "Unexpected TEST mode: $TEST"
  exit 1
  ;;
esac

