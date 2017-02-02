/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.RecognitionException;
import java.io.InterruptedIOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.TextRange;
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
import org.sonar.php.compat.CompatibilityHelper;
import org.sonar.php.compat.CompatibleInputFile;
import org.sonar.php.metrics.CpdVisitor;
import org.sonar.php.metrics.FileMeasures;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPCustomRulesDefinition;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;
import org.sonar.plugins.php.phpunit.PhpUnitCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitItCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitOverallCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitService;
import org.sonar.squidbridge.ProgressReport;
import org.sonar.squidbridge.api.AnalysisException;
import static org.sonar.plugins.php.PhpPlugin.SQ_VERSION_6_0;

public class PHPSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PHPSensor.class);
  private final FileLinesContextFactory fileLinesContextFactory;
  private final PHPChecks checks;
  private final NoSonarFilter noSonarFilter;

  private RuleKey parsingErrorRuleKey;

  private FileSystem fileSystem;
  private SensorContext context;

  public PHPSensor(FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory, NoSonarFilter noSonarFilter) {
    this(fileLinesContextFactory, checkFactory, noSonarFilter, null);
  }

  public PHPSensor(FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory, NoSonarFilter noSonarFilter, @Nullable PHPCustomRulesDefinition[] customRulesDefinitions) {

    this.checks = PHPChecks.createPHPCheck(checkFactory)
      .addChecks(CheckList.REPOSITORY_KEY, CheckList.getChecks())
      .addCustomChecks(customRulesDefinitions);
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
    this.parsingErrorRuleKey = getParsingErrorRuleKey();
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
    this.fileSystem = context.fileSystem();
    this.context = context;

    FilePredicate mainFilePredicate = this.fileSystem.predicates().and(
      this.fileSystem.predicates().hasType(InputFile.Type.MAIN),
      this.fileSystem.predicates().hasLanguage(Php.KEY));

    ImmutableList.Builder<PHPCheck> visitors = ImmutableList.<PHPCheck>builder().addAll(checks.all());
    if (inSonarQube()) {
      visitors.add(new CpdVisitor(context));
    }

    PHPAnalyzer phpAnalyzer = new PHPAnalyzer(fileSystem.encoding(), visitors.build());
    Collection<CompatibleInputFile> inputFiles = CompatibilityHelper.wrap(fileSystem.inputFiles(mainFilePredicate), context);

    Map<String, Integer> numberOLinesOfCode = new HashMap<>();

    try {
      analyseFiles(context, phpAnalyzer, inputFiles, numberOLinesOfCode);
      if (inSonarQube()) {
        processCoverage(numberOLinesOfCode);
      }
    } catch (CancellationException e) {
      LOG.info(e.getMessage());
    }
  }

  private boolean inSonarQube() {
    return !context.getSonarQubeVersion().isGreaterThanOrEqual(SQ_VERSION_6_0) || context.runtime().getProduct() != SonarProduct.SONARLINT;
  }

  private void processCoverage(Map<String, Integer> numberOfLinesOfCode) {
    PhpUnitService phpUnitSensor = new PhpUnitService(
      fileSystem,
      new PhpUnitResultParser(fileSystem),
      new PhpUnitCoverageResultParser(fileSystem),
      new PhpUnitItCoverageResultParser(fileSystem),
      new PhpUnitOverallCoverageResultParser(fileSystem));
    phpUnitSensor.execute(context, numberOfLinesOfCode);
  }

  void analyseFiles(SensorContext context, PHPAnalyzer phpAnalyzer, Collection<CompatibleInputFile> inputFiles, Map<String, Integer> numberOfLinesOfCode) {
    ProgressReport progressReport = new ProgressReport("Report about progress of PHP analyzer", TimeUnit.SECONDS.toMillis(10));
    progressReport.start(inputFiles.stream().map(CompatibleInputFile::file).collect(Collectors.toList()));
    boolean success = false;
    try {
      for (CompatibleInputFile inputFile : inputFiles) {
        checkCancelled(context);
        progressReport.nextFile();
        analyseFile(context, phpAnalyzer, inputFile, numberOfLinesOfCode);
      }
      success = true;
    } finally {
      stopProgressReport(progressReport, success);
    }
  }

  private void checkCancelled(SensorContext context) {
    if (context.getSonarQubeVersion().isGreaterThanOrEqual(SQ_VERSION_6_0) && context.isCancelled()) {
      throw new CancellationException("Analysis cancelled");
    }
  }

  private static void stopProgressReport(ProgressReport progressReport, boolean success) {
    if (success) {
      progressReport.stop();
    } else {
      progressReport.cancel();
    }
  }

  private void analyseFile(SensorContext context, PHPAnalyzer phpAnalyzer, CompatibleInputFile inputFile, Map<String, Integer> numberOfLinesOfCode) {
    try {
      phpAnalyzer.nextFile(inputFile);
      saveIssues(context, phpAnalyzer.analyze(), inputFile);

      if (!context.getSonarQubeVersion().isGreaterThanOrEqual(SQ_VERSION_6_0) || context.runtime().getProduct() != SonarProduct.SONARLINT) {
        saveSyntaxHighlighting(phpAnalyzer.getSyntaxHighlighting(context, inputFile));
        saveSymbolHighlighting(phpAnalyzer.getSymbolHighlighting(context, inputFile));
        FileMeasures measures = phpAnalyzer.computeMeasures(fileLinesContextFactory.createFor(inputFile.wrapped()),
          context.getSonarQubeVersion().isGreaterThanOrEqual(PhpPlugin.SQ_VERSION_6_2));
        numberOfLinesOfCode.put(inputFile.relativePath(), measures.getLinesOfCodeNumber());
        saveNewFileMeasures(context, measures, inputFile.wrapped());
      }
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
  private void saveParsingIssue(SensorContext context, RecognitionException e, CompatibleInputFile inputFile) {
    if (parsingErrorRuleKey != null) {
      NewIssue issue = context.newIssue();

      NewIssueLocation location = issue.newLocation()
        .message(e.getMessage())
        .on(inputFile.wrapped())
        .at(inputFile.selectLine(e.getLine()));

      issue
        .forRule(parsingErrorRuleKey)
        .at(location)
        .save();
    }

    if (context.getSonarQubeVersion().isGreaterThanOrEqual(SQ_VERSION_6_0)) {
      context.newAnalysisError()
        .onFile(inputFile.wrapped())
        .at(inputFile.newPointer(e.getLine(), 0))
        .message(e.getMessage())
        .save();
    }
  }

  private void saveIssues(SensorContext context, List<PhpIssue> issues, CompatibleInputFile inputFile) {
    for (PhpIssue issue : issues) {
      RuleKey ruleKey = checks.ruleKeyFor(issue.check());
      NewIssue newIssue = context.newIssue()
        .forRule(ruleKey)
        .gap(issue.cost());

      if (issue instanceof LegacyIssue) {
        // todo: this block should be removed as PHPIssue's usages will be removed
        LegacyIssue legacyIssue = (LegacyIssue) issue;

        NewIssueLocation location = newIssue.newLocation()
          .message(legacyIssue.message())
          .on(inputFile.wrapped());

        if (legacyIssue.line() > 0) {
          location.at(inputFile.selectLine(legacyIssue.line()));
        }

        newIssue.at(location);

      } else if (issue instanceof LineIssue) {

        LineIssue lineIssue = (LineIssue) issue;

        NewIssueLocation location = newIssue.newLocation()
          .message(lineIssue.message())
          .on(inputFile.wrapped())
          .at(inputFile.selectLine(lineIssue.line()));

        newIssue.at(location);

      } else if (issue instanceof FileIssue) {

        FileIssue fileIssue = (FileIssue) issue;

        NewIssueLocation location = newIssue.newLocation()
          .message(fileIssue.message())
          .on(inputFile.wrapped());

        newIssue.at(location);

      } else {

        PreciseIssue preciseIssue = (PreciseIssue) issue;

        newIssue.at(newLocation(inputFile, newIssue, preciseIssue.primaryLocation()));
        preciseIssue.secondaryLocations().forEach(secondary -> newIssue.addLocation(newLocation(inputFile, newIssue, secondary)));
      }

      newIssue.save();
    }
  }

  private static NewIssueLocation newLocation(CompatibleInputFile inputFile, NewIssue issue, IssueLocation location) {
    TextRange range = inputFile.newRange(location.startLine(), location.startLineOffset(), location.endLine(), location.endLineOffset());

    NewIssueLocation newLocation = issue.newLocation()
      .on(inputFile.wrapped())
      .at(range);

    if (location.message() != null) {
      newLocation.message(location.message());
    }
    return newLocation;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private RuleKey getParsingErrorRuleKey() {
    List<RuleKey> keys = checks.all().stream()
      .filter(check -> check instanceof ParsingErrorCheck)
      .map(checks::ruleKeyFor)
      .collect(Collectors.toList());

    return keys.isEmpty() ? null : keys.get(0);
  }

}
