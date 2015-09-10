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
package org.sonar.plugins.php.phpunit;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
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
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.phpunit.xml.CoverageNode;
import org.sonar.plugins.php.phpunit.xml.FileNode;
import org.sonar.plugins.php.phpunit.xml.LineNode;
import org.sonar.plugins.php.phpunit.xml.MetricsNode;
import org.sonar.plugins.php.phpunit.xml.PackageNode;
import org.sonar.plugins.php.phpunit.xml.ProjectNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PhpUnitCoverageResultParser.
 */
public class PhpUnitCoverageResultParser implements BatchExtension, PhpUnitParser {

  // Used for debugging purposes to store measure by resource
  private static final Map<Resource, Measure> MEASURES_BY_RESOURCE = new HashMap<Resource, Measure>();

  private static final Logger LOG = LoggerFactory.getLogger(PhpUnitCoverageResultParser.class);
  private final SensorContext context;
  private final FileSystem fileSystem;

  protected Metric lineCoverageMetric = CoreMetrics.LINE_COVERAGE;
  protected Metric linesToCoverMetric = CoreMetrics.LINES_TO_COVER;
  protected Metric uncoveredLinesMetric = CoreMetrics.UNCOVERED_LINES;
  protected Metric coverageLineHitsDataMetric = CoreMetrics.COVERAGE_LINE_HITS_DATA;

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

    List<String> unresolvedPaths = Lists.newArrayList();
    List<ProjectNode> projects = coverage.getProjects();
    if (projects != null && !projects.isEmpty()) {
      ProjectNode projectNode = projects.get(0);
      parseFileNodes(projectNode.getFiles(), unresolvedPaths);
      parsePackagesNodes(projectNode.getPackages(), unresolvedPaths);
      saveMeasureForMissingFiles();
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
  private void saveMeasureForMissingFiles() {
    FilePredicate mainFilesPredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(Php.KEY));

    for (InputFile phpFile : fileSystem.inputFiles(mainFilesPredicate)) {
      org.sonar.api.resources.File resource = org.sonar.api.resources.File.create(phpFile.relativePath());

      if (context.getMeasure(resource, lineCoverageMetric) == null) {
        LOG.debug("Coverage metrics have not been set on '{}': default values will be inserted.", phpFile.file().getName());
        context.saveMeasure(resource, lineCoverageMetric, 0.0);
        // for LINES_TO_COVER and UNCOVERED_LINES, we use NCLOC as an approximation
        Measure ncloc = context.getMeasure(resource, CoreMetrics.NCLOC);

        if (ncloc != null && context.getMeasure(linesToCoverMetric) == null) {
          context.saveMeasure(resource, linesToCoverMetric, ncloc.getValue());
        }

        if (ncloc != null && context.getMeasure(uncoveredLinesMetric) == null) {
          context.saveMeasure(resource, uncoveredLinesMetric, ncloc.getValue());
        }
      }
    }
  }

  private void parsePackagesNodes(List<PackageNode> packages, List<String> unresolvedPaths) {
    if (packages != null) {
      for (PackageNode packageNode : packages) {
        parseFileNodes(packageNode.getFiles(), unresolvedPaths);
      }
    }
  }

  private void parseFileNodes(List<FileNode> fileNodes, List<String> unresolvedPaths) {
    if (fileNodes != null) {
      for (FileNode file : fileNodes) {
        saveCoverageMeasure(file, unresolvedPaths);
      }
    }
  }

  /**
   * Saves the required metrics found on the fileNode
   *
   * @param fileNode the file
   * @param unmappedPaths list of paths which cannot be mapped to imported files
   */
  protected void saveCoverageMeasure(FileNode fileNode, List<String> unresolvedPaths) {
    String path = fileNode.getName();
    //PHP supports only absolute paths
    InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(path));

    // Due to an unexpected behaviour in phpunit.coverage.xml containing references to covered source files, we have to check that the
    // targeted file for coverage is not null.
    if (inputFile != null) {
      org.sonar.api.resources.File phpFile = org.sonar.api.resources.File.create(inputFile.relativePath());
      // Properties builder will generate the data associate with COVERAGE_LINE_HITS_DATA metrics.
      // This should look like (lineNumner=Count) : 1=0;2=1;3=1....
      PropertiesBuilder<Integer, Integer> lineHits = new PropertiesBuilder<Integer, Integer>(coverageLineHitsDataMetric);
      if (fileNode.getLines() != null) {
        for (LineNode line : fileNode.getLines()) {
          saveLineMeasure(line, lineHits);
        }
      }
      MetricsNode metrics = fileNode.getMetrics();
      Measure measure = lineHits.build();
      logMeasureByResource(phpFile, measure);
      context.saveMeasure(phpFile, measure);

      // Save uncovered statements (lines)
      double totalStatementsCount = metrics.getTotalStatementsCount();
      double uncoveredLines = totalStatementsCount - metrics.getCoveredStatements();
      double lineCoverage = 0;
      if (Double.doubleToRawLongBits(totalStatementsCount) != 0) {
        lineCoverage = metrics.getCoveredStatements() / totalStatementsCount;
      }

      context.saveMeasure(phpFile, linesToCoverMetric, totalStatementsCount);
      context.saveMeasure(phpFile, this.uncoveredLinesMetric, uncoveredLines);
      context.saveMeasure(phpFile, this.lineCoverageMetric, ParsingUtils.scaleValue(lineCoverage * 100.0));
    } else {
      unresolvedPaths.add(path);
    }
  }

  private void logMeasureByResource(Resource resource, Measure measure) {
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
  private void saveLineMeasure(LineNode line, PropertiesBuilder<Integer, Integer> lineHits) {
    lineHits.add(line.getNum(), line.getCount());
  }

  /**
   * Gets the coverage.
   *
   * @param coverageReportFile the coverage report file
   * @return the coverage
   */
  private CoverageNode getCoverage(File coverageReportFile) {
    InputStream inputStream = null;
    try {
      XStream xstream = new XStream();
      xstream.setClassLoader(getClass().getClassLoader());
      xstream.aliasSystemAttribute("classType", "class");
      xstream.processAnnotations(CoverageNode.class);
      xstream.processAnnotations(ProjectNode.class);
      xstream.processAnnotations(FileNode.class);
      xstream.processAnnotations(MetricsNode.class);
      xstream.processAnnotations(LineNode.class);
      inputStream = new FileInputStream(coverageReportFile);
      return (CoverageNode) xstream.fromXML(inputStream);
    } catch (IOException e) {
      throw new SonarException("Can't read phpUnit report: " + coverageReportFile.getName(), e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  @Override
  public String toString() {
    return "PHPUnit Unit Test Coverage Result Parser";
  }

}
