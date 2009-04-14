/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.phpdepend;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.sonar.plugins.api.maven.model.MavenPom;
import org.sonar.plugins.php.Php;

import java.io.File;

public class PhpDependConfiguration {

  private MavenPom pom = null;
  protected static final String KEY_PATH = "phpdepend.path";
  protected static final String DEFAUT_PATH = "";

  protected static final String COMMAND_LINE = "pdepend";

  public static final String PHPUNIT_OPT = "phpunit-xml";

  public static final String SUFFIXES_OPT = "suffix";  


  public PhpDependConfiguration(MavenPom pom) {
    this.pom = pom;
  }

  // Only for unit tests
  protected PhpDependConfiguration() {
  }

  public String getCommandLine() {
    String path = getCommandLinePath();
    // For Windows
    if (isOsWindows()) {
      return path + COMMAND_LINE + ".bat";
      // For Unix like systems
    } else {
      if (StringUtils.isEmpty(path)) {
        return COMMAND_LINE;
      } else {
        path = StringUtils.removeEnd(path, "/");
        return path + "/" + COMMAND_LINE;
      }
    }
  }

  public String getReportFilecommandOption(){
    return "--"+ PHPUNIT_OPT +"="+ getReportFile().getAbsolutePath();
  }

  public String getSuffixesCommandOption() {
    return "--" + SUFFIXES_OPT + "=" + StringUtils.join(Php.SUFFIXES, ",");
  }

  public File getReportFile(){
    return new File(pom.getBuildDir(), "phpdepend-report.xml");
  }

  public File getSourceDir() {
    return pom.getSourceDir();
  }

  protected boolean isOsWindows() {
    return SystemUtils.IS_OS_WINDOWS;
  }

  protected String getCommandLinePath(){
    return pom.getConfiguration().getString(KEY_PATH, DEFAUT_PATH);
  }

}
