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
package org.sonar.plugins.php.phpunit;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.phpunit.xml.CoverageNode;
import org.sonar.plugins.php.phpunit.xml.FileNode;
import org.sonar.plugins.php.phpunit.xml.LineNode;
import org.sonar.plugins.php.phpunit.xml.MetricsNode;
import org.sonar.plugins.php.phpunit.xml.PackageNode;
import org.sonar.plugins.php.phpunit.xml.ProjectNode;

/**
 * The Class PhpUnitCoverageResultParser.
 */
public class PhpUnitCoverageResultParser implements BatchExtension, PhpUnitParser {

  // Used for debugging purposes to store measure by resource
  private static final Map<Resource, Measure> MEASURES_BY_RESOURCE = new HashMap<>();

  private static final Logger LOG = LoggerFactory.getLogger(PhpUnitCoverageResultParser.class);
  private final SensorContext context;
  private final FileSystem fileSystem;

  protected Metric<Integer> linesToCoverMetric = CoreMetrics.LINES_TO_COVER;
  protected Metric<Integer> uncoveredLinesMetric = CoreMetrics.UNCOVERED_LINES;
  protected Metric<String> coverageLineHitsDataMetric = CoreMetrics.COVERAGE_LINE_HITS_DATA;

  private static final String WRONG_LINE_EXCEPTION_MESSAGE = "Line with number %s doesn't belong to file %s";

  /**
   * Instantiates a new php unit coverage result parser.
   *
   * @param context the context
   */
  public PhpUnitCoverageResultParser(SensorContext context, FileSystem fileSystem) {
    super();
    this.context = context;
    this.fileSystem = fileSystem;
  }

  /**
   * Parses PHPUnit coverage file.
   *
   * @param coverageReportFile the coverage report file
   */
  @Override
  public void parse(File coverageReportFile) {
    LOG.debug("Parsing file: " + coverageReportFile.getAbsolutePath());
    parseFile(coverageReportFile);
  }

  /**
   * Parses the file.
   *
   * @param coverageReportFile the coverage report file
   */
  private void parseFile(File coverageReportFile) {
    CoverageNode coverage = getCoverage(coverageReportFile);

    List<String> unresolvedPaths = new ArrayList<>();
    List<String> resolvedPaths = new ArrayList<>();
    List<ProjectNode> projects = coverage.getProjects();
    if (projects != null && !projects.isEmpty()) {
      ProjectNode projectNode = projects.get(0);
      parseFileNodes(projectNode.getFiles(), unresolvedPaths, resolvedPaths);
      parsePackagesNodes(projectNode.getPackages(), unresolvedPaths, resolvedPaths);
      saveMeasureForMissingFiles(resolvedPaths);
    }
    if (!unresolvedPaths.isEmpty()) {
      LOG.warn(
        String.format(
          "Could not resolve %d file paths in %s, first unresolved path: %s",
          unresolvedPaths.size(), coverageReportFile.getName(), unresolvedPaths.get(0)));
    }
  }

  /**
   * Set default 0 value for files that do not have coverage metrics because they were not touched by any test,
   * and thus not present in the coverage report file.
   */
  private void saveMeasureForMissingFiles(List<String> resolvedPaths) {
    FilePredicate mainFilesPredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(Php.KEY));

    for (InputFile phpFile : fileSystem.inputFiles(mainFilesPredicate)) {
      if (!resolvedPaths.contains(phpFile.relativePath())) {
        org.sonar.api.resources.File resource = org.sonar.api.resources.File.create(phpFile.relativePath());
        LOG.debug("Coverage metrics have not been set on '{}': default values will be inserted.", phpFile.file().getName());

        // for LINES_TO_COVER and UNCOVERED_LINES, we use NCLOC as an approximation
        Measure<Integer> ncloc = context.getMeasure(resource, CoreMetrics.NCLOC);
        if (ncloc != null) {
          context.saveMeasure(resource, linesToCoverMetric, ncloc.getValue());
          context.saveMeasure(resource, uncoveredLinesMetric, ncloc.getValue());
        }

      }
    }
  }

  private void parsePackagesNodes(List<PackageNode> packages, List<String> unresolvedPaths, List<String> resolvedPaths) {
    if (packages != null) {
      for (PackageNode packageNode : packages) {
        parseFileNodes(packageNode.getFiles(), unresolvedPaths, resolvedPaths);
      }
    }
  }

  private void parseFileNodes(List<FileNode> fileNodes, List<String> unresolvedPaths, List<String> resolvedPaths) {
    if (fileNodes != null) {
      for (FileNode file : fileNodes) {
        saveCoverageMeasure(file, unresolvedPaths, resolvedPaths);
      }
    }
  }

  /**
   * Saves the required metrics found on the fileNode
   *
   * @param fileNode the file
   * @param unresolvedPaths list of paths which cannot be mapped to imported files
   * @param resolvedPaths list of paths which can be mapped to imported files
   */
  protected void saveCoverageMeasure(FileNode fileNode, List<String> unresolvedPaths, List<String> resolvedPaths) {
    //PHP supports only absolute paths
    String path = fileNode.getName();
    InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(path));

    // Due to an unexpected behaviour in phpunit.coverage.xml containing references to covered source files, we have to check that the
    // targeted file for coverage is not null.
    if (inputFile != null) {
      org.sonar.api.resources.File phpFile = org.sonar.api.resources.File.create(inputFile.relativePath());
      resolvedPaths.add(inputFile.relativePath());
      saveCoverageLineHitsData(fileNode, inputFile, phpFile);

      // Save uncovered statements (lines)
      MetricsNode metrics = fileNode.getMetrics();
      double totalStatementsCount = metrics.getTotalStatementsCount();
      double uncoveredLines = totalStatementsCount - metrics.getCoveredStatements();
      context.saveMeasure(phpFile, linesToCoverMetric, totalStatementsCount);
      context.saveMeasure(phpFile, this.uncoveredLinesMetric, uncoveredLines);
    } else {
      unresolvedPaths.add(path);
    }
  }

  private void saveCoverageLineHitsData(FileNode fileNode, InputFile inputFile, org.sonar.api.resources.File phpFile) {
    // Properties builder will generate the data associate with COVERAGE_LINE_HITS_DATA metrics.
    // This should look like (lineNumber=Count) : 1=0;2=1;3=1....
    PropertiesBuilder<Integer, Integer> lineHits = new PropertiesBuilder<>(coverageLineHitsDataMetric);
    if (fileNode.getLines() != null) {
      for (LineNode line : fileNode.getLines()) {
        int lineNum = line.getNum();
        if (lineNum > 0 && lineNum <= inputFile.lines()) {
          saveLineMeasure(line, lineHits);
        } else {
          LOG.warn(String.format(WRONG_LINE_EXCEPTION_MESSAGE, lineNum, phpFile.getLongName()));
        }
      }
    }
    Measure measure = lineHits.build();
    logMeasureByResource(phpFile, measure);
    context.saveMeasure(phpFile, measure);
  }

  private static void logMeasureByResource(Resource resource, Measure measure) {
    if (LOG.isDebugEnabled()) {
      Measure alreadySaved = MEASURES_BY_RESOURCE.get(resource);
      if (alreadySaved == null) {
        MEASURES_BY_RESOURCE.put(resource, measure);
      } else {
        LOG.debug("Measure {} already saved for resource {}", measure, resource);
      }
    }
  }

  /**
   * Save line measure.
   *
   * @param line     the line
   * @param lineHits the line hits
   */
  private static void saveLineMeasure(LineNode line, PropertiesBuilder<Integer, Integer> lineHits) {
    lineHits.add(line.getNum(), line.getCount());
  }

  /**
   * Gets the coverage.
   *
   * @param coverageReportFile the coverage report file
   * @return the coverage
   */
  private CoverageNode getCoverage(File coverageReportFile) {
    try (InputStream inputStream = new FileInputStream(coverageReportFile);){
      XStream xstream = new XStream();
      xstream.setClassLoader(getClass().getClassLoader());
      xstream.aliasSystemAttribute("classType", "class");
      xstream.processAnnotations(CoverageNode.class);
      xstream.processAnnotations(ProjectNode.class);
      xstream.processAnnotations(FileNode.class);
      xstream.processAnnotations(MetricsNode.class);
      xstream.processAnnotations(LineNode.class);

      return (CoverageNode) xstream.fromXML(inputStream);
    } catch (IOException e) {
      throw new IllegalStateException("Can't read phpUnit report: " + coverageReportFile.getName(), e);
    }
  }

  @Override
  public String toString() {
    return "PHPUnit Unit Test Coverage Result Parser";
  }

}
