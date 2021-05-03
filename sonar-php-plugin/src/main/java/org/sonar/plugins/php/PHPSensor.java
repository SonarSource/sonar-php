/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
import com.sonar.sslr.api.RecognitionException;
import java.io.File;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
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
import org.sonar.php.checks.UncatchableExceptionCheck;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.compat.PhpFileImpl;
import org.sonar.php.metrics.CpdVisitor.CpdToken;
import org.sonar.php.metrics.FileMeasures;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPCustomRuleRepository;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;
import org.sonar.plugins.php.phpunit.CoverageResultImporter;
import org.sonar.plugins.php.phpunit.TestResultImporter;

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

    this(fileLinesContextFactory,
      PHPChecks.createPHPCheck(checkFactory)
        .addChecks(CheckList.REPOSITORY_KEY, CheckList.getChecks())
        .addCustomChecks(customRuleRepositories),
      noSonarFilter);
  }

  PHPSensor(FileLinesContextFactory fileLinesContextFactory, PHPChecks checks, NoSonarFilter noSonarFilter) {
    this.checks = checks;
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
    FileSystem fileSystem = context.fileSystem();

    FilePredicate phpFilePredicate = fileSystem.predicates().hasLanguage(Php.KEY);

    List<InputFile> inputFiles = new ArrayList<>();
    fileSystem.inputFiles(phpFilePredicate).forEach(inputFiles::add);

    SymbolScanner symbolScanner = new SymbolScanner(context);

    try {
      symbolScanner.execute(inputFiles);
      ProjectSymbolData projectSymbolData = symbolScanner.getProjectSymbolData();
      new AnalysisScanner(context, projectSymbolData).execute(inputFiles);
      if (!inSonarLint(context)) {
        processTestsAndCoverage(context);
      }
    } catch (CancellationException e) {
      LOG.info(e.getMessage());
    }
  }

  private static boolean inSonarLint(SensorContext context) {
    return context.runtime().getProduct() == SonarProduct.SONARLINT;
  }

  private static void processTestsAndCoverage(SensorContext context) {
    new TestResultImporter().importReport(context);

    CoverageResultImporter.multiCoverageImporter().importReport(context);
  }

  private class AnalysisScanner extends Scanner {

    PHPAnalyzer phpAnalyzer;

    private final boolean hasTestFileChecks;

    public AnalysisScanner(SensorContext context, ProjectSymbolData projectSymbolData) {
      super(context);

      List<PHPCheck> allChecks = checks.all();
      if (inSonarLint(context)) {
        allChecks = allChecks.stream()
          .filter(e -> !(e instanceof UncatchableExceptionCheck))
          .collect(Collectors.toList());
      }

      List<PHPCheck> testFilesChecks  = allChecks.stream().
        filter(c -> c instanceof PhpUnitCheck).
        collect(Collectors.toList());
      hasTestFileChecks = !testFilesChecks.isEmpty();

      phpAnalyzer = new PHPAnalyzer(allChecks, testFilesChecks, context.fileSystem().workDir(), projectSymbolData);
    }

    @Override
    String name() {
      return "PHP rules";
    }

    @Override
    void scanFile(InputFile inputFile) {
      if (inputFile.type() == Type.TEST && !hasTestFileChecks) {
        return;
      }

      try {
        phpAnalyzer.nextFile(new PhpFileImpl(inputFile));

        if (!inSonarLint(context)) {
          phpAnalyzer.getSyntaxHighlighting(context, inputFile).save();
          phpAnalyzer.getSymbolHighlighting(context, inputFile).save();
          if (inputFile.type() == Type.MAIN) {
            saveNewFileMeasures(context,
              phpAnalyzer.computeMeasures(fileLinesContextFactory.createFor(inputFile)),
              inputFile);
            saveCpdData(phpAnalyzer.computeCpdTokens(), inputFile, context);
          }
        }

        noSonarFilter.noSonarInFile(inputFile, phpAnalyzer.computeNoSonarLines());
        saveIssues(context, inputFile.type() == Type.MAIN ? phpAnalyzer.analyze() : phpAnalyzer.analyzeTest(), inputFile);
      } catch (RecognitionException e) {
        checkInterrupted(e);
        LOG.error("Unable to parse file [{}] at line {}", inputFile.uri(), e.getLine());
        LOG.error(e.getMessage());
        saveParsingIssue(context, e, inputFile);
      }
    }

    @Override
    void logException(Exception e, InputFile file) {
      LOG.error("Could not analyse " + file.toString(), e);
    }

    @Override
    void onEnd() {
      phpAnalyzer.terminate();
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
  }

  /**
   * Creates and saves an issue for a parsing error.
   */
  private void saveParsingIssue(SensorContext context, RecognitionException e, InputFile inputFile) {
    if (parsingErrorRuleKey != null) {
      NewIssue issue = context.newIssue();

      NewIssueLocation location = issue.newLocation()
        .message("A parsing error occurred in this file.")
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
        preciseIssue.secondaryLocations().forEach(secondary ->
          addSecondaryLocation(context, inputFile, newIssue, secondary)
        );
      }

      newIssue.save();
    }
  }

  private static void addSecondaryLocation(SensorContext context, InputFile inputFile, NewIssue newIssue, IssueLocation secondary) {
    InputFile file = inputFile;
    String filePath = secondary.filePath();
    if (filePath != null) {
      file = context.fileSystem().inputFile(context.fileSystem().predicates().is(new File(filePath)));
    }
    if (file != null) {
      newIssue.addLocation(newLocation(file, newIssue, secondary));
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

}
