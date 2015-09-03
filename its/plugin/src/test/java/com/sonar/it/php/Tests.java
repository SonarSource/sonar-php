/*
 * Copyright (C) 2011-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.it.php;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.locator.FileLocation;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  PHPTest.class,
  PHPIntegrationTest.class,
  PHPUnitTest.class,
  CommonRulesTest.class,
  ReportWithUnresolvedPathTest.class
})
public class Tests {

  private static final String PLUGIN_KEY = "php";

  @ClassRule
  public static final Orchestrator ORCHESTRATOR;

  static {
    OrchestratorBuilder orchestratorBuilder = Orchestrator.builderEnv()
      .addPlugin(FileLocation.of("../../sonar-php-plugin/target/sonar-php-plugin.jar"))
      .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/php/profile.xml"));
    ORCHESTRATOR = orchestratorBuilder.build();
  }

  public static boolean is_after_plugin(String version) {
    return ORCHESTRATOR.getConfiguration().getPluginVersion(PLUGIN_KEY).isGreaterThanOrEquals(version);
  }

}
