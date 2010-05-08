/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 MyCompany
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

package org.sonar.plugins.php.core.configuration;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.sonar.api.resources.Project;

/**
 * Abstract php plugin configuration. Each php plugin should redefine properties names, it handles common properties initialization.
 */
public abstract class PhpPluginAbstractConfiguration {

  /**
   * Suffix used by windows for script files
   */
  private static final String WINDOWS_BAT_SUFFIX = ".bat";

  /** Indicates whether the plugin should only analyze results or launch tool. */
  private boolean analyzeOnly;

  /** The tool argument line. */
  private String argumentLine;

  /** The configured project. */
  protected Project project = null;

  /** The report file name. */
  private String reportFileName;

  /** The report file relative path. */
  private String reportFileRelativePath;

  /** Indicates whether the plugin should run on this project. */
  private boolean shouldRun;

  /**
   * Gets the argument line.
   * 
   * @return the argument line
   */
  public String getArgumentLine() {
    return argumentLine;
  }

  /**
   * Gets the argument line key.
   * 
   * @return the argument line key
   */
  protected abstract String getArgumentLineKey();

  /**
   * Gets the command line.
   * 
   * @return the command line
   */
  protected abstract String getCommandLine();

  /**
   * Gets the default argument line.
   * 
   * @return the default argument line
   */
  protected abstract String getDefaultArgumentLine();

  /**
   * Gets the default report file name.
   * 
   * @return the default report file name
   */
  protected abstract String getDefaultReportFileName();

  /**
   * Gets the default report file path.
   * 
   * @return the default report file path
   */
  protected abstract String getDefaultReportFilePath();

  /**
   * Gets operating system dependent launching script name.
   * 
   * <pre>
   * As an example : 
   * 	For windows php unit launching script is : punit.bat
   *  For Unix  php unit launching script is : punit
   * </pre>
   * 
   * @return the command line
   */
  public String getOsDependentToolScriptName() {
    // For Windows
    if (isOsWindows()) {
      return new StringBuilder(getCommandLine()).append(WINDOWS_BAT_SUFFIX).toString();
      // For Unix like systems
    } else {
      return getCommandLine();
    }
  }

  /**
   * Gets the report file.
   * 
   * <pre>
   * The path is construct as followed : 
   * {PORJECT_BUILD_DIR}\{CONFIG_RELATIVE_REPORT_FILE}\{CONFIG_REPORT_FILE_NAME}
   * </pre>
   * 
   * @return the report file
   */
  public File getReportFile() {
    return new File(project.getFileSystem().getBuildDir(), new StringBuilder().append(reportFileRelativePath).append(File.separator)
        .append(reportFileName).toString());
  }

  /**
   * Returns <code>true<code> if property is not null or empty.
   * <pre>
   *    value.equals(null) return false
   *    value.equals("") return false
   *    value.equals("  ") return false
   *    value.equals(" toto ") return true
	 * </pre>
   * 
   * @param key
   *          the property's key
   * @return <code>true<code> if property is not null or empty; <code>false</code> any other way.
   */
  public boolean isStringPropertySet(String key) {
    return !StringUtils.isBlank(project.getConfiguration().getString(key));
  }

  /**
   * Gets the report file name key.
   * 
   * @return the report file name key
   */
  protected abstract String getReportFileNameKey();

  /**
   * Gets the report file relative path.
   * 
   * @return the report file relative path
   */
  public String getReportFileRelativePath() {
    return reportFileRelativePath;
  }

  /**
   * Gets the report file relative path key.
   * 
   * @return the report file relative path key
   */
  protected abstract String getReportFileRelativePathKey();

  /**
   * Gets the should analyze only key.
   * 
   * @return the should analyze only key
   */
  protected abstract String getShouldAnalyzeOnlyKey();

  /**
   * Gets the should run key.
   * 
   * @return the should run key
   */
  protected abstract String getShouldRunKey();

  /**
   * Gets the source directories.
   * 
   * @return the source directories
   */
  public List<File> getSourceDirectories() {
    return project.getFileSystem().getSourceDirs();
  }

  /**
   * Gets the project test source directories.
   * 
   * @return List<File> A list of all test source folders
   */
  public List<File> getTestDirectories() {
    return project.getFileSystem().getTestDirs();
  }

  /**
   * Initialize the configuration with the given project's data.
   * 
   * @param aProject
   *          the project to analyze
   */
  protected void init(Project aProject) {
    this.project = aProject;
    if (getReportFileNameKey() != null) {
      reportFileName = project.getConfiguration().getString(getReportFileNameKey(), getDefaultReportFileName());
    }
    if (getReportFileRelativePathKey() != null) {
      reportFileRelativePath = project.getConfiguration().getString(getReportFileRelativePathKey(), getDefaultReportFilePath());
      File reportDirectory = new File(project.getFileSystem().getBuildDir().getAbsolutePath(), reportFileRelativePath);
      reportDirectory.mkdir();

    }
    if (getArgumentLineKey() != null) {
      argumentLine = project.getConfiguration().getString(getArgumentLineKey(), getDefaultArgumentLine());
    }
    if (getShouldAnalyzeOnlyKey() != null) {
      analyzeOnly = project.getConfiguration().getBoolean(getShouldAnalyzeOnlyKey(), shouldAnalyzeOnlyDefault());
    }
    if (getShouldRunKey() != null) {
      shouldRun = project.getConfiguration().getBoolean(getShouldRunKey(), shouldRunDefault());
    }
  }

  /**
   * Checks if is analyze only.
   * 
   * @return true, if is analyze only
   */
  public boolean isAnalyseOnly() {
    return analyzeOnly;
  }

  /**
   * Checks if running os is windows.
   * 
   * @return true, if os is windows
   */
  protected boolean isOsWindows() {
    return SystemUtils.IS_OS_WINDOWS;
  }

  /**
   * Checks if is should run.
   * 
   * @return true, if is should run
   */
  public boolean isShouldRun() {
    return shouldRun;
  }

  /**
   * Should analyze only default.
   * 
   * @return true, if successful
   */
  protected abstract boolean shouldAnalyzeOnlyDefault();

  /**
   * Should run default.
   * 
   * @return true, if successful
   */
  protected abstract boolean shouldRunDefault();
}
