/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.check.Rule;
import org.sonar.php.ini.PhpIniCheck;
import org.sonar.php.ini.PhpIniIssue;
import org.sonar.php.ini.tree.PhpIniFile;

import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.php.ini.BasePhpIniIssue.newIssue;

class PhpIniSensorTest {

  @RegisterExtension
  final LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void describe() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("Analyzer for \"php.ini\" files");
  }

  @Test
  void singleFile() throws IOException {
    CheckFactory checkFactory = mock(CheckFactory.class);
    PhpIniSensor sensor = new PhpIniSensor(checkFactory);
    when(checkFactory.<PhpIniCheck>create(any())).thenReturn(checks());

    File baseDir = new File("src/test/resources/phpini");
    SensorContextTester context = SensorContextTester.create(baseDir);
    DefaultInputFile file1 = setupSingleFile(baseDir, context);
    sensor.execute(context);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo("rule1");
    assertThat(issue.primaryLocation().inputComponent()).isEqualTo(file1);
    assertThat(issue.primaryLocation().message()).isEqualTo("message1");
    TextRange textRange = issue.primaryLocation().textRange();
    assertThat(textRange).isNotNull();
    assertThat(textRange.start().line()).isEqualTo(2);
  }

  @Test
  void checkWhereLineNumberIsNegative() throws IOException {
    CheckFactory checkFactory = mock(CheckFactory.class);
    PhpIniSensor sensor = new PhpIniSensor(checkFactory);
    Checks<PhpIniCheck> checks = checks();
    PhpIniCheck rule1 = checks.of(RuleKey.parse("repo1:rule1"));
    ((MyCheck) rule1).lineNumber = -1;

    when(checkFactory.<PhpIniCheck>create(any())).thenReturn(checks);

    File baseDir = new File("src/test/resources/phpini");
    SensorContextTester context = SensorContextTester.create(baseDir);
    DefaultInputFile file1 = setupSingleFile(baseDir, context);
    sensor.execute(context);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo("rule1");
    assertThat(issue.primaryLocation().inputComponent()).isEqualTo(file1);
    assertThat(issue.primaryLocation().message()).isEqualTo("message1");
    TextRange textRange = issue.primaryLocation().textRange();
    assertThat(textRange).isNull();
  }

  @Test
  void parseError() throws Exception {
    File baseDir = new File("src/test/resources/phpini-error");
    SensorContextTester context = SensorContextTester.create(baseDir);
    setupSingleFile(baseDir, context);
    sensor().execute(context, checks());
    assertThat(logTester.logs()).contains("Unable to parse file: php.ini");
  }

  private static DefaultInputFile setupSingleFile(File baseDir, SensorContextTester context) throws IOException {
    String content = readString(Path.of(new File(baseDir, "php.ini").getPath()));
    DefaultInputFile file1 = TestInputFileBuilder.create("moduleKey", baseDir, new File(baseDir, "php.ini"))
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata(content)
      .build();
    context.fileSystem().add(file1);
    return file1;
  }

  private static Checks<PhpIniCheck> checks() {
    NewActiveRule rule = new NewActiveRule.Builder().setRuleKey(RuleKey.of("repo1", "rule1")).build();
    ActiveRules activeRules = new ActiveRulesBuilder()
      .addRule(rule)
      .build();
    CheckFactory checkFactory = new CheckFactory(activeRules);
    return checkFactory.<PhpIniCheck>create("repo1").addAnnotatedChecks(MyCheck.class);
  }

  private static PhpIniSensor sensor() {
    return new PhpIniSensor(new CheckFactory(new ActiveRulesBuilder().build()));
  }

  @Rule(key = "rule1")
  public static class MyCheck implements PhpIniCheck {
    public int lineNumber = 2;

    @Override
    public List<PhpIniIssue> analyze(PhpIniFile phpIniFile) {
      return Collections.singletonList(newIssue("message1").line(lineNumber));
    }
  }
}
