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
package org.sonar.plugins.php.phpunit;

import java.io.File;
import java.util.ArrayList;
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
import org.sonar.plugins.php.phpunit.xml.CoverageNode;
import org.sonar.plugins.php.phpunit.xml.FileNode;
import org.sonar.plugins.php.phpunit.xml.LineNode;
import org.sonar.plugins.php.phpunit.xml.PackageNode;
import org.sonar.plugins.php.phpunit.xml.ProjectNode;

public class CoverageResultImporter extends SingleFileReportImporter {

  private static final Logger LOG = Loggers.get(CoverageResultImporter.class);

  private static final String WRONG_LINE_EXCEPTION_MESSAGE = "Line with number %s doesn't belong to file %s";

  CoverageResultImporter(String reportPathKey, String msg) {
    super(reportPathKey, msg);
  }

  @Override
  protected void importReport(File coverageReportFile, SensorContext context) {
    LOG.debug("Parsing file: " + coverageReportFile.getAbsolutePath());
    parseFile(coverageReportFile, context);
  }

  private static void parseFile(File coverageReportFile, SensorContext context) {
    CoverageNode coverage = getCoverage(coverageReportFile);

    List<String> unresolvedPaths = new ArrayList<>();
    List<ProjectNode> projects = coverage.getProjects();
    if (projects != null && !projects.isEmpty()) {
      ProjectNode projectNode = projects.get(0);
      parseFileNodes(projectNode.getFiles(), unresolvedPaths, context);
      parsePackagesNodes(projectNode.getPackages(), unresolvedPaths, context);
    }
    if (!unresolvedPaths.isEmpty()) {
      LOG.warn(
        String.format(
          "Could not resolve %d file paths in %s, first unresolved path: %s",
          unresolvedPaths.size(), coverageReportFile.getName(), unresolvedPaths.get(0)));
    }
  }


  private static void parsePackagesNodes(@Nullable List<PackageNode> packages, List<String> unresolvedPaths, SensorContext context) {
    if (packages != null) {
      for (PackageNode packageNode : packages) {
        parseFileNodes(packageNode.getFiles(), unresolvedPaths, context);
      }
    }
  }

  private static void parseFileNodes(@Nullable List<FileNode> fileNodes, List<String> unresolvedPaths, SensorContext context) {
    if (fileNodes != null) {
      for (FileNode file : fileNodes) {
        saveCoverageMeasure(file, unresolvedPaths, context);
      }
    }
  }

  /**
   * Saves the required metrics found on the fileNode
   *
   * @param fileNode        the file
   * @param unresolvedPaths list of paths which cannot be mapped to imported files
   */
  private static void saveCoverageMeasure(FileNode fileNode, List<String> unresolvedPaths, SensorContext context) {
    FileSystem fileSystem = context.fileSystem();
    // PHP supports only absolute paths
    String path = fileNode.getName();
    InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasPath(path));

    // Due to an unexpected behaviour in phpunit.coverage.xml containing references to covered source files, we have to check that the
    // targeted file for coverage is not null.
    if (inputFile != null) {
      saveCoverageLineHitsData(fileNode, inputFile, context);

      // Saving the uncovered statements (lines) is no longer needed because coverage metrics are internally derived by the NewCoverage
    } else {
      unresolvedPaths.add(path);
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
  private static CoverageNode getCoverage(File coverageReportFile) {
    SMInputFactory inputFactory = JUnitLogParserForPhpUnit.inputFactory();
    try {
      SMHierarchicCursor rootCursor = inputFactory.rootElementCursor(coverageReportFile);
      rootCursor.advance();
      if (!"coverage".equals(rootCursor.getLocalName())) {
        throw new XMLStreamException("Report should start with <coverage>");
      }
      return parseCoverageNode(rootCursor);
    } catch (XMLStreamException e) {
      throw new IllegalStateException("Can't read phpUnit report: " + coverageReportFile.getName(), e);
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

}
