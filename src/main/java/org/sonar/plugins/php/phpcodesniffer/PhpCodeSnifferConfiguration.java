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
import org.sonar.plugins.php.Php;

import java.io.File;
import java.io.IOException;

public class PhpCodeSnifferConfiguration {

  private MavenPom pom = null;
  private String profileName;

  protected static final String KEY_PATH = "phpcodesniffer.path";
  protected static final String DEFAUT_PATH = "";

  protected static final String COMMAND_LINE = "phpcs";

  private static final String REPORT_FORMAT_OPT = "report";
  private static final String REPORT_FORMAT_DEFAULT_OPT = "xml";

  private static final String REPORT_FILE_OPT = "report-file";

  public static final String STANDARD_OPT = "standard";
  public static final String SUFFIXES_OPT = "extensions";


  public PhpCodeSnifferConfiguration(MavenPom pom, String profileName) {
    this.pom = pom;
    this.profileName = profileName;
  }

  // Only for unit tests
  protected PhpCodeSnifferConfiguration() {
  }


  public String getCommandLine() {
    String path = getCommandLinePath();
    // For Windows
    if (isOsWindows()) {
      return path + COMMAND_LINE + ".bat";

    }

    // For Unix like systems
    if (StringUtils.isEmpty(path)) {
      return COMMAND_LINE;
    }

    path = StringUtils.removeEnd(path, "/");
    return path + "/" + COMMAND_LINE;
  }

  public String getReportFormatCommandOption() {
    return "--" + REPORT_FORMAT_OPT + "=" + REPORT_FORMAT_DEFAULT_OPT;
  }

  public String getReportFileCommandOption() throws IOException {
    return "--" + REPORT_FILE_OPT + "=" + getReportFile().getCanonicalPath();
  }

  public String getSuffixesCommandOption() {
    return "--" + SUFFIXES_OPT + "=" + StringUtils.join(Php.SUFFIXES, ",");
  }

  public File getReportFile() {
    return new File(pom.getBuildDir(), "phpcodesniffer-report.xml");
  }

  public File getSourceDir() {
    return pom.getBuildSourceDir();
  }

  protected boolean isOsWindows() {
    return SystemUtils.IS_OS_WINDOWS;
  }

  protected String getCommandLinePath() {
    return pom.getConfiguration().getString(KEY_PATH, DEFAUT_PATH);
  }

  public String getStandardOption() throws IOException {
    return "--" + STANDARD_OPT + "=" + getDirProfile().getCanonicalPath();
  }

  public File getDirProfile() {
    return new File(pom.getSonarWorkingDirectory(), getCleanProfileName());
  }

  public String getCleanProfileName() {
    return StringUtils.replace(profileName, " ", "_");
  }

  public File getFileProfile() {
    String profileName = getCleanProfileName();
    File profileDir = getDirProfile();
    return new File(profileDir, profileName + "CodingStandard.php");
  }

}