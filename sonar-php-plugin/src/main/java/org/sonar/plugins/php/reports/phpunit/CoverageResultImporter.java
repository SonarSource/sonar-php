/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.plugins.php.reports.phpunit;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.php.PhpPlugin;
import org.sonar.plugins.php.reports.phpunit.xml.FileNode;
import org.sonar.plugins.php.reports.phpunit.xml.LineNode;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.xml.ParseException;

public class CoverageResultImporter extends PhpUnitReportImporter {

  private static final Logger LOG = Loggers.get(CoverageResultImporter.class);

  private static final String WRONG_LINE_EXCEPTION_MESSAGE = "Line with number %s doesn't belong to file %s";
  private static final String COVERAGE_REPORT_DOES_NOT_CONTAINS_ANY_RECORD = "Coverage report does not contains any record in file %s";

  final CoverageFileParserForPhpUnit parser = new CoverageFileParserForPhpUnit();

  public CoverageResultImporter(AnalysisWarningsWrapper analysisWarningsWrapper) {
    super(analysisWarningsWrapper);
  }

  @Override
  public void importReport(File coverageReportFile, SensorContext context) throws IOException, ParseException {
    LOG.info("Importing {}", coverageReportFile);

    CoverageMeasureRecorder recorder = new CoverageMeasureRecorder(this, context);
    parser.consumeAllFileNodes(coverageReportFile, recorder);
    if (recorder.getFileNodeCount() == 0) {
      formatAndCreateWarning(COVERAGE_REPORT_DOES_NOT_CONTAINS_ANY_RECORD, coverageReportFile);
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
          LOG.warn(String.format(WRONG_LINE_EXCEPTION_MESSAGE, lineNum, inputFile.filename()));
        }
      }
    }

    newCoverage.save();
  }

  @Override
  public String reportPathKey() {
    return PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY;
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
   * Small class that save Coverage for each found fileNode and keep track of count.
   */
  private static class CoverageMeasureRecorder implements Consumer<FileNode> {

    private final CoverageResultImporter importer;
    SensorContext context;
    int fileNodeCount=0;

    public CoverageMeasureRecorder(CoverageResultImporter importer, SensorContext context) {
      this.importer = importer;
      this.context = context;
    }

    public int getFileNodeCount() {
      return fileNodeCount;
    }

    @Override
    public void accept(FileNode fileNode) {
      importer.saveCoverageMeasure(fileNode, context);
      fileNodeCount++;
    }
  }

}
