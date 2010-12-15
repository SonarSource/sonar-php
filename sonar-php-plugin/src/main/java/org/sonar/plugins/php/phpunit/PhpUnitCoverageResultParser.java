/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 EchoSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.phpunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.PhpFile;
import org.sonar.plugins.php.core.PhpPackage;
import org.sonar.plugins.php.phpunit.xml.CoverageNode;
import org.sonar.plugins.php.phpunit.xml.FileNode;
import org.sonar.plugins.php.phpunit.xml.LineNode;
import org.sonar.plugins.php.phpunit.xml.MetricsNode;
import org.sonar.plugins.php.phpunit.xml.ProjectNode;

import com.thoughtworks.xstream.XStream;

/**
 * The Class PhpUnitCoverageResultParser.
 */
public class PhpUnitCoverageResultParser implements BatchExtension {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(PhpUnitCoverageResultParser.class);

  /** The project. */
  private Project project;

  /** The context. */
  private SensorContext context;

  /** The class by package. */
  private Map<String, PackageNode> classByPackage;

  /**
   * Instantiates a new php unit coverage result parser.
   * 
   * @param project
   *          the project
   * @param context
   *          the context
   */
  public PhpUnitCoverageResultParser(Project project, SensorContext context) {
    super();
    this.project = project;
    this.context = context;
    classByPackage = new HashMap<String, PackageNode>();
  }

  /**
   * Parses the.
   * 
   * @param coverageReportFile
   *          the coverage report file
   */
  public void parse(File coverageReportFile) {
    if (coverageReportFile == null) {
      insertZeroWhenNoReports();
    } else {
      logger.info("Parsing file : {0}", coverageReportFile.getName());
      parseFile(context, coverageReportFile);
    }
  }

  /**
   * Insert zero when no reports.
   */
  private void insertZeroWhenNoReports() {
    context.saveMeasure(CoreMetrics.COVERAGE, 0d);
  }

  /**
   * Parses the file.
   * 
   * @param context
   *          the context
   * @param coverageReportFile
   *          the coverage report file
   * @param project
   *          the project
   */
  private void parseFile(SensorContext context, File coverageReportFile) {
    CoverageNode coverage = getCoverage(coverageReportFile);
    // Used to record the number of covered statements (lines)
    double totalStatements = 0;
    double coveredStatements = 0;

    List<ProjectNode> projects = coverage.getProjects();
    if (projects != null && !projects.isEmpty()) {
      ProjectNode projectNode = projects.get(0);
      for (FileNode file : projectNode.getFiles()) {
        cumulateResults(file);
      }
      MetricsNode metrics = projectNode.getMetrics();
      totalStatements += metrics.getTotalStatementsCount();
      coveredStatements += metrics.getCoveredStatements();
      saveMeasures(classByPackage);
    }
    context.saveMeasure(CoreMetrics.LINES_TO_COVER, totalStatements);
    context.saveMeasure(CoreMetrics.UNCOVERED_LINES, totalStatements - coveredStatements);
  }

  /**
   * Cumulate results.
   * 
   * @param file
   *          the file
   */
  private void cumulateResults(FileNode file) {
    PhpFile phpFile = PhpFile.fromAbsolutePath(file.getName(), project);
    if (phpFile == null) {
      logger.info(file.getName() + " can't be found amoung the project source directories");
      return;
    }
    PhpPackage phpPackage = phpFile.getParent();
    PackageNode packageNode = classByPackage.get(phpPackage.getName());
    if (packageNode == null) {
      packageNode = new PackageNode();
      classByPackage.put(phpPackage.getName(), packageNode);
    }
    packageNode.addClassByFileNode(file, phpFile);
  }

  /**
   * Save measures for all classes.
   * 
   * @param classByPackage
   *          the class by package
   * @return the double
   */
  private void saveMeasures(Map<String, PackageNode> classByPackage) {
    // For each package
    Collection<PackageNode> nodes = classByPackage.values();
    for (PackageNode node : nodes) {
      // Saves the class measures and adds its coverage percent
      Map<FileNode, PhpFile> classByFileNode = node.getClassByFileNode();
      for (Entry<FileNode, PhpFile> entry : classByFileNode.entrySet()) {
        saveClassMeasure(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Saves one class measure and calls {@link #saveLineMeasure(LineNode, PropertiesBuilder, String))} for each class lines.
   * 
   * @param fileNode
   *          the file node
   * @param phpFile
   *          the php file
   * @return the double
   */
  private void saveClassMeasure(FileNode fileNode, PhpFile phpFile) {
    // Properties builder will generate the data associate with COVERAGE_LINE_HITS_DATA metrics.
    // This should look like (lineNumner=Count) : 1=0;2=1;3=1....
    PropertiesBuilder<Integer, Integer> lineHits = new PropertiesBuilder<Integer, Integer>(CoreMetrics.COVERAGE_LINE_HITS_DATA);
    for (LineNode line : fileNode.getLines()) {
      saveLineMeasure(line, lineHits);
    }
    MetricsNode metrics = fileNode.getMetrics();
    context.saveMeasure(phpFile, lineHits.build());

    // Save uncovered statements (lines)
    double totalStatementsCount = metrics.getTotalStatementsCount();
    double uncoveredLines = totalStatementsCount - metrics.getCoveredStatements();
    context.saveMeasure(phpFile, CoreMetrics.LINES_TO_COVER, totalStatementsCount);
    context.saveMeasure(phpFile, CoreMetrics.UNCOVERED_LINES, uncoveredLines);
  }

  /**
   * Save line measure.
   * 
   * @param line
   *          the line
   * @param lineHits
   *          the line hits
   * @param fileName
   *          the class name
   */
  private void saveLineMeasure(LineNode line, PropertiesBuilder<Integer, Integer> lineHits) {
    lineHits.add(line.getNum(), line.getCount());
  }

  /**
   * Gets the coverage.
   * 
   * @param coverageReportFile
   *          the coverage report file
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
      throw new SonarException("Can't read pUnit report : " + coverageReportFile.getName(), e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * The Class PackageNode.
   */
  private static class PackageNode {

    /** The class by file node. */
    private Map<FileNode, PhpFile> classByFileNode;

    /**
     * Gets the class by file node.
     * 
     * @return the class by file node
     */
    public Map<FileNode, PhpFile> getClassByFileNode() {
      return classByFileNode;
    }

    /**
     * Instantiates a new package node.
     * 
     * @param phpPackage
     *          the php package
     */
    public PackageNode() {
      super();
      classByFileNode = new HashMap<FileNode, PhpFile>();
    }

    /**
     * Adds the class by file node.
     * 
     * @param fileNode
     *          the file node
     * @param phpFile
     *          the php file
     */
    public void addClassByFileNode(FileNode fileNode, PhpFile phpFile) {
      classByFileNode.put(fileNode, phpFile);
    }
  }

}
