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
package org.sonar.plugins.php.reports.phpunit;

import com.ctc.wstx.exc.WstxIOException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.php.PhpPlugin;
import org.sonar.plugins.php.reports.phpunit.xml.CoverageNode;
import org.sonar.plugins.php.reports.phpunit.xml.FileNode;
import org.sonar.plugins.php.reports.phpunit.xml.LineNode;
import org.sonar.plugins.php.reports.phpunit.xml.PackageNode;
import org.sonar.plugins.php.reports.phpunit.xml.ProjectNode;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.xml.ParseException;

public class CoverageResultImporter extends PhpUnitReportImporter {

  private static final Logger LOG = Loggers.get(CoverageResultImporter.class);

  private static final String WRONG_LINE_EXCEPTION_MESSAGE = "Line with number %s doesn't belong to file %s";

  public CoverageResultImporter(AnalysisWarningsWrapper analysisWarningsWrapper) {
    super(analysisWarningsWrapper);
  }

  @Override
  public void importReport(File coverageReportFile, SensorContext context) throws IOException, ParseException {
    LOG.info("Importing {}", coverageReportFile);
    parseFile(coverageReportFile, context);
  }

  private void parseFile(File coverageReportFile, SensorContext context) throws IOException, ParseException {
    CoverageNode coverage = getCoverage(coverageReportFile);

    List<ProjectNode> projects = coverage.getProjects();
    if (projects != null && !projects.isEmpty()) {
      ProjectNode projectNode = projects.get(0);
      parseFileNodes(projectNode.getFiles(), context);
      parsePackagesNodes(projectNode.getPackages(), context);
    }
  }


  private void parsePackagesNodes(@Nullable List<PackageNode> packages, SensorContext context) {
    if (packages != null) {
      for (PackageNode packageNode : packages) {
        parseFileNodes(packageNode.getFiles(), context);
      }
    }
  }

  private void parseFileNodes(@Nullable List<FileNode> fileNodes, SensorContext context) {
    if (fileNodes != null) {
      for (FileNode file : fileNodes) {
        saveCoverageMeasure(file, context);
      }
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

  /**
   * Gets the coverage.
   *
   * @param coverageReportFile the coverage report file
   * @return the coverage
   */
  private static CoverageNode getCoverage(File coverageReportFile) throws ParseException, IOException {
    SMInputFactory inputFactory = JUnitLogParserForPhpUnit.inputFactory();
    try {
      SMHierarchicCursor rootCursor = inputFactory.rootElementCursor(coverageReportFile);
      rootCursor.advance();
      if (!"coverage".equals(rootCursor.getLocalName())) {
        throw new XMLStreamException("Report should start with <coverage>");
      }
      return parseCoverageNode(rootCursor);
    } catch (WstxIOException e) {
      throw new IOException(e.getMessage(), e.getCause());
    } catch (XMLStreamException e) {
      throw new ParseException(e);
    }
  }

  private static CoverageNode parseCoverageNode(SMHierarchicCursor cursor) throws XMLStreamException {
    CoverageNode result = new CoverageNode();
    SMInputCursor childCursor = cursor.childElementCursor("project");
    while (childCursor.getNext() != null) {
      result.getProjects().add(parseProjectNode(childCursor));
    }
    return result;
  }

  private static ProjectNode parseProjectNode(SMInputCursor cursor) throws XMLStreamException {
    ProjectNode result = new ProjectNode();
    result.setName(cursor.getAttrValue("name"));
    SMInputCursor childCursor = cursor.childElementCursor();
    while (childCursor.getNext() != null) {
      if ("package".equals(childCursor.getLocalName())) {
        result.getPackages().add(parsePackageNode(childCursor));
      } else if ("file".equals(childCursor.getLocalName())) {
        result.getFiles().add(parseFileNode(childCursor));
      }
    }
    return result;
  }

  private static PackageNode parsePackageNode(SMInputCursor cursor) throws XMLStreamException {
    PackageNode result = new PackageNode();
    result.setName(cursor.getAttrValue("name"));
    SMInputCursor childCursor = cursor.childElementCursor("file");
    while (childCursor.getNext() != null) {
      result.getFiles().add(parseFileNode(childCursor));
    }
    return result;
  }

  private static FileNode parseFileNode(SMInputCursor cursor) throws XMLStreamException {
    FileNode result = new FileNode();
    result.setName(cursor.getAttrValue("name"));
    SMInputCursor childCursor = cursor.childElementCursor("line");
    while (childCursor.getNext() != null) {
      result.getLines().add(parseLineNode(childCursor));
    }
    return result;
  }

  private static LineNode parseLineNode(SMInputCursor cursor) throws XMLStreamException {
    int count = attributeIntValue(cursor, "count");
    int num = attributeIntValue(cursor, "num");
    String type = cursor.getAttrValue("type");
    return new LineNode(count, num, type);
  }

  private static int attributeIntValue(SMInputCursor cursor, String name) throws XMLStreamException {
    return Integer.parseInt(cursor.getAttrValue(name));
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
}
