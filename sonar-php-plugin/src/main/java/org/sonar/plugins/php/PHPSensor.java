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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.RecognitionException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.php.PHPAnalyzer;
import org.sonar.php.checks.CheckList;
import org.sonar.php.checks.ParsingErrorCheck;
import org.sonar.php.compat.PhpFileImpl;
import org.sonar.php.metrics.CpdVisitor.CpdToken;
import org.sonar.php.metrics.FileMeasures;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.visitors.PHPCustomRuleRepository;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCustomRulesDefinition;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;
import org.sonar.plugins.php.phpunit.CompatibilityImportersFactory;
import org.sonar.plugins.php.phpunit.TestResultImporter;
import org.sonar.squidbridge.ProgressReport;

public class PHPSensor implements Sensor {

  private static final Logger LOG = Loggers.get(PHPSensor.class);
  private final FileLinesContextFactory fileLinesContextFactory;
  private final PHPChecks checks;
  private final NoSonarFilter noSonarFilter;

  private RuleKey parsingErrorRuleKey;

  public PHPSensor(FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory, NoSonarFilter noSonarFilter) {
    this(fileLinesContextFactory, checkFactory, noSonarFilter, null);
  }

  public PHPSensor(FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory, NoSonarFilter noSonarFilter, @Nullable PHPCustomRuleRepository[] customRuleRepositories) {

    this.checks = PHPChecks.createPHPCheck(checkFactory)
      .addChecks(CheckList.REPOSITORY_KEY, CheckList.getChecks())
      .addCustomChecks(customRuleRepositories);
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
    this.parsingErrorRuleKey = getParsingErrorRuleKey();
    // Hack to have PHPCustomRulesDefinition referenced somewhere so that maven shade minimization
    // includes PHPCustomRulesDefinition and its dependencies in the final jar.
    PHPCustomRulesDefinition.class.getName();
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
    FileSystem fileSystem = context.fileSystem();

    FilePredicate mainFilePredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(Php.KEY));

    PHPAnalyzer phpAnalyzer = new PHPAnalyzer(ImmutableList.copyOf(checks.all()));

    List<InputFile> inputFiles = new ArrayList<>();
    fileSystem.inputFiles(mainFilePredicate).forEach(inputFiles::add);

    ProgressReport progressReport = new ProgressReport("Report about progress of PHP analyzer", TimeUnit.SECONDS.toMillis(10));
    progressReport.start(inputFiles.stream().map(InputFile::toString).collect(Collectors.toList()));

    try {
      analyseFiles(context, phpAnalyzer, inputFiles, progressReport);
      if (inSonarQube(context)) {
        processTestsAndCoverage(context);
      }
    } catch (CancellationException e) {
      LOG.info(e.getMessage());
    }
  }

  private static boolean inSonarQube(SensorContext context) {
    return context.runtime().getProduct() != SonarProduct.SONARLINT;
  }

  private static void processTestsAndCoverage(SensorContext context) {
    new TestResultImporter().importReport(context);

    final CompatibilityImportersFactory importersFactory = new CompatibilityImportersFactory(context);
    importersFactory.deprecationWarnings().forEach(LOG::warn);
    importersFactory.createCoverageImporter().importReport(context);
  }

  void analyseFiles(SensorContext context, PHPAnalyzer phpAnalyzer, Iterable<InputFile> inputFiles, ProgressReport progressReport) {
    boolean success = false;
    try {
      for (InputFile inputFile : inputFiles) {
        checkCancelled(context);
        progressReport.nextFile();
        analyseFile(context, phpAnalyzer, inputFile);
      }
      success = true;
    } finally {
      stopProgressReport(progressReport, success);
    }
  }

  private static void checkCancelled(SensorContext context) {
    if (context.isCancelled()) {
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

  private void analyseFile(SensorContext context, PHPAnalyzer phpAnalyzer, InputFile inputFile) {
    try {
      phpAnalyzer.nextFile(new PhpFileImpl(inputFile));

      if (inSonarQube(context)) {
        phpAnalyzer.getSyntaxHighlighting(context, inputFile).save();
        phpAnalyzer.getSymbolHighlighting(context, inputFile).save();
        saveNewFileMeasures(context,
          phpAnalyzer.computeMeasures(fileLinesContextFactory.createFor(inputFile)),
          inputFile);
        if (inSonarQube(context)) {
          saveCpdData(phpAnalyzer.computeCpdTokens(), inputFile, context);
        }
      }

      noSonarFilter.noSonarInFile(inputFile, phpAnalyzer.computeNoSonarLines());
      saveIssues(context, phpAnalyzer.analyze(), inputFile);

    } catch (RecognitionException e) {
      checkInterrupted(e);
      LOG.error("Unable to parse file [{}] at line {}", inputFile.uri(), e.getLine());
      LOG.error(e.getMessage());
      saveParsingIssue(context, e, inputFile);
    } catch (Exception e) {
      checkInterrupted(e);
      throw new AnalysisException("Could not analyse " + inputFile.filename(), e);
    }
  }

  private static void checkInterrupted(Exception e) {
    Throwable cause = Throwables.getRootCause(e);
    if (cause instanceof InterruptedException || cause instanceof InterruptedIOException) {
      throw new AnalysisException("Analysis cancelled", e);
    }
  }

  private static void saveNewFileMeasures(SensorContext context, FileMeasures fileMeasures, InputFile inputFile) {
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getLinesOfCodeNumber()).forMetric(CoreMetrics.NCLOC).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getCommentLinesNumber()).forMetric(CoreMetrics.COMMENT_LINES).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getClassNumber()).forMetric(CoreMetrics.CLASSES).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getFunctionNumber()).forMetric(CoreMetrics.FUNCTIONS).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getStatementNumber()).forMetric(CoreMetrics.STATEMENTS).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getFileCognitiveComplexity()).forMetric(CoreMetrics.COGNITIVE_COMPLEXITY).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getFileComplexity()).forMetric(CoreMetrics.COMPLEXITY).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getClassComplexity()).forMetric(CoreMetrics.COMPLEXITY_IN_CLASSES).save();
    context.<Integer>newMeasure().on(inputFile).withValue(fileMeasures.getFunctionComplexity()).forMetric(CoreMetrics.COMPLEXITY_IN_FUNCTIONS).save();

    String functionComplexityMeasure = fileMeasures.getFunctionComplexityDistribution().build();
    context.<String>newMeasure().on(inputFile).withValue(functionComplexityMeasure).forMetric(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION).save();

    String fileComplexityMeasure = fileMeasures.getFileComplexityDistribution().build();
    context.<String>newMeasure().on(inputFile).withValue(fileComplexityMeasure).forMetric(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION).save();
  }

  /**
   * Creates and saves an issue for a parsing error.
   */
  private void saveParsingIssue(SensorContext context, RecognitionException e, InputFile inputFile) {
    if (parsingErrorRuleKey != null) {
      NewIssue issue = context.newIssue();

      NewIssueLocation location = issue.newLocation()
        .message(e.getMessage())
        .on(inputFile)
        .at(inputFile.selectLine(e.getLine()));

      issue
        .forRule(parsingErrorRuleKey)
        .at(location)
        .save();
    }

    context.newAnalysisError()
      .onFile(inputFile)
      .at(inputFile.newPointer(e.getLine(), 0))
      .message(e.getMessage())
      .save();
  }

  private void saveIssues(SensorContext context, List<PhpIssue> issues, InputFile inputFile) {
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
          .on(inputFile);

        if (legacyIssue.line() > 0) {
          location.at(inputFile.selectLine(legacyIssue.line()));
        }

        newIssue.at(location);

      } else if (issue instanceof LineIssue) {

        LineIssue lineIssue = (LineIssue) issue;

        NewIssueLocation location = newIssue.newLocation()
          .message(lineIssue.message())
          .on(inputFile)
          .at(inputFile.selectLine(lineIssue.line()));

        newIssue.at(location);

      } else if (issue instanceof FileIssue) {

        FileIssue fileIssue = (FileIssue) issue;

        NewIssueLocation location = newIssue.newLocation()
          .message(fileIssue.message())
          .on(inputFile);

        newIssue.at(location);

      } else {

        PreciseIssue preciseIssue = (PreciseIssue) issue;

        newIssue.at(newLocation(inputFile, newIssue, preciseIssue.primaryLocation()));
        preciseIssue.secondaryLocations().forEach(secondary -> newIssue.addLocation(newLocation(inputFile, newIssue, secondary)));
      }

      newIssue.save();
    }
  }

  private static NewIssueLocation newLocation(InputFile inputFile, NewIssue issue, IssueLocation location) {
    TextRange range = inputFile.newRange(location.startLine(), location.startLineOffset(), location.endLine(), location.endLineOffset());

    NewIssueLocation newLocation = issue.newLocation()
      .on(inputFile)
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

  private static void saveCpdData(List<CpdToken> cpdTokens, InputFile inputFile, SensorContext context) {
    NewCpdTokens newCpdTokens = context.newCpdTokens().onFile(inputFile);

    cpdTokens.forEach(cpdToken -> newCpdTokens.addToken(
      inputFile.newRange(
        cpdToken.syntaxToken().line(),
        cpdToken.syntaxToken().column(),
        cpdToken.syntaxToken().endLine(),
        cpdToken.syntaxToken().endColumn()),
      cpdToken.image()));

    newCpdTokens.save();
  }

  private static class CancellationException extends RuntimeException {

    CancellationException(String message) {
      super(message);
    }
  }
}
