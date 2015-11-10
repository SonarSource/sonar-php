/*
 * PHP :: Integration Tests
 * Copyright (C) 2011 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sonar.it.php;

import com.google.common.collect.Iterables;
import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.locator.FileLocation;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  PHPTest.class,
  PHPIntegrationTest.class,
  PHPUnitTest.class,
  CommonRulesTest.class,
  CustomRulesTest.class,
  ReportWithUnresolvedPathTest.class
})
public class Tests {

  public static final String PROJECT_ROOT_DIR = "../projects/";
  private static final String PLUGIN_KEY = "php";
  private static final String RESOURCE_DIRECTORY = "/com/sonar/it/php/";

  @ClassRule
  public static final Orchestrator ORCHESTRATOR;

  static {
    OrchestratorBuilder orchestratorBuilder = Orchestrator.builderEnv()
      // PHP Plugin
      .addPlugin(FileLocation.of(Iterables.getOnlyElement(Arrays.asList(new File("../../../sonar-php-plugin/target/").listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(".jar") && !name.endsWith("-sources.jar");
        }
      }))).getAbsolutePath()))
      .restoreProfileAtStartup(FileLocation.ofClasspath(RESOURCE_DIRECTORY + "profile.xml"))
      // Custom rules plugin
      .addPlugin(FileLocation.of("../plugins/php-custom-rules-plugin/target/php-custom-rules-plugin-1.0-SNAPSHOT.jar"))
      .restoreProfileAtStartup(FileLocation.ofClasspath(RESOURCE_DIRECTORY + "profile-php-custom-rules.xml"));
    ORCHESTRATOR = orchestratorBuilder.build();
  }

  public static boolean is_after_plugin(String version) {
    return ORCHESTRATOR.getConfiguration().getPluginVersion(PLUGIN_KEY).isGreaterThanOrEquals(version);
  }

  public static final File projectDirectoryFor(String projectDirName) {
    return new File(Tests.PROJECT_ROOT_DIR + projectDirName + "/");
  }
}
