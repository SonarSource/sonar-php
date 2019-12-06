/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
import com.sonar.sslr.api.RecognitionException;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.cpd.internal.TokensLine;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.PHPAnalyzer;
import org.sonar.php.checks.CheckList;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPCustomRuleRepository;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.ProgressReport;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PHPSensorTest {

  @org.junit.Rule
  public LogTester logTester = new LogTester();

  private ProgressReport progressReport = mock(ProgressReport.class);

  private final SensorContextTester context = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());

  private CheckFactory checkFactory = new CheckFactory(mock(ActiveRules.class));

  private static final Version SONARLINT_DETECTABLE_VERSION = Version.create(6, 7);
  private static final SonarRuntime SONARLINT_RUNTIME = SonarRuntimeImpl.forSonarLint(SONARLINT_DETECTABLE_VERSION);
  private static final SonarRuntime NOT_SONARLINT_RUNTIME = SonarRuntimeImpl.forSonarQube(SONARLINT_DETECTABLE_VERSION, SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
  private static final SonarRuntime SONARQUBE_6_7 = SonarRuntimeImpl.forSonarQube(Version.create(6, 7), SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);

  private static final String PARSE_ERROR_FILE = "parseError.php";
  private static final String ANALYZED_FILE = "PHPSquidSensor.php";

  private Set<File> tempReportFiles = new HashSet<>();

  @org.junit.Rule
  public final ExpectedException thrown = ExpectedException.none();

  @org.junit.Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  private static final PHPCustomRuleRepository[] CUSTOM_RULES = {new PHPCustomRuleRepository() {
    @Override
    public String repositoryKey() {
      return "customKey";
    }

    @Override
    public ImmutableList<Class> checkClasses() {
      return ImmutableList.of(MyCustomRule.class);
    }
  }};

  @Rule(
    key = "key",
    name = "name",
    description = "desc",
    tags = {"bug"})
  public static class MyCustomRule extends PHPVisitorCheck {
    @RuleProperty(
      key = "customParam",
      description = "Custom parameter",
      defaultValue = "value")
    public String customParam = "value";
  }

  @Before
  public void before() throws IOException {
    context.fileSystem().setWorkDir(tmpFolder.newFolder().toPath());
  }

  private PHPSensor createSensor() {
    return new PHPSensor(createFileLinesContextFactory(), checkFactory, new NoSonarFilter(), CUSTOM_RULES);
  }

  @Test
  public void sensor_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    createSensor().describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("PHP sensor");
    assertThat(descriptor.languages()).containsOnly("php");
    assertThat(descriptor.type()).isEqualTo(Type.MAIN);
  }

  @Test
  public void analyse() throws NoSuchFieldException, IllegalAccessException {
    String componentKey = "moduleKey:" + ANALYZED_FILE;

    PHPSensor phpSensor = createSensor();
    analyseSingleFile(phpSensor, ANALYZED_FILE);

    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.NCLOC, 32);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.COMMENT_LINES, 7);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.COGNITIVE_COMPLEXITY, 6);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.COMPLEXITY, 9);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.CLASSES, 1);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.STATEMENTS, 16);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.FUNCTIONS, 3);

  }

  @Test
  public void test_cpd() throws NoSuchFieldException, IllegalAccessException {
    String fileName = "cpd.php";
    String componentKey = "moduleKey:" + fileName;

    PHPSensor phpSensor = createSensor();
    analyseSingleFile(phpSensor, fileName);

    List<TokensLine> tokensLines = context.cpdTokens(componentKey);
    assertThat(tokensLines)
      .isNotNull()
      .hasSize(8);
    assertThat(tokensLines.get(0).getValue()).isEqualTo("require_once$CHARS;");
    assertThat(tokensLines.get(1).getValue()).isEqualTo("classAextendsB");
    assertThat(tokensLines.get(2).getValue()).isEqualTo("{");
    assertThat(tokensLines.get(3).getValue()).isEqualTo("protected$a=$CHARS;");
    assertThat(tokensLines.get(4).getValue()).isEqualTo("public$b=$NUMBER;");
    assertThat(tokensLines.get(5).getValue()).isEqualTo("}");
    assertThat(tokensLines.get(6).getValue()).isEqualTo("echo$CHARS");
    assertThat(tokensLines.get(7).getValue()).isEqualTo(";");
  }

  @Test
  public void empty_file_should_raise_no_issue() throws Exception {
    analyseSingleFile(createSensorWithParsingErrorCheckActivated(), "empty.php");

    assertThat(context.allIssues()).as("No issue must be raised").hasSize(0);
  }

  @Test
  public void parsing_error_should_raise_an_issue_if_check_rule_is_activated() throws Exception {
    analyseSingleFile(createSensorWithParsingErrorCheckActivated(), PARSE_ERROR_FILE);

    assertThat(context.allIssues()).as("One issue must be raised").hasSize(1);

    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo("S2260");

    TextRange range = issue.primaryLocation().textRange();
    assertThat(range).isNotNull();
    assertThat(range.start().line()).isEqualTo(2);
    assertThat(range.start().lineOffset()).isEqualTo(0);
    assertThat(range.end().line()).isEqualTo(2);
    assertThat(range.end().lineOffset()).isEqualTo(16);
  }

  @Test
  public void parsing_error_should_raise_be_reported_in_sensor_context() throws Exception {
    analyseSingleFile(createSensor(), PARSE_ERROR_FILE);
    assertThat(context.allAnalysisErrors()).hasSize(1);
  }

  @Test
  public void parsing_error_should_raise_no_issue_if_check_rule_is_not_activated() throws Exception {
    analyseSingleFile(createSensor(), PARSE_ERROR_FILE);
    assertThat(context.allIssues()).as("One issue must be raised").isEmpty();
  }

  private void analyseSingleFile(PHPSensor sensor, String fileName) {
    context.fileSystem().add(inputFile(fileName));
    sensor.execute(context);
  }

  private static DefaultInputFile inputFile(String fileName) {
    try {
      return TestInputFileBuilder.create("moduleKey", fileName)
        .setModuleBaseDir(PhpTestUtils.getModuleBaseDir().toPath())
        .setType(Type.MAIN)
        .setCharset(Charset.defaultCharset())
        .setLanguage(Php.KEY)
        .initMetadata(new String(java.nio.file.Files.readAllBytes(new File("src/test/resources/" + fileName).toPath()), StandardCharsets.UTF_8)).build();
    } catch (IOException e) {
      throw new IllegalStateException("File not found", e);
    }

  }

  @Test
  public void progress_report_should_be_stopped() throws Exception {
    PHPAnalyzer phpAnalyzer = new PHPAnalyzer(ImmutableList.of(), null);
    createSensor().analyseFiles(context, phpAnalyzer, Collections.emptyList(), progressReport);
    verify(progressReport).stop();
  }

  @Test
  public void init_and_terminate_method_called_only_once() throws Exception {
    PHPCheck check = spy(new PHPVisitorCheck() {});
    PHPAnalyzer phpAnalyzer = new PHPAnalyzer(ImmutableList.of(check), null);
    List<InputFile> inputFiles = asList(inputFile(ANALYZED_FILE), inputFile("cpd.php"), inputFile("empty.php"));
    createSensor().analyseFiles(context, phpAnalyzer, inputFiles, progressReport);

    verify(check, times(1)).init();
    verify(check, times(1)).terminate();
  }

  @Test
  public void exception_should_report_file_name() throws Exception {
    PHPCheck check = new ExceptionRaisingCheck(new IllegalStateException());
    PHPAnalyzer phpAnalyzer = new PHPAnalyzer(ImmutableList.of(check), null);
    DefaultInputFile defaultInputFile = inputFile(ANALYZED_FILE);
    createSensor().analyseFiles(context, phpAnalyzer, ImmutableList.of(defaultInputFile), progressReport);
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("Could not analyse PHPSquidSensor.php");
  }

  @Test
  public void cancelled_analysis() throws Exception {
    PHPCheck check = new ExceptionRaisingCheck(new IllegalStateException(new InterruptedException()));
    analyseFileWithException(check, inputFile(ANALYZED_FILE), "Analysis cancelled");
  }

  @Test
  public void cancelled_analysis_causing_recognition_exception() throws Exception {
    PHPCheck check = new ExceptionRaisingCheck(new RecognitionException(42, "message", new InterruptedIOException()));
    analyseFileWithException(check, inputFile(ANALYZED_FILE), "Analysis cancelled");
  }

  /**
   * Same as method <code>createSensor</code>, with one rule activated (the rule for parsing errors).
   */
  private static PHPSensor createSensorWithParsingErrorCheckActivated() {
    FileLinesContextFactory fileLinesContextFactory = createFileLinesContextFactory();

    String parsingErrorCheckKey = "S2260";
    NewActiveRule activeRule = new NewActiveRule.Builder()
      .setRuleKey(RuleKey.of(CheckList.REPOSITORY_KEY, parsingErrorCheckKey))
      .setName(parsingErrorCheckKey)
      .build();
    ActiveRules activeRules = new ActiveRulesBuilder()
      .addRule(activeRule)
      .build();
    return new PHPSensor(fileLinesContextFactory, new CheckFactory(activeRules), new NoSonarFilter(), CUSTOM_RULES);
  }

  private static FileLinesContextFactory createFileLinesContextFactory() {
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
    return fileLinesContextFactory;
  }

  @Test
  public void test_issues() throws Exception {
    checkFactory = new CheckFactory(getActiveRules());
    analyseSingleFile(createSensor(), ANALYZED_FILE);

    Collection<Issue> issues = context.allIssues();

    assertThat(issues).extracting("ruleKey.rule", "primaryLocation.textRange.start.line").containsOnly(
      tuple("S101", 6),
      tuple("S1997", null),
      tuple("S103", 22),
      tuple("S1124", 22));
  }

  @Test
  public void should_stop_if_cancel() throws Exception {
    checkFactory = new CheckFactory(getActiveRules());

    context.setCancelled(true);
    analyseSingleFile(createSensor(), ANALYZED_FILE);

    assertThat(context.allIssues()).as("Should have no issue").isEmpty();
    assertThat(context.measures("moduleKey:PHPSquidSensor.php")).isEmpty();

    context.setCancelled(false);
    analyseSingleFile(createSensor(), ANALYZED_FILE);

    assertThat(context.allIssues()).as("Should have no issue").isNotEmpty();
    assertThat(context.measures("moduleKey:PHPSquidSensor.php")).isNotEmpty();
  }

  private static ActiveRules getActiveRules() {
    // class name check -> PreciseIssue
    NewActiveRule s101 = new NewActiveRule.Builder().setRuleKey(RuleKey.of(CheckList.REPOSITORY_KEY, "S101")).build();
    // inline html in file check -> FileIssue
    NewActiveRule s1997 = new NewActiveRule.Builder().setRuleKey(RuleKey.of(CheckList.REPOSITORY_KEY, "S1997")).build();
    // line size -> LineIssue
    NewActiveRule s103 = new NewActiveRule.Builder().setRuleKey(RuleKey.of(CheckList.REPOSITORY_KEY, "S103")).build();
    // Modifiers order -> PreciseIssue
    NewActiveRule s1124 = new NewActiveRule.Builder().setRuleKey(RuleKey.of(CheckList.REPOSITORY_KEY, "S1124")).build();
    return new ActiveRulesBuilder().addRule(s101).addRule(s1997).addRule(s103).addRule(s1124).build();
  }

  @Test
  public void test_file_with_bom() {
    try {
      analyseSingleFile(createSensor(), "fileWithBom.php");
    } catch (Exception e) {
      fail("Should never happen - bom should be handled correctly");
    }
  }

  @Test
  public void should_disable_unnecessary_features_for_sonarlint() throws Exception {
    context.settings().setProperty(PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY, PhpTestUtils.PHPUNIT_REPORT_NAME);
    context.settings().setProperty(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY, PhpTestUtils.PHPUNIT_COVERAGE_REPORT);
    DefaultInputFile inputFile = inputFile(ANALYZED_FILE);

    DefaultInputFile testFile = TestInputFileBuilder.create("moduleKey", "src/AppTest.php")
      .setModuleBaseDir(context.fileSystem().baseDirPath())
      .setType(InputFile.Type.TEST)
      .setLanguage(Php.KEY).build();

    String testFileKey = testFile.key();
    String mainFileKey = inputFile.key();

    context.fileSystem().add(inputFile);
    context.fileSystem().add(testFile);

    context.setRuntime(SONARLINT_RUNTIME);
    createSensor().execute(context);

    // no cpd tokens
    assertThat(context.cpdTokens(mainFileKey)).isNull();

    // no highlighting
    assertThat(context.highlightingTypeAt(mainFileKey, 1, 0)).isEmpty();

    // no tests
    assertThat(context.measure(testFileKey, CoreMetrics.TESTS)).isNull();

    // no coverage
    assertThat(context.measure(testFileKey, CoreMetrics.LINES_TO_COVER)).isNull();

    // metrics are not saved
    assertThat(context.measure(mainFileKey, CoreMetrics.NCLOC)).isNull();

    // no symbol highlighting
    assertThat(context.referencesForSymbolAt(mainFileKey, 6, 7)).isNull();

    context.setRuntime(NOT_SONARLINT_RUNTIME);
    createSensor().execute(context);

    // cpd tokens exist
    assertThat(context.cpdTokens(mainFileKey)).isNotEmpty();

    // highlighting exists
    assertThat(context.highlightingTypeAt(mainFileKey, 2, 0)).isNotEmpty();

    // tests exist
    assertThat(context.measure(testFileKey, CoreMetrics.TESTS)).isNotNull();

    // metrics are saved
    assertThat(context.measure(mainFileKey, CoreMetrics.NCLOC)).isNotNull();

    // symbol highlighting is there
    assertThat(context.referencesForSymbolAt(mainFileKey, 6, 7)).isNotNull();
  }

  @Test
  public void should_use_multi_path_coverage() throws Exception {
    context.setRuntime(SONARQUBE_6_7);

    context.settings().setProperty(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY,
      String.join(",", PhpTestUtils.GENERATED_UT_COVERAGE_REPORT_RELATIVE_PATH, PhpTestUtils.GENERATED_IT_COVERAGE_REPORT_RELATIVE_PATH,
        // should not fail with empty path, it should be ignored
        " ",
        PhpTestUtils.GENERATED_OVERALL_COVERAGE_REPORT_RELATIVE_PATH));

    DefaultInputFile inputFile = inputFile("src/App.php");

    createReportWithAbsolutePath(PhpTestUtils.GENERATED_UT_COVERAGE_REPORT_RELATIVE_PATH, PhpTestUtils.UT_COVERAGE_REPORT_RELATIVE_PATH, inputFile);
    createReportWithAbsolutePath(PhpTestUtils.GENERATED_IT_COVERAGE_REPORT_RELATIVE_PATH, PhpTestUtils.IT_COVERAGE_REPORT_RELATIVE_PATH, inputFile);
    createReportWithAbsolutePath(PhpTestUtils.GENERATED_OVERALL_COVERAGE_REPORT_RELATIVE_PATH, PhpTestUtils.OVERALL_COVERAGE_REPORT_RELATIVE_PATH, inputFile);

    String mainFileKey = inputFile.key();
    context.fileSystem().add(inputFile);

    createSensor().execute(context);

    assertThat(context.lineHits(mainFileKey, 3)).isEqualTo(3);
    assertThat(context.lineHits(mainFileKey, 6)).isEqualTo(2);
    assertThat(context.lineHits(mainFileKey, 7)).isEqualTo(1);
  }

  @Test
  public void should_log_message_when_no_coverage_and_test_property() throws Exception {
    context.setSettings(new MapSettings());

    context.setRuntime(SONARQUBE_6_7);
    createSensor().execute(context);
    assertThat(logTester.logs()).contains(
      "No PHPUnit test report provided (see '" + PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY + "' property)",
      "No PHPUnit coverage reports provided (see '" + PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY + "' property)");

    logTester.clear();
  }

  @After
  public void tearDown() {
    tempReportFiles.forEach(File::delete);
  }

  /**
   * Creates a file name with absolute path in coverage report.
   *
   * This hack allow to have this unit test, as only absolute path
   * in report is supported.
   * */
  private void createReportWithAbsolutePath(String generatedReportRelativePath, String relativeReportPath, InputFile inputFile) throws Exception {
    File tempReport = new File(context.fileSystem().baseDir(), generatedReportRelativePath);
    if (tempReport.createNewFile()) {
      File originalReport = new File(context.fileSystem().baseDir(), relativeReportPath);

      String content = Files.readLines(originalReport, StandardCharsets.UTF_8)
        .stream()
        .collect(Collectors.joining("\n"))
        .replace(inputFile.relativePath(), inputFile.absolutePath());

      Files.asCharSink(tempReport, StandardCharsets.UTF_8).write(content);

      tempReportFiles.add(tempReport);
    }
  }

  private void analyseFileWithException(PHPCheck check, InputFile inputFile, String expectedMessageSubstring) {
    PHPAnalyzer phpAnalyzer = new PHPAnalyzer(ImmutableList.of(check), null);
    thrown.expect(AnalysisException.class);
    thrown.expectMessage(expectedMessageSubstring);
    try {
      createSensor().analyseFiles(context, phpAnalyzer, Collections.singletonList(inputFile), progressReport);
    } finally {
      verify(progressReport).cancel();
    }
  }

  private static final class ExceptionRaisingCheck extends PHPVisitorCheck {

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
