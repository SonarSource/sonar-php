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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandExecutor;
import org.sonar.plugins.php.api.Php;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract php plugin executor. This class handles common executor needs such as running the process, reading its common and error output
 * streams and logging. In nominal case implementing executor should just construct the desire command line.
 */

public abstract class AbstractPhpExecutor implements BatchExtension {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractPhpExecutor.class);
  private static final long MINUTES_TO_MILLISECONDS = 60000;
  private static final String RULESET_PREFIX = "ruleset";
  private static final String XML_SUFFIX = ".xml";
  private static final String PHP_COMMAND_LINE = "php";

  private Php php;
  private AbstractPhpConfiguration configuration;
  private Collection<Integer> acceptedExitCodes;

  protected AbstractPhpExecutor(Php php, AbstractPhpConfiguration configuration) {
    this(php, configuration, Lists.newArrayList(0));
  }

  protected AbstractPhpExecutor(Php php, AbstractPhpConfiguration configuration, Collection<Integer> acceptedExitCodes) {
    this.php = php;
    this.configuration = configuration;
    this.acceptedExitCodes = acceptedExitCodes;
  }

  protected Php getPhpLanguage() {
    return php;
  }

  /**
   * Executes the external tool.
   */
  public void execute() {
    if (testExternalTool()) {
      executeExternalTool();
    }
    else if (getPHARName() != null) {
      executePhar();
    }
    else {
      throw new SonarException("Unable to find " + getExecutedTool());
    }
  }

  private void executeExternalTool() {
    List<String> commandLine = new LinkedList<String>();
    commandLine.add(configuration.getOsDependentToolScriptName());
    commandLine.addAll(getCommandLineArguments());
    LOG.info("Executing " + getExecutedTool() + " with command '{}'", prettyPrint(commandLine));

    doExecute(commandLine);
  }

  private void executePhar() {
    try {
      URL pharURL = getPHAREmbeddedURL();

      File workDir = new File(configuration.getFileSystem().getSonarWorkingDirectory(), getExecutedTool());
      File tempPhar = new File(workDir, getPHARName());
      if (!tempPhar.exists()) {
        FileUtils.copyURLToFile(pharURL, tempPhar);
      }

      List<String> commandLine = new LinkedList<String>();
      commandLine.add(PHP_COMMAND_LINE);
      commandLine.add(tempPhar.getAbsolutePath());
      commandLine.addAll(getCommandLineArguments());
      LOG.info("Executing embedded " + getExecutedTool() + " with command '{}'", prettyPrint(commandLine));

      doExecute(commandLine);
    } catch (Exception e) {
      throw new SonarException("Error during execution of embedded " + getExecutedTool(), e);
    }
  }

  @VisibleForTesting
  public URL getPHAREmbeddedURL() {
    URL pharURL = this.getClass().getResource("/" + getPHARName());
    if (pharURL == null) {
      throw new SonarException("Unable to find embedded PHAR archive " + getPHARName());
    }
    return pharURL;
  }

  private void doExecute(List<String> commandLine) {
    Iterator<String> commandLineIterator = commandLine.iterator();
    Command command = Command.create(commandLineIterator.next());
    command.setDirectory(configuration.getFileSystem().getBasedir());
    while (commandLineIterator.hasNext()) {
      command.addArgument(commandLineIterator.next());
    }
    int exitCode = CommandExecutor.create().execute(command, configuration.getTimeout() * MINUTES_TO_MILLISECONDS);
    if (!acceptedExitCodes.contains(exitCode)) {
      throw new SonarException(getExecutedTool() + " execution failed with returned code '" + exitCode
        + "'. Please check the documentation of " + getExecutedTool() + " to know more about this failure.");
    } else {
      LOG.info(getExecutedTool() + " succeeded with returned code '{}'.", exitCode);
    }
  }

  /**
   * Test presence of external tool .
   */
  public boolean testExternalTool() {
    List<String> commandLine = getTestCommandLine();
    LOG.debug("Testing " + getExecutedTool() + " with command '{}'", prettyPrint(commandLine));

    Iterator<String> commandLineIterator = commandLine.iterator();
    Command command = Command.create(commandLineIterator.next());
    command.setDirectory(configuration.getFileSystem().getBasedir());
    while (commandLineIterator.hasNext()) {
      command.addArgument(commandLineIterator.next());
    }
    try {
      int exitCode = CommandExecutor.create().execute(command, configuration.getTimeout() * MINUTES_TO_MILLISECONDS);
      LOG.debug(getExecutedTool() + " test succeeded with returned code '{}'.", exitCode);
      return exitCode == 0;
    } catch (Exception e) {
      LOG.debug(getExecutedTool() + " test failed");
      return false;
    }
  }

  protected File getRuleset(AbstractPhpConfiguration configuration, RulesProfile profile, ProfileExporter exporter) {
    File workingDir = configuration.createWorkingDirectory();
    File ruleset = null;
    Writer writer = null;
    try {
      ruleset = File.createTempFile(RULESET_PREFIX, XML_SUFFIX, workingDir);
      writer = new FileWriter(ruleset);
      exporter.exportProfile(profile, writer);
    } catch (IOException e) {
      String msg = "Error while creating temporary ruleset from profile: " + profile + " to file : " + ruleset + " in dir " + workingDir;
      LOG.error(msg);
    } finally {
      IOUtils.closeQuietly(writer);
    }
    return (ruleset != null && ruleset.length() > 0) ? ruleset : null;
  }

  /**
   * Returns a String where each list member is separated with a space
   * 
   * @param commandLine
   *          the external tool command line argument
   * @return String where each list member is separated with a space
   */
  @VisibleForTesting
  public static String prettyPrint(List<String> commandLine) {
    StringBuilder sb = new StringBuilder();
    for (Iterator<String> iter = commandLine.iterator(); iter.hasNext();) {
      String part = iter.next();
      sb.append(part);
      if (iter.hasNext()) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }

  /**
   * Gets the command line.
   * 
   * @return the command line
   */
  protected abstract List<String> getCommandLineArguments();

  /**
   * Gets the test command line used to test presence of external tool
   * 
   * @return the test command line
   */
  @VisibleForTesting
  public List<String> getTestCommandLine() {
    List<String> result = new ArrayList<String>();
    result.add(configuration.getOsDependentToolScriptName());
    result.add("--version");
    return result;
  }

  /**
   * Gets the executed tool.
   * 
   * @return the executed tool
   */
  protected abstract String getExecutedTool();

  /**
   * The PHAR name of the tool if it is embedded.
   * @return null if tool is not embedded
   */
  protected abstract String getPHARName();
}
