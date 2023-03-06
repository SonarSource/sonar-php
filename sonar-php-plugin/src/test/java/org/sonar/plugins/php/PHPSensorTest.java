/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.batch.sensor.issue.internal.DefaultNoSonarFilter;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.CheckList;
import org.sonar.php.checks.UncatchableExceptionCheck;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.utils.ReadWriteInMemoryCache;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPCustomRuleRepository;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpInputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.FileHashingUtils.inputFileContentHash;
import static org.sonar.plugins.php.PhpTestUtils.assertMeasure;
import static org.sonar.plugins.php.PhpTestUtils.assertNoMeasure;
import static org.sonar.plugins.php.PhpTestUtils.inputFile;
import static org.sonar.plugins.php.PhpTestUtils.inputFileHashCacheKey;
import static org.sonar.plugins.php.warning.DefaultAnalysisWarningsWrapper.NOOP_ANALYSIS_WARNINGS;

public class PHPSensorTest {

  @org.junit.Rule
  public LogTester logTester = new LogTester();

  private SensorContextTester context = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());

  private CheckFactory checkFactory = new CheckFactory(mock(ActiveRules.class));

  private static final Version VERSION_7_9 = Version.create(7, 9);
  private static final SonarRuntime SONARLINT_RUNTIME = SonarRuntimeImpl.forSonarLint(VERSION_7_9);
  private static final SonarRuntime NOT_SONARLINT_RUNTIME = SonarRuntimeImpl.forSonarQube(VERSION_7_9, SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
  private static final SonarRuntime SONARQUBE_7_9 = SonarRuntimeImpl.forSonarQube(VERSION_7_9, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);

  private static final String PARSE_ERROR_FILE = "parseError.php";
  private static final String ANALYZED_FILE = "PHPSquidSensor.php";
  private static final String REGEX_FILE = "regexIssue.php";
  private static final String TEST_FILE = "Test.php";

  private static final String CUSTOM_REPOSITORY_KEY = "customKey";
  private static final String CUSTOM_RULE_KEY = "key";

  private ReadWriteInMemoryCache previousCache;
  private ReadWriteInMemoryCache nextCache;

  private final Set<File> tempReportFiles = new HashSet<>();

  private Path tmpFolderPath;

  @org.junit.Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  private static final PHPCustomRuleRepository[] CUSTOM_RULES = {new PHPCustomRuleRepository() {
    @Override
    public String repositoryKey() {
      return CUSTOM_REPOSITORY_KEY;
    }

    @Override
    public List<Class<?>> checkClasses() {
      return Collections.singletonList(MyCustomRule.class);
    }
  }};

  @Rule(
    key = CUSTOM_RULE_KEY,
    name = "name",
    description = "desc",
    tags = {"bug"})
  public static class MyCustomRule extends PHPVisitorCheck {
    @RuleProperty(
      key = "customParam",
      description = "Custom parameter",
      defaultValue = "value")
    public String customParam = "value";

    @Override
    public boolean scanWithoutParsing(PhpInputFileContext phpInputFileContext) {
      return false;
    }
  }

  @Before
  public void before() throws IOException {
    tmpFolderPath = tmpFolder.newFolder().toPath();
    resetContext();
    disableCache();
  }

  private void resetContext() {
    context = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());
    context.fileSystem().setWorkDir(tmpFolderPath);
  }

  private void disableCache() {
    context.setCacheEnabled(false);
    context.setCanSkipUnchangedFiles(false);
    previousCache = null;
    nextCache = null;
    context.setPreviousCache(previousCache);
    context.setNextCache(nextCache);
  }

  private void enableCache() {
    context.setCacheEnabled(true);
    context.setCanSkipUnchangedFiles(true);
    previousCache = new ReadWriteInMemoryCache();
    nextCache = new ReadWriteInMemoryCache();
    context.setPreviousCache(previousCache);
    context.setNextCache(nextCache);
  }

  private void setCacheFromPreviousAnalysis() {
    previousCache = nextCache;
    nextCache = new ReadWriteInMemoryCache();
    context.setPreviousCache(previousCache);
    context.setNextCache(nextCache);
  }

  private PHPSensor createSensor() {
    return new PHPSensor(createFileLinesContextFactory(), checkFactory, new DefaultNoSonarFilter(), CUSTOM_RULES);
  }

  private PHPSensor createSensor(PHPCheck check) {
    PHPChecks checks = mock(PHPChecks.class);
    when(checks.all()).thenReturn(Collections.singletonList(check));
    return new PHPSensor(createFileLinesContextFactory(), checks, new DefaultNoSonarFilter(), NOOP_ANALYSIS_WARNINGS);
  }

  @Test
  public void sensor_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    createSensor().describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("PHP sensor");
    assertThat(descriptor.languages()).containsOnly("php");
    assertThat(descriptor.type()).isNull();
  }

  @Test
  public void analyse() {
    String componentKey = "moduleKey:" + ANALYZED_FILE;

    PHPSensor phpSensor = createSensor();
    analyseSingleFile(phpSensor, ANALYZED_FILE);

    assertMeasure(context, componentKey, CoreMetrics.NCLOC, 32);
    assertMeasure(context, componentKey, CoreMetrics.COMMENT_LINES, 7);
    assertMeasure(context, componentKey, CoreMetrics.COGNITIVE_COMPLEXITY, 6);
    assertMeasure(context, componentKey, CoreMetrics.COMPLEXITY, 9);
    assertMeasure(context, componentKey, CoreMetrics.CLASSES, 1);
    assertMeasure(context, componentKey, CoreMetrics.STATEMENTS, 16);
    assertMeasure(context, componentKey, CoreMetrics.FUNCTIONS, 3);
  }

  @Test
  public void test_cpd() {
    String fileName = "cpd.php";
    String componentKey = "moduleKey:" + fileName;

    PHPSensor phpSensor = createSensor();
    analyseSingleFile(phpSensor, fileName);

    List<TokensLine> tokensLines = context.cpdTokens(componentKey);
    assertThat(tokensLines.stream().map(TokensLine::getValue)).containsExactly(
      "require_once$CHARS;",
      "classAextendsB",
      "{",
      "protected$a=$CHARS;",
      "public$b=$NUMBER;",
      "}",
      "echo$CHARS",
      ";"
    );
  }

  @Test
  public void test_no_cpd_on_test_files() {
    String fileName = "cpd.php";
    String componentKey = "moduleKey:" + fileName;

    PHPSensor phpSensor = createSensor();
    InputFile testFile = inputFile(fileName, Type.TEST);
    context.fileSystem().add(testFile);
    phpSensor.execute(context);

    List<TokensLine> tokensLines = context.cpdTokens(componentKey);
    assertThat(tokensLines).isNull();
  }

  @Test
  public void should_read_cpd_from_cache() throws IOException, NoSuchAlgorithmException {
    enableCache();
    String fileName = "cpd.php";
    InputFile inputFile = inputFile(fileName, Type.MAIN, InputFile.Status.SAME);

    PHPSensor phpSensor = createSensor();
    context.fileSystem().add(inputFile);
    phpSensor.execute(context);

    setCacheFromPreviousAnalysis();
    previousCache.write(inputFileHashCacheKey(inputFile), inputFileContentHash(inputFile));

    phpSensor.execute(context);

    assertThat(previousCache.readKeys()).containsExactly(
      "php.contentHashes:moduleKey:cpd.php",
      "php.projectSymbolData.data:moduleKey:cpd.php",
      "php.projectSymbolData.stringTable:moduleKey:cpd.php",
      "php.contentHashes:moduleKey:cpd.php",
      "php.cpd.data:moduleKey:cpd.php",
      "php.cpd.stringTable:moduleKey:cpd.php"
    );
  }

  @Test
  public void hash_exception_when_trying_to_compare_hash() {
    enableCache();
    InputFile inputFile = inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME);
    PHPSensor phpSensor = createSensor();
    context.fileSystem().add(inputFile);

    try (MockedStatic<FileHashingUtils> FileHashingUtilsStaticMock = Mockito.mockStatic(FileHashingUtils.class)) {
      FileHashingUtilsStaticMock.when(() -> FileHashingUtils.inputFileContentHash(any())).thenThrow(new IOException("BOOM!"));
      phpSensor.execute(context);
      assertThat(logTester.logs(LoggerLevel.DEBUG)).contains("Failed to compute content hash for file moduleKey:PHPSquidSensor.php");
    }
  }

  @Test
  public void should_store_cpd_in_cache() {
    enableCache();
    String fileName = "cpd.php";

    PHPSensor phpSensor = createSensor();
    analyseSingleFile(phpSensor, fileName);

    assertThat(nextCache.writeKeys()).containsExactly(
      "php.contentHashes:moduleKey:cpd.php",
      "php.projectSymbolData.data:moduleKey:cpd.php",
      "php.projectSymbolData.stringTable:moduleKey:cpd.php",
      "php.cpd.data:moduleKey:cpd.php",
      "php.cpd.stringTable:moduleKey:cpd.php"
    );
  }

  @Test
  public void empty_file_should_raise_no_issue() {
    analyseSingleFile(createSensorWithParsingErrorCheckActivated(), "empty.php");

    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
  }

  @Test
  public void parsing_error_should_raise_an_issue_if_check_rule_is_activated() {
    analyseSingleFile(createSensorWithParsingErrorCheckActivated(), PARSE_ERROR_FILE);

    assertThat(context.allIssues()).as("One issue must be raised").hasSize(1);

    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo("S2260");

    TextRange range = issue.primaryLocation().textRange();
    assertRange(2, 0, 2, 16, range);
  }

  @Test
  public void parsing_error_should_raise_be_reported_in_sensor_context() {
    analyseSingleFile(createSensor(), PARSE_ERROR_FILE);
    assertThat(context.allAnalysisErrors()).hasSize(1);
  }

  @Test
  public void parsing_error_should_raise_no_issue_if_check_rule_is_not_activated() {
    analyseSingleFile(createSensor(), PARSE_ERROR_FILE);
    assertThat(context.allIssues()).as("One issue must be raised").isEmpty();
  }

  @Test
  public void parsing_error_should_not_fail_the_analysis_even_with_fail_fast() {
    context.setSettings(new MapSettings().setProperty("sonar.internal.analysis.failFast", "true"));
    analyseSingleFile(createSensor(), PARSE_ERROR_FILE);
    assertThat(context.allAnalysisErrors()).hasSize(1);
  }

  private void analyseSingleFile(PHPSensor sensor, String fileName) {
    addInputFiles(fileName);
    sensor.execute(context);
  }

  private void addInputFiles(String... paths) {
    for (String path : paths) {
      context.fileSystem().add(inputFile(path));
    }
  }

  @Test
  public void init_and_terminate_method_called_only_once() {
    PHPCheck check = spy(new PHPVisitorCheck() {});

    addInputFiles(ANALYZED_FILE, "cpd.php", "empty.php");
    createSensor(check).execute(context);

    verify(check, times(1)).init();
    verify(check, times(1)).terminate();
  }

  @Test
  public void exception_should_report_file_name() {
    PHPCheck check = new ExceptionRaisingCheck(new IllegalStateException());
    addInputFiles(ANALYZED_FILE);
    createSensor(check).execute(context);
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("Could not analyse PHPSquidSensor.php");
  }

  @Test
  public void exception_should_fail_analysis_if_configured_so() {
    enableCache();
    RuntimeException exception = new NumberFormatException();
    PHPCheck check = new ExceptionRaisingCheck(exception);
    addInputFiles(ANALYZED_FILE);
    context.setSettings(new MapSettings().setProperty("sonar.internal.analysis.failFast", "true"));
    PHPSensor sensor = createSensor(check);
    assertThatThrownBy(() -> sensor.execute(context))
      .isInstanceOf(IllegalStateException.class)
      .hasCause(exception)
      .hasMessageContaining("PHPSquidSensor.php");
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
    return new PHPSensor(fileLinesContextFactory, new CheckFactory(activeRules), new DefaultNoSonarFilter(), CUSTOM_RULES);
  }

  private static FileLinesContextFactory createFileLinesContextFactory() {
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
    return fileLinesContextFactory;
  }

  @Test
  public void test_issues() {
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
  public void test_regex_issues() {
    // S5855: Regex alternatives should not be redundant
    ActiveRules rules = new ActiveRulesBuilder()
      .addRule(newActiveRule("S5855"))
      .build();

    checkFactory = new CheckFactory(rules);
    analyseSingleFile(createSensor(), REGEX_FILE);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertLocation("Remove or rework this redundant alternative.", 3, 18, 3, 19,   issue.primaryLocation());

    assertThat(issue.flows()).hasSize(1);
    Issue.Flow secondaryFlow = issue.flows().get(0);
    assertThat(secondaryFlow.locations()).hasSize(1);
    assertLocation("Alternative to keep", 3, 13, 3, 17, secondaryFlow.locations().get(0));
  }

  private void assertLocation(String message, int startLine, int startLineOffset, int endLine, int endLineOffset, IssueLocation location) {
    assertThat(location.message()).isEqualTo(message);
    TextRange range = location.textRange();
    assertThat(range).isNotNull();
    assertRange(startLine, startLineOffset, endLine, endLineOffset, range);
  }

  private void assertRange(int startLine, int startLineOffset, int endLine, int endLineOffset, TextRange textRange) {
    assertThat(textRange.start().line())
      .withFailMessage(String.format("Start line is expected to be %s, but get %s.", startLine, textRange.start().line()))
      .isEqualTo(startLine);
    assertThat(textRange.start().lineOffset())
      .withFailMessage(String.format("Start line offset is expected to be %s, but get %s.", startLineOffset, textRange.start().lineOffset()))
      .isEqualTo(startLineOffset);
    assertThat(textRange.end().line())
      .withFailMessage(String.format("End line is expected to be %s, but get %s.", endLine, textRange.end().line()))
      .isEqualTo(endLine);
    assertThat(textRange.end().lineOffset())
      .withFailMessage(String.format("End line offset is expected to be %s, but get %s.", endLineOffset, textRange.end().lineOffset()))
      .isEqualTo(endLineOffset);
  }


  @Test
  public void cross_file_issue() {
    checkFactory = new CheckFactory(new ActiveRulesBuilder().addRule(newActiveRule("S1045")).build());
    addInputFiles("cross-file/A.php", "cross-file/B.php");
    createSensor().execute(context);
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo("S1045");
    assertThat(issue.primaryLocation().inputComponent()).hasToString("cross-file/A.php");
    assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(6);
    assertThat(issue.flows()).extracting(
      f -> f.locations().get(0).inputComponent().toString(),
      f -> f.locations().get(0).textRange().start().line()
    ).containsExactly(
      tuple("cross-file/A.php", 5)
    );
  }

  @Test
  public void should_stop_if_cancel() {
    checkFactory = new CheckFactory(getActiveRules());

    context.setCancelled(true);
    analyseSingleFile(createSensor(), ANALYZED_FILE);

    assertThat(context.allIssues()).as("Should have no issue").isEmpty();
    assertThat(context.measures("moduleKey:PHPSquidSensor.php")).isEmpty();

    context.setCancelled(false);
    analyseSingleFile(createSensor(), ANALYZED_FILE);

    assertThat(context.allIssues()).isNotEmpty();
    assertThat(context.measures("moduleKey:PHPSquidSensor.php")).isNotEmpty();
  }

  private static ActiveRules getActiveRules() {
    // class name check -> PreciseIssue
    NewActiveRule s101 = newActiveRule("S101");
    // inline html in file check -> FileIssue
    NewActiveRule s1997 = newActiveRule("S1997");
    // line size -> LineIssue
    NewActiveRule s103 = newActiveRule("S103");
    // Modifiers order -> PreciseIssue
    NewActiveRule s1124 = newActiveRule("S1124");
    return new ActiveRulesBuilder().addRule(s101).addRule(s1997).addRule(s103).addRule(s1124).build();
  }

  private static NewActiveRule newActiveRule(String ruleKey) {
    return new NewActiveRule.Builder().setRuleKey(RuleKey.of(CheckList.REPOSITORY_KEY, ruleKey)).build();
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
  public void should_disable_unnecessary_features_for_sonarlint() {
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
    assertNoMeasure(context, testFileKey, CoreMetrics.TESTS);

    // no coverage
    assertNoMeasure(context, testFileKey, CoreMetrics.LINES_TO_COVER);

    // metrics are not saved
    assertNoMeasure(context, testFileKey, CoreMetrics.NCLOC);

    // no symbol highlighting
    assertThat(context.referencesForSymbolAt(mainFileKey, 6, 7)).isNull();

    context.setRuntime(NOT_SONARLINT_RUNTIME);
    createSensor().execute(context);

    // cpd tokens exist
    assertThat(context.cpdTokens(mainFileKey)).isNotEmpty();

    // highlighting exists
    assertThat(context.highlightingTypeAt(mainFileKey, 2, 0)).isNotEmpty();

    // tests exist
    assertMeasure(context, testFileKey, CoreMetrics.TESTS, 1);

    // metrics are saved
    assertMeasure(context, mainFileKey, CoreMetrics.NCLOC, 32);

    // symbol highlighting is there
    assertThat(context.referencesForSymbolAt(mainFileKey, 6, 7)).isNotNull();
  }

  @Test
  public void no_measures_for_test_files() {
    checkFactory = new CheckFactory(new ActiveRulesBuilder()
      .addRule(newActiveRule("S2187"))
      .build());

    InputFile testFile = inputFile(TEST_FILE, Type.TEST);

    String testFileKey = testFile.key();

    context.fileSystem().add(testFile);
    context.setRuntime(NOT_SONARLINT_RUNTIME);

    createSensor().execute(context);

    assertThat(context.allIssues()).isNotEmpty();
    assertThat(context.measure(testFileKey, CoreMetrics.NCLOC)).isNull();

  }

  @Test
  public void should_use_multi_path_coverage() throws IOException {
    context.setRuntime(SONARQUBE_7_9);

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

    assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(1);
    String resourcesFolder = FilenameUtils.separatorsToSystem("/src/test/resources");
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0))
      .startsWith("An error occurred when reading report file")
      .contains(resourcesFolder + "', nothing will be imported from this report.");
  }

  @Test
  public void should_log_message_when_no_coverage_and_test_property() {
    context.setSettings(new MapSettings());

    context.setRuntime(SONARQUBE_7_9);
    createSensor().execute(context);
    assertThat(logTester.logs()).contains(
      "No PHPUnit tests reports provided (see '" + PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY + "' property)",
      "No PHPUnit coverage reports provided (see '" + PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY + "' property)");

    logTester.clear();
  }

  @Test
  public void should_disable_rules_for_sonarlint() {
    checkFactory = new CheckFactory(new ActiveRulesBuilder()
      .addRule(newActiveRule(UncatchableExceptionCheck.KEY))
      .build());

    // SonarLint Runtime
    context.setRuntime(SONARLINT_RUNTIME);
    analyseSingleFile(createSensor(), "disable_rules_for_sonarlint.php");
    assertThat(context.allIssues()).isEmpty();

    // SonarQube Runtime
    context.setRuntime(NOT_SONARLINT_RUNTIME);
    analyseSingleFile(createSensor(), "disable_rules_for_sonarlint.php");
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void should_use_test_file_checks() {
    TestFileCheck check = new TestFileCheck();
    InputFile testFile = inputFile(ANALYZED_FILE, Type.TEST);
    context.fileSystem().add(testFile);
    createSensor(check).execute(context);
    assertThat(check.wasTriggered).isTrue();
  }

  @Test
  public void should_not_analyze_unchanged_file_if_setting_is_enabled() {
    checkFactory = new CheckFactory(getActiveRules());
    InputFile inputFile = inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME);
    analyzeBaseCommit(inputFile);
    createSensor().execute(context);
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  public void should_analyze_file_without_status_if_setting_is_enabled() {
    enableCache();
    checkFactory = new CheckFactory(getActiveRules());
    context.fileSystem().add(inputFile(ANALYZED_FILE, Type.MAIN, null));
    context.setCanSkipUnchangedFiles(true);
    createSensor().execute(context);
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  public void should_analyze_changed_file_if_setting_is_enabled() {
    checkFactory = new CheckFactory(getActiveRules());
    context.fileSystem().add(inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.CHANGED));
    context.setCanSkipUnchangedFiles(true);
    createSensor().execute(context);
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  public void should_not_analyze_unchanged_file_if_enabled_by_property() {
    checkFactory = new CheckFactory(getActiveRules());

    InputFile inputFile = inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME);
    analyzeBaseCommit(inputFile);
    context.fileSystem().add(inputFile);

    createSensor().execute(context);
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  public void should_not_analyze_unchanged_file_if_disabled_by_property() {
    checkFactory = new CheckFactory(getActiveRules());
    context.fileSystem().add(inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME));
    context.setSettings(new MapSettings().setProperty("sonar.php.skipUnchanged", "false"));
    createSensor().execute(context);
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  public void should_not_raise_issue_on_same_file_when_no_check_requires_parsing() {
    ActiveRules rules = new ActiveRulesBuilder()
      .addRule(new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of(CheckList.REPOSITORY_KEY, "S101"))
        .build())
      .build();
    checkFactory = new CheckFactory(rules);

    InputFile inputFile = inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME);
    analyzeBaseCommit(inputFile);

    context.fileSystem().add(inputFile);
    createSensor().execute(context);
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  public void should_raise_issue_on_same_file_when_one_check_requires_parsing() throws IOException, NoSuchAlgorithmException {
    enableCache();

    ActiveRules rules = new ActiveRulesBuilder()
      .addRule(new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of(CheckList.REPOSITORY_KEY, "S101"))
        .build())
      .addRule(new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of(CUSTOM_REPOSITORY_KEY, CUSTOM_RULE_KEY))
        .build())
      .build();
    checkFactory = new CheckFactory(rules);

    InputFile inputFile = inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME);

    context.fileSystem().add(inputFile);
    context.setSettings(new MapSettings().setProperty("sonar.php.skipUnchanged", "true"));
    previousCache.write(inputFileHashCacheKey(inputFile), inputFileContentHash(inputFile));

    createSensor().execute(context);
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  public void should_analyze_unchanged_file_in_sonarlint_context() {
    checkFactory = new CheckFactory(getActiveRules());
    context.fileSystem().add(inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME));
    context.setSettings(new MapSettings().setProperty("sonar.php.skipUnchanged", "true"));
    context.setRuntime(SONARLINT_RUNTIME);
    createSensor().execute(context);
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  public void create_sensor_for_sonar_lint() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    PHPSensor phpSensor = new PHPSensor(createFileLinesContextFactory(), checkFactory, new DefaultNoSonarFilter());
    phpSensor.describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("PHP sensor");
    assertThat(descriptor.languages()).containsOnly("php");
    assertThat(descriptor.type()).isNull();
  }

  @Test
  public void sensor_for_sonar_lint_doesnt_provide_metrics() {
    String componentKey = "moduleKey:" + ANALYZED_FILE;
    PHPSensor phpSensor = new PHPSensor(createFileLinesContextFactory(), checkFactory, new DefaultNoSonarFilter());
    context.setRuntime(SONARLINT_RUNTIME);

    analyseSingleFile(phpSensor, ANALYZED_FILE);

    assertNoMeasure(context, componentKey, CoreMetrics.NCLOC);
    assertNoMeasure(context, componentKey, CoreMetrics.COMMENT_LINES);
    assertNoMeasure(context, componentKey, CoreMetrics.COGNITIVE_COMPLEXITY);
    assertNoMeasure(context, componentKey, CoreMetrics.COMPLEXITY);
    assertNoMeasure(context, componentKey, CoreMetrics.CLASSES);
    assertNoMeasure(context, componentKey, CoreMetrics.STATEMENTS);
    assertNoMeasure(context, componentKey, CoreMetrics.FUNCTIONS);
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
  private void createReportWithAbsolutePath(String generatedReportRelativePath, String relativeReportPath, InputFile inputFile) throws IOException {
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

  private void analyzeBaseCommit(InputFile inputFile) {
    enableCache();
    context.fileSystem().add(inputFile);
    context.setCanSkipUnchangedFiles(true);
    createSensor().execute(context);
    resetContext();
    context.setCacheEnabled(true);
    context.setCanSkipUnchangedFiles(true);
    setCacheFromPreviousAnalysis();
  }

  private static class ExceptionRaisingCheck extends PHPVisitorCheck {

    private final RuntimeException exception;

    public ExceptionRaisingCheck(RuntimeException exception) {
      this.exception = exception;
    }

    @Override
    public void visitCompilationUnit(CompilationUnitTree tree) {
      throw exception;
    }
  }

  private static class TestFileCheck extends PhpUnitCheck {
    protected boolean wasTriggered = false;

    @Override
    public void visitCompilationUnit(CompilationUnitTree tree) {
      wasTriggered = true;
    }
  }
}
