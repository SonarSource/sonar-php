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

package org.sonar.plugins.php.phpcodesniffer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.sonar.plugins.api.maven.model.MavenPom;

import java.io.File;

public class PhpCodeSnifferConfiguration {

  private MavenPom pom = null;

  protected static final String KEY_PATH = "phpcodesniffer.path";
  protected static final String DEFAUT_PATH = "";

  protected static final String COMMAND_LINE = "phpcs";

  public static final String REPORT_FORMAT_OPT = "report";
  public static final String REPORT_FORMAT_DEFAULT_OPT = "xml";

  public static final String REPORT_FILE_OPT = "report-file";


  public PhpCodeSnifferConfiguration(MavenPom pom) {
    this.pom = pom;
  }

  // Only for unit tests
  protected PhpCodeSnifferConfiguration() {
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

  public String getReportFormatOption(){
    return "--"+ REPORT_FORMAT_OPT +"="+ REPORT_FORMAT_DEFAULT_OPT;
  }

  public String getReportFileOption(){
    return "--"+ REPORT_FILE_OPT +"="+ getReportFile().getAbsolutePath();
  }

  public File getReportFile(){
    return new File(pom.getBuildDir(), "phpcodesniffer-report.xml");
  }

  public File getSourceDir() {
    return pom.getBuildSourceDir();
  }

  protected boolean isOsWindows() {
    return SystemUtils.IS_OS_WINDOWS;
  }

  protected String getCommandLinePath(){
    return pom.getConfiguration().getString(KEY_PATH, DEFAUT_PATH);
  }

}