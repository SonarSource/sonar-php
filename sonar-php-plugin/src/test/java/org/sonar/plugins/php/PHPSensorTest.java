/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
import com.sonar.sslr.api.RecognitionException;
import java.io.File;
import java.io.InterruptedIOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.internal.google.common.base.Charsets;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scan.issue.filter.FilterableIssue;
import org.sonar.api.scan.issue.filter.IssueFilterChain;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.PHPAnalyzer;
import org.sonar.php.checks.CheckList;
import org.sonar.php.checks.LeftCurlyBraceEndsLineCheck;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPCustomRulesDefinition;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.ProgressReport;
import org.sonar.squidbridge.api.AnalysisException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PHPSensorTest {

  private DefaultFileSystem fileSystem;

  private PHPSensor sensor;

  private ProgressReport progressReport = mock(ProgressReport.class);

  private final SensorContextTester context = SensorContextTester.create(new File("src/test/resources"));

  @org.junit.Rule
  public final ExpectedException thrown = ExpectedException.none();

  private final PHPCustomRulesDefinition[] CUSTOM_RULES = {new PHPCustomRulesDefinition() {
    @Override
    public String repositoryName() {
      return "custom name";
    }

    @Override
    public String repositoryKey() {
      return "customKey";
    }

    @Override
    public ImmutableList<Class> checkClasses() {
      return ImmutableList.<Class>of(MyCustomRule.class);
    }
  }};

  @Rule(
    key = "key",
    name = "name",
    description = "desc",
    tags = {"bug"})
  public class MyCustomRule extends PHPVisitorCheck {
    @RuleProperty(
      key = "customParam",
      description = "Custom parameter",
      defaultValue = "value")
    public String customParam = "value";
  }

  @Before
  public void setUp() {
    fileSystem = PhpTestUtils.getDefaultFileSystem();
    FileLinesContextFactory fileLinesContextFactory = createFileLinesContextFactory();
    CheckFactory checkFactory = new CheckFactory(mock(ActiveRules.class));

    sensor = new PHPSensor(fileSystem, fileLinesContextFactory, checkFactory, new NoSonarFilter(), CUSTOM_RULES);
  }

  @Test
  public void sensor_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor.describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("PHP sensor");
    assertThat(descriptor.languages()).containsOnly("php");
    assertThat(descriptor.type()).isEqualTo(Type.MAIN);
  }

  @Test
  public void analyse() throws NoSuchFieldException, IllegalAccessException {
    String fileName = "PHPSquidSensor.php";
    String componentKey = "moduleKey:" + fileName;

    analyseSingleFile(context, fileName);

    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.LINES, 55);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.NCLOC, 32);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.COMPLEXITY_IN_CLASSES, 7);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.COMPLEXITY_IN_FUNCTIONS, 10);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.COMMENT_LINES, 7);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.COMPLEXITY, 12);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.CLASSES, 1);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.STATEMENTS, 16);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.FUNCTIONS, 3);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, "1=0;2=2;4=1;6=0;8=0;10=0;12=0");
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION, "0=0;5=0;10=1;20=0;30=0;60=0;90=0");
    
    // the .php file contains NOSONAR at line 34
    checkNoSonar(componentKey, 33, true);
    checkNoSonar(componentKey, 34, false);
  }

  private void checkNoSonar(String componentKey, int line, boolean expected) throws NoSuchFieldException, IllegalAccessException {
    // retrieve the noSonarFilter, which is private
    Field field = PHPSensor.class.getDeclaredField("noSonarFilter");
    field.setAccessible(true);
    NoSonarFilter noSonarFilter = (NoSonarFilter) field.get(sensor);

    // a filter chain that does nothing
    IssueFilterChain chain = mock(IssueFilterChain.class);
    when(chain.accept(any(FilterableIssue.class))).thenReturn(true);

    // an issue
    FilterableIssue issue = mock(FilterableIssue.class);
    when(issue.line()).thenReturn(line);
    when(issue.componentKey()).thenReturn(componentKey);
    when(issue.ruleKey()).thenReturn(RuleKey.parse(CheckList.REPOSITORY_KEY + ":" + LeftCurlyBraceEndsLineCheck.KEY));

    // test the noSonarFilter
    boolean accepted = noSonarFilter.accept(issue, chain);
    assertThat(accepted).as("response of noSonarFilter.accept for line " + line).isEqualTo(expected);
  }

  @Test
  public void empty_file_should_raise_no_issue() throws Exception {
    analyseSingleFile(context, "empty.php");

    assertThat(context.allIssues()).as("No issue must be raised").hasSize(0);
  }

  @Test
  public void parsing_error_should_raise_an_issue_if_check_rule_is_activated() throws Exception {
    // Add 1 rule, in order to active the rule of parsing errors
    FileLinesContextFactory fileLinesContextFactory = createFileLinesContextFactory();
    String parsingErrorCheckKey = "S2260";
    ActiveRules activeRules = (new ActiveRulesBuilder())
      .create(RuleKey.of(CheckList.REPOSITORY_KEY, parsingErrorCheckKey))
      .setName(parsingErrorCheckKey)
      .activate()
      .build();
    CheckFactory checkFactory = new CheckFactory(activeRules);
    sensor = new PHPSensor(fileSystem, fileLinesContextFactory, checkFactory, new NoSonarFilter(), CUSTOM_RULES);

    analyseSingleFile(context, "parseError.php");

    assertThat(context.allIssues()).as("One issue must be raised").hasSize(1);

    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo("S2260");

    TextRange range = issue.primaryLocation().textRange();
    assertThat(range.start().line()).isEqualTo(2);
    assertThat(range.start().lineOffset()).isEqualTo(0);
    assertThat(range.end().line()).isEqualTo(2);
    assertThat(range.end().lineOffset()).isEqualTo(16);
  }

  @Test
  public void parsing_error_should_raise_no_issue_if_check_rule_is_not_activated() throws Exception {
    analyseSingleFile(context, "parseError.php");

    assertThat(context.allIssues()).as("One issue must be raised").isEmpty();
  }

  private void analyseSingleFile(SensorContext context, String fileName) {
    fileSystem.add(inputFile(fileName));
    sensor.execute(context);
  }

  private DefaultInputFile inputFile(String fileName) {
    DefaultInputFile inputFile = new DefaultInputFile("moduleKey", fileName)
      .setModuleBaseDir(PhpTestUtils.getModuleBaseDir().toPath())
      .setType(Type.MAIN)
      .setLanguage(Php.KEY);
    inputFile.initMetadata(new FileMetadata().readMetadata(inputFile.file(), Charsets.UTF_8));
    return inputFile;
  }

  @Test
  public void progress_report_should_be_stopped() throws Exception {
    PHPAnalyzer phpAnalyzer = new PHPAnalyzer(StandardCharsets.UTF_8, ImmutableList.<PHPCheck>of());
    sensor.analyseFiles(context, phpAnalyzer, ImmutableList.<InputFile>of(), progressReport, new HashMap<File, Integer>());
    verify(progressReport).stop();
  }

  @Test
  public void exception_should_report_file_name() throws Exception {
    PHPCheck check = new ExceptionRaisingCheck(new IllegalStateException());
    analyseFileWithException(check, inputFile("PHPSquidSensor.php"), "PHPSquidSensor.php");
  }

  @Test
  public void cancelled_analysis() throws Exception {
    PHPCheck check = new ExceptionRaisingCheck(new IllegalStateException(new InterruptedException()));
    analyseFileWithException(check, inputFile("PHPSquidSensor.php"), "Analysis cancelled");
  }

  @Test
  public void cancelled_analysis_causing_recognition_exception() throws Exception {
    PHPCheck check = new ExceptionRaisingCheck(new RecognitionException(42, "message", new InterruptedIOException()));
    analyseFileWithException(check, inputFile("PHPSquidSensor.php"), "Analysis cancelled");
  }

  private FileLinesContextFactory createFileLinesContextFactory() {
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
    return fileLinesContextFactory;
  }

  private void analyseFileWithException(PHPCheck check, InputFile inputFile, String expectedMessageSubstring) {
    PHPAnalyzer phpAnalyzer = new PHPAnalyzer(StandardCharsets.UTF_8, ImmutableList.of(check));
    thrown.expect(AnalysisException.class);
    thrown.expectMessage(expectedMessageSubstring);
    try {
      sensor.analyseFiles(context, phpAnalyzer, ImmutableList.of(inputFile), progressReport, new HashMap<File, Integer>());
    } finally {
      verify(progressReport).cancel();
    }
  }

  private final class ExceptionRaisingCheck extends PHPVisitorCheck {

    private final RuntimeException exception;

    public ExceptionRaisingCheck(RuntimeException exception) {
      this.exception = exception;
    }

    @Override
    public void visitCompilationUnit(CompilationUnitTree tree) {
      throw exception;
    }
  }

}
