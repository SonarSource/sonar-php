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

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LogTester;
import org.sonar.check.Rule;
import org.sonar.php.ini.PhpIniCheck;
import org.sonar.php.ini.PhpIniIssue;
import org.sonar.php.ini.tree.PhpIniFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.ini.BasePhpIniIssue.newIssue;

public class PhpIniSensorTest {

  @org.junit.Rule
  public LogTester logTester = new LogTester();

  @Test
  public void describe() throws Exception {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("Analyzer for \"php.ini\" files");
  }

  @Test
  public void single_file() throws Exception {
    File baseDir = new File("src/test/resources/phpini");
    SensorContextTester context = SensorContextTester.create(baseDir);
    DefaultInputFile file1 = setupSingleFile(baseDir, context);
    sensor().execute(context, checks());

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo("rule1");
    assertThat(issue.primaryLocation().inputComponent()).isEqualTo(file1);
    assertThat(issue.primaryLocation().message()).isEqualTo("message1");
    assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(2);
  }

  @Test
  public void parse_error() throws Exception {
    File baseDir = new File("src/test/resources/phpini-error");
    SensorContextTester context = SensorContextTester.create(baseDir);
    DefaultInputFile file = setupSingleFile(baseDir, context);
    sensor().execute(context, checks());
    assertThat(logTester.logs()).contains("Unable to parse file: " + file.absolutePath());
  }

  private DefaultInputFile setupSingleFile(File baseDir, SensorContextTester context) throws IOException {
    DefaultInputFile file1 = TestInputFileBuilder.create("moduleKey", baseDir, new File(baseDir, "php.ini"))
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata(Files.toString(new File(baseDir, "php.ini"), StandardCharsets.UTF_8))
      .build();
    context.fileSystem().add(file1);
    return file1;
  }

  private Checks<PhpIniCheck> checks() {
    ActiveRules activeRules = new ActiveRulesBuilder()
      .create(RuleKey.of("repo1", "rule1"))
      .activate()
      .build();
    CheckFactory checkFactory = new CheckFactory(activeRules);
    return checkFactory.<PhpIniCheck>create("repo1").addAnnotatedChecks(MyCheck.class);
  }

  private PhpIniSensor sensor() {
    return new PhpIniSensor(null);
  }

  @Rule(key = "rule1")
  public static class MyCheck implements PhpIniCheck {
    @Override
    public List<PhpIniIssue> analyze(PhpIniFile phpIniFile) {
      return ImmutableList.of(newIssue("message1").line(2));
    }
  }

}
