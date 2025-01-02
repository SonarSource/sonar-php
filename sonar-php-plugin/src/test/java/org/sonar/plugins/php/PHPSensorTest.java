/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.event.Level;
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
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.api.utils.Version;
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
import org.sonar.plugins.php.reports.phpunit.PhpUnitSensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.tuple;
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

class PHPSensorTest {

  @RegisterExtension
  public final LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private SensorContextTester context = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());

  private CheckFactory checkFactory = new CheckFactory(mock(ActiveRules.class));

  private static final Version VERSION_7_9 = Version.create(7, 9);
  private static final SonarRuntime SONARLINT_RUNTIME = SonarRuntimeImpl.forSonarLint(VERSION_7_9);
  private static final SonarRuntime NOT_SONARLINT_RUNTIME = SonarRuntimeImpl.forSonarQube(VERSION_7_9, SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
  private static final String PARSE_ERROR_FILE = "parseError.php";
  private static final String ANALYZED_FILE = "PHPSquidSensor.php";
  private static final String REGEX_FILE = "regexIssue.php";
  private static final String TEST_FILE = "Test.php";

  private static final String CUSTOM_REPOSITORY_KEY = "customKey";
  private static final String CUSTOM_RULE_KEY = "key";

  private ReadWriteInMemoryCache previousCache;
  private ReadWriteInMemoryCache nextCache;

  private Path tmpFolderPath;

  @TempDir
  public File tmpFolder;

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

  @BeforeEach
  public void before() {
    tmpFolderPath = tmpFolder.toPath();
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
    return new PHPSensor(createFileLinesContextFactory(), checks, new DefaultNoSonarFilter());
  }

  @Test
  void sensorDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    createSensor().describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("PHP sensor");
    assertThat(descriptor.languages()).containsOnly("php");
    assertThat(descriptor.type()).isNull();
  }

  @Test
  void analyse() {
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
  void cpdTokenShouldBeValidForSimpleFile() {
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
      ";");
  }

  @Test
  void cpdTokenShouldBeValidForReadonlyPropertyPromotion() {
    String fileName = "readonlyPropertyPromotion.php";
    String componentKey = "moduleKey:" + fileName;

    PHPSensor phpSensor = createSensor();
    analyseSingleFile(phpSensor, fileName);

    List<TokensLine> tokensLines = context.cpdTokens(componentKey);
    assertThat(tokensLines.stream().map(TokensLine::getValue)).containsExactly(
      "namespaceMyCoolNamespace;",
      "classDomain",
      "{",
      "publicfunction__construct(readonlyprivatestring$prop){}",
      "}");
  }

  @Test
  void testNoCpdOnTestFiles() {
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
  void shouldReadCpdFromCache() {
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
      "php.cpd.stringTable:moduleKey:cpd.php");
  }

  @Test
  void hashExceptionWhenTryingToCompareHash() {
    enableCache();
    InputFile inputFile = inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME);
    PHPSensor phpSensor = createSensor();
    context.fileSystem().add(inputFile);

    try (MockedStatic<FileHashingUtils> FileHashingUtilsStaticMock = Mockito.mockStatic(FileHashingUtils.class)) {
      FileHashingUtilsStaticMock.when(() -> FileHashingUtils.inputFileContentHash(any())).thenThrow(new IllegalStateException("BOOM!"));
      phpSensor.execute(context);
      assertThat(logTester.logs(Level.DEBUG)).contains("Failed to compute content hash for file moduleKey:PHPSquidSensor.php");
    }
  }

  @Test
  void shouldStoreCpdInCache() {
    enableCache();
    String fileName = "cpd.php";

    PHPSensor phpSensor = createSensor();
    analyseSingleFile(phpSensor, fileName);

    assertThat(nextCache.writeKeys()).containsExactly(
      "php.contentHashes:moduleKey:cpd.php",
      "php.projectSymbolData.data:moduleKey:cpd.php",
      "php.projectSymbolData.stringTable:moduleKey:cpd.php",
      "php.cpd.data:moduleKey:cpd.php",
      "php.cpd.stringTable:moduleKey:cpd.php");
  }

  @Test
  void emptyFileShouldRaiseNoIssue() {
    analyseSingleFile(createSensorWithParsingErrorCheckActivated(), "empty.php");

    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
  }

  @Test
  void parsingErrorShouldRaiseAnIssueIfCheckRuleIsActivated() {
    analyseSingleFile(createSensorWithParsingErrorCheckActivated(), PARSE_ERROR_FILE);

    assertThat(context.allIssues()).as("One issue must be raised").hasSize(1);

    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo("S2260");

    TextRange range = issue.primaryLocation().textRange();
    assertRange(2, 0, 2, 16, range);
  }

  @Test
  void parsingErrorShouldRaiseBeReportedInSensoCcontext() {
    analyseSingleFile(createSensor(), PARSE_ERROR_FILE);
    assertThat(context.allAnalysisErrors()).hasSize(1);
  }

  @Test
  void parsingErrorShouldRaiseNoIssueIfCheckRuleIsNotActivated() {
    analyseSingleFile(createSensor(), PARSE_ERROR_FILE);
    assertThat(context.allIssues()).as("One issue must be raised").isEmpty();
  }

  @Test
  void parsingErrorShouldNotFailTheAnalysisEvenWithFailFast() {
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
  void initAndTerminateMethodCalledOnlyOnce() {
    PHPCheck check = spy(new PHPVisitorCheck() {
    });

    addInputFiles(ANALYZED_FILE, "cpd.php", "empty.php");
    createSensor(check).execute(context);

    verify(check, times(1)).init();
    verify(check, times(1)).terminate();
  }

  @Test
  void exceptionShouldReportFileName() {
    PHPCheck check = new ExceptionRaisingCheck(new IllegalStateException());
    addInputFiles(ANALYZED_FILE);
    createSensor(check).execute(context);
    assertThat(logTester.logs(Level.ERROR)).contains("Could not analyse PHPSquidSensor.php");
  }

  @Test
  void exceptionShouldFailAnalysisIfConfiguredSo() {
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
  void testIssues() {
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
  void testSuppressWarningsIssues() {
    checkFactory = new CheckFactory(getActiveRules());
    analyseSingleFile(createSensor(), "suppressWarnings.php");
    Collection<Issue> issuesWithSuppressedWarnings = context.allIssues();

    assertThat(issuesWithSuppressedWarnings).extracting("ruleKey.rule", "primaryLocation.textRange.start.line").containsOnly(
      tuple("S103", 8),
      tuple("S1124", 8));
  }

  @Test
  void testRegexIssues() {
    // S5855: Regex alternatives should not be redundant
    ActiveRules rules = new ActiveRulesBuilder()
      .addRule(newActiveRule("S5855"))
      .build();

    checkFactory = new CheckFactory(rules);
    analyseSingleFile(createSensor(), REGEX_FILE);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertLocation("Remove or rework this redundant alternative.", 3, 18, 3, 19, issue.primaryLocation());

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
  void crossFileIssue() {
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
      f -> f.locations().get(0).textRange().start().line()).containsExactly(
        tuple("cross-file/A.php", 5));
  }

  @Test
  void shouldStopIfCancel() {
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
  void testFileWithBom() {
    try {
      analyseSingleFile(createSensor(), "fileWithBom.php");
    } catch (Exception e) {
      fail("Should never happen - bom should be handled correctly");
    }
  }

  @Test
  void shouldDisableUnnecessaryFeaturesForSonarlint() {
    context.settings().setProperty(PhpUnitSensor.PHPUNIT_TESTS_REPORT_PATH_KEY, PhpTestUtils.PHPUNIT_REPORT_NAME);
    context.settings().setProperty(PhpUnitSensor.PHPUNIT_COVERAGE_REPORT_PATHS_KEY, PhpTestUtils.PHPUNIT_COVERAGE_REPORT);
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
    assertNoMeasure(context, testFileKey, CoreMetrics.TESTS);

    // metrics are saved
    assertMeasure(context, mainFileKey, CoreMetrics.NCLOC, 32);

    // symbol highlighting is there
    assertThat(context.referencesForSymbolAt(mainFileKey, 6, 7)).isNotNull();
  }

  @Test
  void noMeasuresForTestFiles() {
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
  void shouldDisableRulesForSonarlint() {
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
  void shouldUseTestFileChecks() {
    TestFileCheck check = new TestFileCheck();
    InputFile testFile = inputFile(ANALYZED_FILE, Type.TEST);
    context.fileSystem().add(testFile);
    createSensor(check).execute(context);
    assertThat(check.wasTriggered).isTrue();
  }

  @Test
  void shouldNotAnalyzeUnchangedFileIfSettingIsEnabled() {
    checkFactory = new CheckFactory(getActiveRules());
    InputFile inputFile = inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME);
    analyzeBaseCommit(inputFile);
    createSensor().execute(context);
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void shouldAnalyzeFileWithoutStatusIfSettingIsEnabled() {
    enableCache();
    checkFactory = new CheckFactory(getActiveRules());
    context.fileSystem().add(inputFile(ANALYZED_FILE, Type.MAIN, null));
    context.setCanSkipUnchangedFiles(true);
    createSensor().execute(context);
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  void shouldAnalyzeChangedFileIfSettingIsEnabled() {
    checkFactory = new CheckFactory(getActiveRules());
    context.fileSystem().add(inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.CHANGED));
    context.setCanSkipUnchangedFiles(true);
    createSensor().execute(context);
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  void shouldNotAnalyzeUnchangedFileIfEnabledByProperty() {
    checkFactory = new CheckFactory(getActiveRules());

    InputFile inputFile = inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME);
    analyzeBaseCommit(inputFile);
    context.fileSystem().add(inputFile);

    createSensor().execute(context);
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void shouldNotAnalyzeUnchangedFileIfDisabledByProperty() {
    checkFactory = new CheckFactory(getActiveRules());
    context.fileSystem().add(inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME));
    context.setSettings(new MapSettings().setProperty("sonar.php.skipUnchanged", "false"));
    createSensor().execute(context);
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  void shouldNotRaiseIssueOnSameFileWhenNoCheckRequiresParsing() {
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
  void shouldRaiseIssueOnSameFileWhenOneCheckRequiresParsing() {
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
  void shouldAnalyzeUnchangedFileInSonarlintContext() {
    checkFactory = new CheckFactory(getActiveRules());
    context.fileSystem().add(inputFile(ANALYZED_FILE, Type.MAIN, InputFile.Status.SAME));
    context.setSettings(new MapSettings().setProperty("sonar.php.skipUnchanged", "true"));
    context.setRuntime(SONARLINT_RUNTIME);
    createSensor().execute(context);
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  void createSensorForSonarLint() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    PHPSensor phpSensor = new PHPSensor(createFileLinesContextFactory(), checkFactory, new DefaultNoSonarFilter());
    phpSensor.describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("PHP sensor");
    assertThat(descriptor.languages()).containsOnly("php");
    assertThat(descriptor.type()).isNull();
  }

  @Test
  void sensorForSonarLintDoesntProvideMetrics() {
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
