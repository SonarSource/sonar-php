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
package org.sonar.plugins.php.reports.phpunit;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.plugins.php.reports.phpunit.xml.FileNode;
import org.sonar.plugins.php.reports.phpunit.xml.LineNode;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

public class CoverageResultImporter extends PhpUnitReportImporter {

  private static final Logger LOG = LoggerFactory.getLogger(CoverageResultImporter.class);

  private static final String WRONG_LINE_EXCEPTION_MESSAGE = "Line with number {} doesn't belong to file {}";
  private static final String COVERAGE_REPORT_DOES_NOT_CONTAIN_ANY_RECORD = "Coverage report does not contain any record in file %s";

  private final CoverageFileParserForPhpUnit parser = new CoverageFileParserForPhpUnit();

  public CoverageResultImporter(AnalysisWarningsWrapper analysisWarningsWrapper) {
    super(analysisWarningsWrapper);
  }

  @Override
  public void importReport(File report, SensorContext context) throws IOException, ParseException {
    LOG.info("Importing {}", report);

    CoverageMeasureRecorder recorder = new CoverageMeasureRecorder(this, context);
    parser.parse(report, recorder);

    if (recorder.fileNodeCount == 0) {
      createWarning(COVERAGE_REPORT_DOES_NOT_CONTAIN_ANY_RECORD, report);
    }
  }

  /**
   * Saves the required metrics found on the fileNode
   *
   * @param fileNode        the file
   */
  private void saveCoverageMeasure(FileNode fileNode, SensorContext context) {
    FileSystem fileSystem = context.fileSystem();
    // PHP supports only absolute paths
    String path = fileHandler.relativePath(fileNode.getName());
    InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasPath(path));

    // Due to an unexpected behaviour in phpunit.coverage.xml containing references to covered source files, we have to check that the
    // targeted file for coverage is not null.
    if (inputFile != null) {
      saveCoverageLineHitsData(fileNode, inputFile, context);

      // Saving the uncovered statements (lines) is no longer needed because coverage metrics are internally derived by the NewCoverage
    } else {
      addUnresolvedInputFile(path);
    }
  }

  private static void saveCoverageLineHitsData(FileNode fileNode, InputFile inputFile, SensorContext context) {
    NewCoverage newCoverage = context.newCoverage().onFile(inputFile);

    if (fileNode.getLines() != null) {
      for (LineNode line : fileNode.getLines()) {
        int lineNum = line.getNum();
        if (lineNum > 0 && lineNum <= inputFile.lines()) {
          newCoverage.lineHits(line.getNum(), line.getCount());
        } else {
          String filename = inputFile.filename();
          LOG.warn(WRONG_LINE_EXCEPTION_MESSAGE, lineNum, filename);
        }
      }
    }

    newCoverage.save();
  }

  @Override
  public String reportPathKey() {
    return PhpUnitSensor.PHPUNIT_COVERAGE_REPORT_PATHS_KEY;
  }

  @Override
  public String reportName() {
    return "PHPUnit coverage";
  }

  @Override
  public Logger logger() {
    return LOG;
  }

  /**
   * Class used to count the encountered fileNodes and save the coverage measurements for each of them.
   */
  private static class CoverageMeasureRecorder implements Consumer<FileNode> {

    private final CoverageResultImporter importer;
    SensorContext context;
    protected int fileNodeCount = 0;

    public CoverageMeasureRecorder(CoverageResultImporter importer, SensorContext context) {
      this.importer = importer;
      this.context = context;
    }

    @Override
    public void accept(FileNode fileNode) {
      importer.saveCoverageMeasure(fileNode, context);
      fileNodeCount++;
    }
  }

}
