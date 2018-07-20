/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.php;

import com.google.common.annotations.VisibleForTesting;
import com.sonar.sslr.api.RecognitionException;
import java.util.List;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.php.checks.CheckList;
import org.sonar.php.compat.PhpFileImpl;
import org.sonar.php.ini.PhpIniCheck;
import org.sonar.php.ini.PhpIniIssue;
import org.sonar.php.ini.PhpIniParser;
import org.sonar.php.ini.tree.PhpIniFile;
import org.sonar.plugins.php.api.Php;

public class PhpIniSensor implements Sensor {

  private static final Logger LOG = Loggers.get(PhpIniSensor.class);

  private final CheckFactory checkFactory;

  public PhpIniSensor(CheckFactory checkFactory) {
    this.checkFactory = checkFactory;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("Analyzer for \"php.ini\" files")
      // don't target ".php" files, but it makes no sense to invoke this sensor for project without ".php" files
      .onlyOnLanguage(Php.KEY);
  }

  @Override
  public void execute(SensorContext context) {
    Checks<PhpIniCheck> checks = checkFactory
      .<PhpIniCheck>create(CheckList.REPOSITORY_KEY)
      .addAnnotatedChecks(CheckList.getPhpIniChecks());
    execute(context, checks);
  }

  @VisibleForTesting
  protected void execute(SensorContext context, Checks<PhpIniCheck> checks) {
    PhpIniParser parser = new PhpIniParser();
    FileSystem fs = context.fileSystem();
    Iterable<InputFile> inputFiles = fs.inputFiles(fs.predicates().matchesPathPattern("**/php.ini"));
    for (InputFile inputFile : inputFiles) {
      PhpIniFile phpIni;
      try {
        phpIni = parser.parse(new PhpFileImpl(inputFile));
      } catch (RecognitionException e) {
        LOG.error("Unable to parse file: " + inputFile.absolutePath());
        LOG.error(e.getMessage());
        continue;
      }
      for (PhpIniCheck check : checks.all()) {
        List<PhpIniIssue> issues = check.analyze(phpIni);
        saveIssues(context, inputFile, checks.ruleKey(check), issues);
      }
    }
  }

  private static void saveIssues(SensorContext context, InputFile inputFile, RuleKey ruleKey, List<PhpIniIssue> issues) {
    for (PhpIniIssue phpIssue : issues) {
      NewIssue issue = context.newIssue();

      NewIssueLocation location = issue.newLocation()
        .message(phpIssue.message())
        .on(inputFile);

      if (phpIssue.line() > 0) {
        location.at(inputFile.selectLine(phpIssue.line()));
      }

      issue
        .forRule(ruleKey)
        .at(location)
        .save();
    }
  }

}
