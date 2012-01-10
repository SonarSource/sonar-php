/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.php.core;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.resources.Project;

/**
 * 
 * Each php plugin should redefine properties names, it handles common properties initialization.
 */
public abstract class AbstractPhpConfiguration implements BatchExtension {

  /**
   * Default timeout used for external tool execution
   */
  public static final int DEFAULT_TIMEOUT = 30;

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractPhpConfiguration.class);
  /** Suffix used by windows for script files */
  private static final String WINDOWS_BAT_SUFFIX = ".bat";

  protected Project project;
  /** Indicates whether dynamic analysis (test & unit test) should be activated or not. */
  protected boolean isDynamicAnalysis;
  /** Indicates whether the tool should be executed or not. */
  protected boolean skip;
  /** Indicates whether the plugin should only analyze results or launch tool. */
  protected boolean analyzeOnly;
  /** The tool argument line. */
  private String argumentLine;
  /** The report file name. */
  private String reportFileName;
  /** The report file relative path. */
  private String reportFileRelativePath;
  /** The timeout (in minutes) used to execute the tool */
  private int timeout;

  /**
   * @param project
   */
  protected AbstractPhpConfiguration(Project project) {
    this.project = project;
    Configuration configuration = getProject().getConfiguration();
    this.reportFileName = configuration.getString(getReportFileNameKey(), getDefaultReportFileName());
    this.reportFileRelativePath = configuration.getString(getReportFileRelativePathKey(), getDefaultReportFilePath());
    this.argumentLine = configuration.getString(getArgumentLineKey(), getDefaultArgumentLine());
    this.analyzeOnly = configuration.getBoolean(getShouldAnalyzeOnlyKey(), false);
    this.timeout = configuration.getInt(getTimeoutKey(), DEFAULT_TIMEOUT);
    if (isStringPropertySet(getSkipKey())) { // NOSONAR Use of non final method, that is not final for mocking purposes
      skip = project.getConfiguration().getBoolean(getSkipKey());
    } else if (isStringPropertySet(getShouldRunKey())) { // NOSONAR Use of non final method, that is not final for mocking purposes
      skip = !project.getConfiguration().getBoolean(getShouldRunKey());
    }
    String dynamicAnalysis = configuration.getString("sonar.dynamicAnalysis", "true");
    if (!"false".equalsIgnoreCase(dynamicAnalysis)) {
      isDynamicAnalysis = true;
    }
  }

  /**
   * Gets the argument line.
   * 
   * @return the argument line
   */
  public String getArgumentLine() {
    return argumentLine;
  }

  /**
   * Gets operating system dependent launching script name.
   * 
   * <pre>
   * As an example:
   * For windows php unit launching script is : punit.bat
   * For Unix  php unit launching script is : punit
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
   * @return the created working directory.
   */
  public File createWorkingDirectory() {
    File target = getProject().getFileSystem().getBuildDir();
    File logs = new File(target, getReportFileRelativePath());
    synchronized (this) {
      logs.mkdirs();
    }
    return logs;
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
    StringBuilder fileName = new StringBuilder(reportFileRelativePath).append(File.separator);
    fileName.append(reportFileName);
    File reportFile = new File(getProject().getFileSystem().getBuildDir(), fileName.toString());
    LOG.info("Report file for: " + getCommandLine() + " : " + reportFile);
    return reportFile;

  }

  /**
   * Gets the source directories.
   * 
   * @return the source directories
   */
  public List<File> getSourceDirectories() {
    return getProject().getFileSystem().getSourceDirs();
  }

  /**
   * Gets the project test source directories.
   * 
   * @return List<File> A list of all test source folders
   */
  public List<File> getTestDirectories() {
    return getProject().getFileSystem().getTestDirs();
  }

  /**
   * Checks if running os is windows.
   * 
   * @return true, if os is windows
   */
  public boolean isOsWindows() {
    return SystemUtils.IS_OS_WINDOWS;
  }

  /**
   * @return the project
   */
  public Project getProject() {
    return project;
  }

  /**
   * Returns <code>true<code> if property is not null or empty.
   * <pre>
   * value.equals(null) return false
   * value.equals("") return false
   * value.equals("  ") return false
   * value.equals(" toto ") return true
   * </pre>
   * 
   * @param key
   *          the property's key
   * @return <code>true<code> if property is not null or empty; <code>false</code> any other way.
   */
  public boolean isStringPropertySet(String key) {
    return project.getConfiguration().containsKey(key);
  }

  /**
   * Tells whether dynamic analysis is enabled or not
   * 
   * @return true if dynamic analysis is enabled
   */
  public boolean isDynamicAnalysisEnabled() {
    return isDynamicAnalysis;
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
   * Checks if the tool should be skipped.
   * 
   * @return true, if it should be skipped
   */
  public boolean isSkip() {
    return skip;
  }

  /**
   * The timeout (in minutes) used to execute the tool.
   * 
   * @return the timeout
   */
  public int getTimeout() {
    return timeout;
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
   * Get the parameter that tells if the sensor must be skipped or not
   * 
   * @return the parameter name
   */
  protected abstract String getSkipKey();

  /**
   * Gets the should run key.
   * 
   * @deprecated Not used anymore, "skip" is preferred (this method should disappear some day)
   * 
   * @return the should run key
   */
  @Deprecated
  protected abstract String getShouldRunKey();

  /**
   * Get the parameter that gives the timeout for the tool
   * 
   * @return the parameter name
   */
  protected abstract String getTimeoutKey();

}
