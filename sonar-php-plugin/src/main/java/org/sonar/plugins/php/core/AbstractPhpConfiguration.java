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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.ProjectFileSystem;

import java.io.File;
import java.util.List;

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

  private Settings settings;
  private final ProjectFileSystem fileSystem;

  private File reportFile;

  /**
   * @param project
   */
  protected AbstractPhpConfiguration(Settings settings, ProjectFileSystem fileSystem) {
    this.settings = settings;
    this.fileSystem = fileSystem;
  }

  /**
   * Gets the report file name.
   * 
   * @return the report file name
   */
  public String getReportFileName() {
    return settings.getString(getReportFileNameKey());
  }

  /**
   * Gets the report file relative path.
   * 
   * @return the report file relative path
   * @deprecated since 1.2
   */
  @Deprecated
  public String getReportFileRelativePath() {
    return settings.getString(getReportFileRelativePathKey());
  }

  /**
   * Gets the report path.
   * 
   * @return the report path
   * @since 1.2
   */
  public String getReportPath() {
    return settings.getString(getReportPathKey());
  }

  public File getDefaultReportFile() {
    return new File(getFileSystem().getSonarWorkingDirectory(), getCommandLine() + ".xml");
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
    if (reportFile == null) {
      String reportPath = getReportPath();
      if (StringUtils.isBlank(reportPath)) {
        // Test if deprecated properties are used
        if (getSettings().hasKey(getReportFileNameKey())) {
          LOG.warn("/!\\ " + getReportFileNameKey() + " is deprecated. Please update project settings and use " + getReportPathKey());
          StringBuilder fileName = new StringBuilder(getReportFileRelativePath()).append(File.separator);
          fileName.append(getReportFileName());
          reportFile = new File(getFileSystem().getBuildDir(), fileName.toString());
        }
        else {
          reportFile = getDefaultReportFile();
        }
      }
      else {
        reportFile = getFileSystem().resolvePath(reportPath);
      }
      LOG.info("Report file for: " + getCommandLine() + " : " + reportFile);
    }
    return reportFile;
  }

  /**
   * Gets the argument line.
   * 
   * @return the argument line
   */
  public String getArgumentLine() {
    return settings.getString(getArgumentLineKey());
  }

  /**
   * The timeout (in minutes) used to execute the tool.
   * 
   * @return the timeout
   */
  public int getTimeout() {
    String timeoutKey = getTimeoutKey();
    int timeout = 0;
    if (settings.hasKey(timeoutKey)) {
      timeout = settings.getInt(timeoutKey);
    } else {
      String defaultTimeout = settings.getDefaultValue(timeoutKey);
      if (StringUtils.isNotEmpty(defaultTimeout)) {
        timeout = Integer.parseInt(defaultTimeout);
      }
    }
    return timeout;
  }

  /**
   * Checks if is analyze only.
   * 
   * @return true, if is analyze only
   */
  public boolean isAnalyseOnly() {
    return getBooleanFromSettings(getShouldAnalyzeOnlyKey());
  }

  /**
   * Checks if the tool should be skipped.
   * 
   * @return true, if it should be skipped
   */
  public boolean isSkip() {
    boolean skip = false;
    if (settings.hasKey(getSkipKey())) {
      skip = settings.getBoolean(getSkipKey());
    } else if (settings.hasKey(getShouldRunKey())) {
      skip = !settings.getBoolean(getShouldRunKey());
    }
    return skip;
  }

  /**
   * Tells whether dynamic analysis is enabled or not
   * 
   * @return true if dynamic analysis is enabled
   */
  public boolean isDynamicAnalysisEnabled() {
    boolean isDynamicAnalysis = true;
    if (settings.hasKey("sonar.dynamicAnalysis")) {
      isDynamicAnalysis = settings.getBoolean("sonar.dynamicAnalysis");
    }
    return isDynamicAnalysis;
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
    File target = getFileSystem().getBuildDir();
    File logs = new File(target, getReportFileRelativePath());
    synchronized (this) {
      logs.mkdirs();
    }
    return logs;
  }

  /**
   * Gets the source directories.
   * 
   * @return the source directories
   */
  public List<File> getSourceDirectories() {
    return getFileSystem().getSourceDirs();
  }

  /**
   * Gets the project test source directories.
   * 
   * @return List<File> A list of all test source folders
   */
  public List<File> getTestDirectories() {
    return getFileSystem().getTestDirs();
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
   * Returns the current module filesystem
   * 
   * @return the project
   */
  public ProjectFileSystem getFileSystem() {
    return fileSystem;
  }

  /**
   * Returns the settings of the project
   * 
   * @return the settings
   */
  protected Settings getSettings() {
    return settings;
  }

  /**
   * If the settings has the key, then returns the value.
   * If not, then tries to find the default value.
   * It no default value is specified, then false is returned.
   */
  protected final boolean getBooleanFromSettings(String key) {
    if (getSettings().hasKey(key)) {
      return getSettings().getBoolean(key);
    } else {
      String defaultValue = getSettings().getDefaultValue(key);
      return StringUtils.isNotEmpty(defaultValue) && Boolean.parseBoolean(defaultValue);
    }
  }

  /**
   * Gets the command line.
   * 
   * @return the command line
   */
  protected abstract String getCommandLine();

  /**
   * Gets the argument line key.
   * 
   * @return the argument line key
   */
  protected abstract String getArgumentLineKey();

  /**
   * Gets the report path key.
   * 
   * @return the report path key
   */
  protected abstract String getReportPathKey();

  /**
   * Gets the report file name key.
   * 
   * @return the report file name key
   * @deprecated
   */
  @Deprecated
  protected abstract String getReportFileNameKey();

  /**
   * Gets the report file relative path key.
   * 
   * @return the report file relative path key
   * @deprecated
   */
  @Deprecated
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
   * Get the parameter that gives the timeout for the tool
   * 
   * @return the parameter name
   */
  protected abstract String getTimeoutKey();

  /**
   * Gets the should run key.
   * 
   * @deprecated Not used anymore, "skip" is preferred (this method should disappear some day)
   * 
   * @return the should run key
   */
  @Deprecated
  protected abstract String getShouldRunKey();

}
