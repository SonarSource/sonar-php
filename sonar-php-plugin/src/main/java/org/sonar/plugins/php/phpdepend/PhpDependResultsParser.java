/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.php.phpdepend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.phpdepend.xml.ClassNode;
import org.sonar.plugins.php.phpdepend.xml.FileNode;
import org.sonar.plugins.php.phpdepend.xml.FunctionNode;
import org.sonar.plugins.php.phpdepend.xml.MethodNode;
import org.sonar.plugins.php.phpdepend.xml.MetricsNode;
import org.sonar.plugins.php.phpdepend.xml.PackageNode;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * The PhpDependResultsParser par pdepend reports files and associate measures with metrics and resources.
 */
public class PhpDependResultsParser implements BatchExtension {

  private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12};
  private static final Number[] CLASSES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

  /**
   * The context.
   */
  private SensorContext context;
  /**
   * The project.
   */
  private Project project;
  private double classComplexity;
  private int numberOfMethods;

  /**
   * Instantiates a new php depend results parser.
   * 
   * @param config
   *          the config
   * @param context
   *          the context
   */
  public PhpDependResultsParser(Project project, SensorContext context) {
    this.project = project;
    this.context = context;
  }

  /**
   * Parses the pdepend report file.
   */
  public void parse(File reportXml) {
    if (!reportXml.exists()) {
      throw new SonarException("PDepdend result file not found: " + reportXml.getAbsolutePath() + ".");
    }

    MetricsNode metricsNode = getMetrics(reportXml);
    List<FileNode> files = metricsNode.getFiles();
    for (FileNode fileNode : files) {
      analyzeFileNode(fileNode);
    }

  }

  protected void analyzeFileNode(FileNode fileNode) {
    String fileName = fileNode.getFileName();
    if (!StringUtils.isEmpty(fileName)) {
      org.sonar.api.resources.File sonarFile = org.sonar.api.resources.File.fromIOFile(new File(fileName), project);
      if (sonarFile != null && !ResourceUtils.isUnitTestClass(sonarFile)) {
        saveMeasures(sonarFile, fileNode);
      }
    }
  }

  protected void saveMeasures(org.sonar.api.resources.File sonarFile, FileNode fileNode) {
    saveSimpleMeasures(sonarFile, fileNode);

    RangeDistributionBuilder classComplexityDistribution = new RangeDistributionBuilder(CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION,
        CLASSES_DISTRIB_BOTTOM_LIMITS);
    RangeDistributionBuilder methodComplexityDistribution = new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION,
        FUNCTIONS_DISTRIB_BOTTOM_LIMITS);

    classComplexity = 0.0;
    numberOfMethods = 0;
    analyseClasses(sonarFile, fileNode, classComplexityDistribution, methodComplexityDistribution);
    analyseFunctions(fileNode, methodComplexityDistribution);

    context.saveMeasure(sonarFile, CoreMetrics.FUNCTIONS, (double) numberOfMethods);
    context.saveMeasure(sonarFile, CoreMetrics.COMPLEXITY, classComplexity);
    context.saveMeasure(sonarFile, classComplexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
    context.saveMeasure(sonarFile, methodComplexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
  }

  protected void analyseFunctions(FileNode fileNode, RangeDistributionBuilder methodComplexityDistribution) {
    if (fileNode.getFunctions() != null) {
      for (FunctionNode funcNode : fileNode.getFunctions()) {
        numberOfMethods += 1;
        classComplexity += funcNode.getComplexity();
        methodComplexityDistribution.add(funcNode.getComplexity());
      }
    }
  }

  protected void analyseClasses(org.sonar.api.resources.File sonarFile, FileNode fileNode,
      RangeDistributionBuilder classComplexityDistribution, RangeDistributionBuilder methodComplexityDistribution) {
    if (fileNode.getClasses() != null) {
      boolean firstClass = true;
      for (ClassNode classNode : fileNode.getClasses()) {
        if (firstClass) {
          // we save the DIT and NumberOfChildren only for the first class, as usually there will be only 1 class per file
          context.saveMeasure(sonarFile, CoreMetrics.DEPTH_IN_TREE, classNode.getDepthInTreeNumber());
          context.saveMeasure(sonarFile, CoreMetrics.NUMBER_OF_CHILDREN, classNode.getNumberOfChildrenClassesNumber());
          firstClass = false;
        }

        double innerClassComplexity = 0.0;
        // for all methods in this class.
        List<MethodNode> methodes = classNode.getMethodes();
        if (methodes != null && !methodes.isEmpty()) {
          for (MethodNode methodNode : methodes) {
            numberOfMethods += 1;
            innerClassComplexity += methodNode.getComplexity();
            methodComplexityDistribution.add(methodNode.getComplexity());
          }
        }

        classComplexityDistribution.add(innerClassComplexity);
        classComplexity += innerClassComplexity;
      }
    }
  }

  protected void saveSimpleMeasures(org.sonar.api.resources.File sonarFile, FileNode fileNode) {
    context.saveMeasure(sonarFile, CoreMetrics.FILES, 1.0);
    context.saveMeasure(sonarFile, CoreMetrics.CLASSES, fileNode.getClassNumber());
    context.saveMeasure(sonarFile, CoreMetrics.LINES, fileNode.getLinesNumber());
    context.saveMeasure(sonarFile, CoreMetrics.NCLOC, fileNode.getCodeLinesNumber());
    context.saveMeasure(sonarFile, CoreMetrics.COMMENT_LINES, fileNode.getCommentLineNumber());
  }

  /**
   * Gets the metrics.
   * 
   * @param report
   *          the report
   * @return the metrics
   */
  private MetricsNode getMetrics(File report) {
    InputStream inputStream = null;
    try {
      XStream xstream = new XStream();
      // Migration Sonar 2.2
      xstream.setClassLoader(getClass().getClassLoader());
      xstream.processAnnotations(MetricsNode.class);
      xstream.processAnnotations(PackageNode.class);
      xstream.processAnnotations(FileNode.class);
      xstream.processAnnotations(ClassNode.class);
      xstream.processAnnotations(FunctionNode.class);
      xstream.processAnnotations(MethodNode.class);
      inputStream = new FileInputStream(report);
      return (MetricsNode) xstream.fromXML(inputStream);
    } catch (XStreamException e) {
      throw new SonarException("PDepend report isn't valid: " + report.getName(), e);
    } catch (IOException e) {
      throw new SonarException("Can't read report : " + report.getName(), e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

}
