/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.php;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.php.PHPAnalyzer;
import org.sonar.php.PHPAstScanner;
import org.sonar.php.PHPConfiguration;
import org.sonar.php.api.PHPMetric;
import org.sonar.php.checks.CheckList;
import org.sonar.php.metrics.FileLinesVisitor;
import org.sonar.php.metrics.FileMeasures;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.ProgressReport;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.CodeVisitor;
import org.sonar.squidbridge.api.SourceClass;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceFunction;
import org.sonar.squidbridge.indexer.QueryByParent;
import org.sonar.squidbridge.indexer.QueryByType;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PHPSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PHPSensor.class);
  private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12};
  private static final Number[] FILES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

  private final ResourcePerspectives resourcePerspectives;
  private final FileSystem fileSystem;
  private final FilePredicate mainFilePredicate;
  private final FileLinesContextFactory fileLinesContextFactory;
  private final Checks<CodeVisitor> checks;
  private final NoSonarFilter noSonarFilter;
  private AstScanner<LexerlessGrammar> scanner;
  private SensorContext context;

  public PHPSensor(ResourcePerspectives resourcePerspectives, FileSystem filesystem,
                   FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory, NoSonarFilter noSonarFilter) {
    this.checks = checkFactory
      .<CodeVisitor>create(CheckList.REPOSITORY_KEY)
      .addAnnotatedChecks(CheckList.getChecks());
    this.resourcePerspectives = resourcePerspectives;
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.fileSystem = filesystem;
    this.noSonarFilter = noSonarFilter;
    this.mainFilePredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(Php.KEY));
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.hasFiles(mainFilePredicate);
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    this.context = context;

    List<CodeVisitor> visitors = getCheckVisitors();

    visitors.add(new FileLinesVisitor(fileSystem, fileLinesContextFactory));
    ImmutableList.Builder<PHPCheck> phpCheckBuilder = ImmutableList.builder();

    // fixme : Remove this after migration of all checks
    // --------------
    List<CodeVisitor> oldChecks = new ArrayList<>();

    for (CodeVisitor codeVisitor : visitors) {
      if (codeVisitor instanceof PHPCheck) {
        phpCheckBuilder.add((PHPCheck) codeVisitor);
      } else {
        oldChecks.add(codeVisitor);
      }
    }

    this.scanner = PHPAstScanner.create(createConfiguration(), oldChecks.toArray(new SquidAstVisitor[oldChecks.size()]));
    scanner.scanFiles(Lists.newArrayList(fileSystem.files(mainFilePredicate)));
    save(scanner.getIndex().search(new QueryByType(SourceFile.class)));

    LOG.info("Starting running rules based on strongly-typed tree");
    // --------------

    PHPAnalyzer phpAnalyzer = new PHPAnalyzer(fileSystem.encoding(), phpCheckBuilder.build());
    ArrayList<InputFile> inputFiles = Lists.newArrayList(fileSystem.inputFiles(mainFilePredicate));

    ProgressReport progressReport = new ProgressReport("Report about progress of PHP analyzer", TimeUnit.SECONDS.toMillis(10));
    progressReport.start(Lists.newArrayList(fileSystem.files(mainFilePredicate)));

    for (InputFile inputFile : inputFiles) {
      progressReport.nextFile();
      phpAnalyzer.nextFile(inputFile.file());

      saveIssues(phpAnalyzer.analyze(), inputFile);
//      saveNewFileMeasures(phpAnalyzer.computeMeasures(fileLinesContextFactory.createFor(inputFile)), inputFile);
    }

    progressReport.stop();

  }

  private void saveNewFileMeasures(FileMeasures fileMeasures, InputFile inputFile) {
    context.saveMeasure(inputFile, CoreMetrics.LINES, fileMeasures.getLinesNumber());
    context.saveMeasure(inputFile, CoreMetrics.NCLOC, fileMeasures.getLinesOfCodeNumber());
    context.saveMeasure(inputFile, CoreMetrics.COMMENT_LINES, fileMeasures.getCommentLinesNumber());
    context.saveMeasure(inputFile, CoreMetrics.CLASSES, fileMeasures.getClassNumber());
    context.saveMeasure(inputFile, CoreMetrics.FUNCTIONS, fileMeasures.getFunctionNumber());
    context.saveMeasure(inputFile, CoreMetrics.STATEMENTS, fileMeasures.getStatementNumber());

    context.saveMeasure(inputFile, CoreMetrics.COMPLEXITY, fileMeasures.getFileComplexity());
    context.saveMeasure(inputFile, CoreMetrics.COMPLEXITY_IN_CLASSES, fileMeasures.getClassComplexity());
    context.saveMeasure(inputFile, CoreMetrics.COMPLEXITY_IN_FUNCTIONS, fileMeasures.getFunctionComplexity());

    context.saveMeasure(inputFile, fileMeasures.getFunctionComplexityDistribution().build(true).setPersistenceMode(PersistenceMode.MEMORY));
    context.saveMeasure(inputFile, fileMeasures.getFileComplexityDistribution().build(true).setPersistenceMode(PersistenceMode.MEMORY));

    noSonarFilter.addComponent(context.getResource(inputFile).getEffectiveKey(), fileMeasures.getNoSonarLines());
  }

  private void save(Collection<SourceCode> squidSourceFiles) {
    for (SourceCode squidSourceFile : squidSourceFiles) {
      SourceFile squidFile = (SourceFile) squidSourceFile;
      InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(squidFile.getKey()));

      if (inputFile != null) {
        org.sonar.api.resources.File sonarFile = org.sonar.api.resources.File.create(inputFile.relativePath());

        saveClassComplexity(sonarFile, squidFile);
        saveFilesComplexityDistribution(sonarFile, squidFile);
        saveFunctionsComplexityDistribution(sonarFile, squidFile);
        saveFileMeasures(sonarFile, squidFile);
        saveOldIssues(sonarFile, squidFile);
      } else {
        LOG.warn("Cannot save analysis information for file {}. Unable to retrieve the associated sonar resource.", squidFile.getKey());
      }
    }
  }

  private void saveFileMeasures(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    context.saveMeasure(sonarFile, CoreMetrics.FILES, squidFile.getDouble(PHPMetric.FILES));
    context.saveMeasure(sonarFile, CoreMetrics.LINES, squidFile.getDouble(PHPMetric.LINES));
    context.saveMeasure(sonarFile, CoreMetrics.NCLOC, squidFile.getDouble(PHPMetric.LINES_OF_CODE));
    context.saveMeasure(sonarFile, CoreMetrics.COMMENT_LINES, squidFile.getDouble(PHPMetric.COMMENT_LINES));
    context.saveMeasure(sonarFile, CoreMetrics.CLASSES, squidFile.getDouble(PHPMetric.CLASSES));
    context.saveMeasure(sonarFile, CoreMetrics.FUNCTIONS, squidFile.getDouble(PHPMetric.FUNCTIONS));
    context.saveMeasure(sonarFile, CoreMetrics.STATEMENTS, squidFile.getDouble(PHPMetric.STATEMENTS));
    context.saveMeasure(sonarFile, CoreMetrics.COMPLEXITY, squidFile.getDouble(PHPMetric.COMPLEXITY));
  }

  private void saveClassComplexity(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    Collection<SourceCode> classes = scanner.getIndex().search(new QueryByParent(squidFile), new QueryByType(SourceClass.class));
    double complexityInClasses = 0;
    for (SourceCode squidClass : classes) {
      double classComplexity = squidClass.getDouble(PHPMetric.COMPLEXITY);
      complexityInClasses += classComplexity;
    }
    context.saveMeasure(sonarFile, CoreMetrics.COMPLEXITY_IN_CLASSES, complexityInClasses);
  }

  private void saveFunctionsComplexityDistribution(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    Collection<SourceCode> squidFunctionsInFile = scanner.getIndex().search(new QueryByParent(squidFile), new QueryByType(SourceFunction.class));
    RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
    double complexityInFunction = 0;

    for (SourceCode squidFunction : squidFunctionsInFile) {
      double functionComplexity = squidFunction.getDouble(PHPMetric.COMPLEXITY);
      complexityDistribution.add(functionComplexity);
      complexityInFunction += functionComplexity;
    }
    context.saveMeasure(sonarFile, complexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
    context.saveMeasure(sonarFile, CoreMetrics.COMPLEXITY_IN_FUNCTIONS, complexityInFunction);
  }

  private void saveFilesComplexityDistribution(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION, FILES_DISTRIB_BOTTOM_LIMITS);
    complexityDistribution.add(squidFile.getDouble(PHPMetric.COMPLEXITY));
    context.saveMeasure(sonarFile, complexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
  }


  /**
   * To remove after migration of all checks.
   */
  private void saveOldIssues(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    Collection<CheckMessage> messages = squidFile.getCheckMessages();
    if (messages != null) {

      for (CheckMessage message : messages) {
        RuleKey ruleKey = checks.ruleKey((CodeVisitor) message.getCheck());
        Issuable issuable = resourcePerspectives.as(Issuable.class, sonarFile);

        if (issuable != null) {
          Issue issue = issuable.newIssueBuilder()
            .ruleKey(ruleKey)
            .line(message.getLine())
            .message(message.getText(Locale.ENGLISH))
            .effortToFix(message.getCost())
            .build();
          issuable.addIssue(issue);
        }
      }
    }
  }

  private void saveIssues(List<org.sonar.plugins.php.api.visitors.Issue> issues, InputFile inputFile) {
    for (org.sonar.plugins.php.api.visitors.Issue phpIssue : issues) {
      RuleKey ruleKey = RuleKey.of(CheckList.REPOSITORY_KEY, phpIssue.ruleKey());
      Issuable issuable = resourcePerspectives.as(Issuable.class, inputFile);

      if (issuable != null) {
        Issuable.IssueBuilder issueBuilder = issuable.newIssueBuilder()
          .ruleKey(ruleKey)
          .message(phpIssue.message())
          .effortToFix(phpIssue.cost());

        if (phpIssue.line() > 0) {
          issueBuilder.line(phpIssue.line());
        }

        issuable.addIssue(issueBuilder.build());
      }
    }
  }

  private PHPConfiguration createConfiguration() {
    return new PHPConfiguration(fileSystem.encoding());
  }

  private List<CodeVisitor> getCheckVisitors() {
    return new ArrayList<>(checks.all());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
