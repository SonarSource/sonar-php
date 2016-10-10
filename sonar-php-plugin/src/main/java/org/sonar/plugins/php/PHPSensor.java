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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.RecognitionException;
import java.io.File;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.php.PHPAnalyzer;
import org.sonar.php.checks.CheckList;
import org.sonar.php.checks.ParsingErrorCheck;
import org.sonar.php.metrics.FileMeasures;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.visitors.Issue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPCustomRulesDefinition;
import org.sonar.plugins.php.phpunit.PhpUnitCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitItCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitOverallCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitService;
import org.sonar.squidbridge.ProgressReport;
import org.sonar.squidbridge.api.AnalysisException;

public class PHPSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PHPSensor.class);

  private final FileSystem fileSystem;
  private final FilePredicate mainFilePredicate;
  private final FileLinesContextFactory fileLinesContextFactory;
  private final PHPChecks checks;
  private final NoSonarFilter noSonarFilter;

  private RuleKey parsingErrorRuleKey;

  public PHPSensor(FileSystem fileSystem, FileLinesContextFactory fileLinesContextFactory,
                   CheckFactory checkFactory, NoSonarFilter noSonarFilter) {
    this(fileSystem, fileLinesContextFactory, checkFactory, noSonarFilter, null);
  }

  public PHPSensor(FileSystem fileSystem, FileLinesContextFactory fileLinesContextFactory,
                   CheckFactory checkFactory, NoSonarFilter noSonarFilter, @Nullable PHPCustomRulesDefinition[] customRulesDefinitions) {

    this.checks = PHPChecks.createPHPCheck(checkFactory)
      .addChecks(CheckList.REPOSITORY_KEY, CheckList.getChecks())
      .addCustomChecks(customRulesDefinitions);
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.fileSystem = fileSystem;
    this.noSonarFilter = noSonarFilter;
    this.mainFilePredicate = this.fileSystem.predicates().and(
      this.fileSystem.predicates().hasType(InputFile.Type.MAIN),
      this.fileSystem.predicates().hasLanguage(Php.KEY));

    parsingErrorRuleKey = getParsingErrorRuleKey();
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage(Php.KEY)
      .name("PHP sensor")
      .onlyOnFileType(Type.MAIN);
  }

  @Override
  public void execute(SensorContext context) {
    ImmutableList<PHPCheck> visitors = getCheckVisitors();

    PHPAnalyzer phpAnalyzer = new PHPAnalyzer(fileSystem.encoding(), visitors);
    ArrayList<InputFile> inputFiles = Lists.newArrayList(fileSystem.inputFiles(mainFilePredicate));

    ProgressReport progressReport = new ProgressReport("Report about progress of PHP analyzer", TimeUnit.SECONDS.toMillis(10));
    progressReport.start(Lists.newArrayList(fileSystem.files(mainFilePredicate)));
    
    Map<File, Integer> numberOLinesOfCode = new HashMap<>();

    analyseFiles(context, phpAnalyzer, inputFiles, progressReport, numberOLinesOfCode);
    
    processCoverage(context, numberOLinesOfCode);
  }
  
  private void processCoverage(SensorContext context, Map<File, Integer> numberOfLinesOfCode) {
    PhpUnitService phpUnitSensor = new PhpUnitService(
      fileSystem,
      new PhpUnitResultParser(fileSystem),
      new PhpUnitCoverageResultParser(fileSystem),
      new PhpUnitItCoverageResultParser(fileSystem),
      new PhpUnitOverallCoverageResultParser(fileSystem));
    phpUnitSensor.execute(context, numberOfLinesOfCode);
  }

  void analyseFiles(
      SensorContext context, 
      PHPAnalyzer phpAnalyzer, 
      List<InputFile> inputFiles, 
      ProgressReport progressReport,
      Map<File, Integer> numberOfLinesOfCode) {
    boolean success = false;
    try {
      for (InputFile inputFile : inputFiles) {
        progressReport.nextFile();
        analyseFile(context, phpAnalyzer, inputFile, numberOfLinesOfCode);
      }
      success = true;
    } finally {
      stopProgressReport(progressReport, success);
    }
  }

  private static void stopProgressReport(ProgressReport progressReport, boolean success) {
    if (success) {
      progressReport.stop();
    } else {
      progressReport.cancel();
    }
  }

  private void analyseFile(SensorContext context, PHPAnalyzer phpAnalyzer, InputFile inputFile, Map<File, Integer> numberOfLinesOfCode) {
    try {
      phpAnalyzer.nextFile(inputFile.file());
      saveIssues(context, phpAnalyzer.analyze(), inputFile);
      saveSyntaxHighlighting(phpAnalyzer.getSyntaxHighlighting(context, inputFile));
      saveSymbolHighlighting(phpAnalyzer.getSymbolHighlighting(context, inputFile));
      saveNewFileMeasures(context, phpAnalyzer.computeMeasures(fileLinesContextFactory.createFor(inputFile), numberOfLinesOfCode), inputFile);
    } catch (RecognitionException e) {
      checkInterrupted(e);
      LOG.error("Unable to parse file: " + inputFile.absolutePath());
      LOG.error(e.getMessage());
      saveParsingIssue(context, e, inputFile);
      return;
    } catch (Exception e) {
      checkInterrupted(e);
      throw new AnalysisException("Could not analyse " + inputFile.absolutePath(), e);
    }
  }
  
  private static void checkInterrupted(Exception e) {
    Throwable cause = Throwables.getRootCause(e);
    if (cause instanceof InterruptedException || cause instanceof InterruptedIOException) {
      throw new AnalysisException("Analysis cancelled", e);
    }
  }

  private static void saveSyntaxHighlighting(NewHighlighting highlighting) {
    highlighting.save();
  }

  private static void saveSymbolHighlighting(NewSymbolTable newSymbolTable) {
    newSymbolTable.save();
  }
  
  private void saveNewFileMeasures(SensorContext context, FileMeasures fileMeasures, InputFile inputFile) {
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getLinesNumber()).forMetric(CoreMetrics.LINES).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getLinesOfCodeNumber()).forMetric(CoreMetrics.NCLOC).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getCommentLinesNumber()).forMetric(CoreMetrics.COMMENT_LINES).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getClassNumber()).forMetric(CoreMetrics.CLASSES).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getFunctionNumber()).forMetric(CoreMetrics.FUNCTIONS).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getStatementNumber()).forMetric(CoreMetrics.STATEMENTS).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getFileComplexity()).forMetric(CoreMetrics.COMPLEXITY).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getClassComplexity()).forMetric(CoreMetrics.COMPLEXITY_IN_CLASSES).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getFunctionComplexity()).forMetric(CoreMetrics.COMPLEXITY_IN_FUNCTIONS).save();

    String functionComplexityMeasure = fileMeasures.getFunctionComplexityDistribution().build();
    context.<String>newMeasure().on(inputFile).withValue(functionComplexityMeasure).forMetric(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION).save();

    String fileComplexityMeasure = fileMeasures.getFileComplexityDistribution().build();
    context.<String>newMeasure().on(inputFile).withValue(fileComplexityMeasure).forMetric(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION).save();

    noSonarFilter.noSonarInFile(inputFile, fileMeasures.getNoSonarLines());
  }

  /**
   * Creates and saves an issue for a parsing error.
   */
  private void saveParsingIssue(SensorContext context, RecognitionException e, InputFile inputFile) {
    if (parsingErrorRuleKey != null) {
      NewIssue issue = context.newIssue();

      NewIssueLocation location = issue.newLocation()
        .message(e.getMessage())
        .on(inputFile);

      if (e.getLine() > 0) {
        location.at(inputFile.selectLine(e.getLine()));
      }

      issue
        .forRule(parsingErrorRuleKey)
        .at(location)
        .save();
    }
  }

  private void saveIssues(SensorContext context, List<Issue> issues, InputFile inputFile) {
    for (Issue phpIssue : issues) {
      RuleKey ruleKey = checks.ruleKeyFor(phpIssue.check());

      NewIssue issue = context.newIssue();

      NewIssueLocation location = issue.newLocation()
        .message(phpIssue.message())
        .on(inputFile);

      if (phpIssue.line() > 0) {
        location.at(inputFile.selectLine(phpIssue.line()));
      }

      issue
        .forRule(ruleKey)
        .gap(phpIssue.cost())
        .at(location)
        .save();
    }
  }

  private ImmutableList<PHPCheck> getCheckVisitors() {
    return ImmutableList.copyOf(checks.all());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private RuleKey getParsingErrorRuleKey() {
    List<RuleKey> keys = checks.all().stream()
      .filter(check -> check instanceof ParsingErrorCheck)
      .map(check -> checks.ruleKeyFor((ParsingErrorCheck) check))
      .collect(Collectors.toList());

    return keys.isEmpty() ? null : keys.get(0);
  }

}
