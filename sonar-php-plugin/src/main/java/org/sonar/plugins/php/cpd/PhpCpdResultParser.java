/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

package org.sonar.plugins.php.cpd;

import static org.sonar.api.measures.CoreMetrics.DUPLICATED_BLOCKS;
import static org.sonar.api.measures.CoreMetrics.DUPLICATED_FILES;
import static org.sonar.api.measures.CoreMetrics.DUPLICATED_LINES;
import static org.sonar.api.measures.CoreMetrics.DUPLICATIONS_DATA;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.PhpFile;
import org.sonar.plugins.php.cpd.xml.DuplicationNode;
import org.sonar.plugins.php.cpd.xml.FileNode;
import org.sonar.plugins.php.cpd.xml.PmdCpdNode;

import com.thoughtworks.xstream.XStream;

/**
 * Parses the php-cpd file to get xopy paste information metrics.
 * 
 * @author akram
 * 
 */
@SuppressWarnings("rawtypes")
public class PhpCpdResultParser implements BatchExtension {

  private static final Logger LOG = LoggerFactory.getLogger(PhpCpdResultParser.class);
  /**
   * The context.
   */
  private SensorContext context;

  /**
   * The project.
   */
  private Project project;

  /**
   * @param project
   * @param context
   */
  public PhpCpdResultParser(Project project, SensorContext context) {
    this.project = project;
    this.context = context;
  }

  /**
   * Parses the report file.
   */
  public void parse(File file) {
    // If no files can be found, plugin will stop normally only logging the error
    if ( !file.exists()) {
      LOG.error("Result file not found : " + file.getAbsolutePath() + ". Plugin will stop");
      return;
    }
    try {
      LOG.info("Collecting measures...");
      parseFile(context, file, project);
    } catch (Exception e) {
      LOG.error("Report file is invalid or can't be found, plugin will stop.", e);
      throw new SonarException(e);
    }
  }

  /**
   * Parses the file.
   * 
   * @param context
   *          the context
   * @param file
   *          the coverage report file
   * @param project
   *          the project
   */
  private void parseFile(SensorContext context, File file, Project project) {
    List<DuplicationNode> duplications = getDuplications(file);
    Map<Resource, ClassDuplicationData> duplicationsData = new HashMap<Resource, ClassDuplicationData>();

    for (DuplicationNode duplication : duplications) {
      List<FileNode> files = duplication.getFiles();
      FileNode fileA = files.get(0);
      FileNode fileB = files.get(1);
      processClassMeasure(context, duplicationsData, fileB, fileA, duplication, project);
      processClassMeasure(context, duplicationsData, fileA, fileB, duplication, project);
    }

    for (ClassDuplicationData data : duplicationsData.values()) {
      data.saveUsing(context);
    }
  }

  /**
   * @param context
   * @param duplicationsData
   * @param original
   * @param copied
   * @param duplication
   * @param project
   */
  private void processClassMeasure(SensorContext context, Map<Resource, ClassDuplicationData> duplicationsData, FileNode original,
      FileNode copied, DuplicationNode duplication, Project project) {
    PhpFile instance = PhpFile.getInstance(project);
    Resource file = instance.fromAbsolutePath(copied.getPath(), project.getFileSystem().getSourceDirs(), false);
    Resource targetPhpClass = instance.fromAbsolutePath(original.getPath(), project.getFileSystem().getSourceDirs(), false);
    if (file != null) {
      ClassDuplicationData data = duplicationsData.get(file);
      if (data == null) {
        data = new ClassDuplicationData(file, context);
        duplicationsData.put(file, data);
      }
      data.cumulate(targetPhpClass, original.getLineNumber(), copied.getLineNumber(), duplication.getLines());
    }

  }

  /**
   * Gets the duplications.
   * 
   * @param reportFile
   *          the report file
   * @return the coverage
   */
  private List<DuplicationNode> getDuplications(File reportFile) {
    InputStream inputStream = null;
    try {
      XStream xstream = new XStream();
      xstream.setClassLoader(getClass().getClassLoader());
      xstream.aliasSystemAttribute("classType", "class");
      xstream.processAnnotations(PmdCpdNode.class);
      xstream.processAnnotations(DuplicationNode.class);
      xstream.processAnnotations(FileNode.class);
      inputStream = new FileInputStream(reportFile);
      PmdCpdNode node = (PmdCpdNode) xstream.fromXML(inputStream);
      if (node.getDuplications() != null) {
        return node.getDuplications();
      }
    } catch (IOException e) {
      throw new SonarException("Can't read phpcpd report : " + reportFile.getName(), e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return new ArrayList<DuplicationNode>();
  }

  /**
   * Models duplication.
   * 
   * @author akram
   * 
   */
  private static final class ClassDuplicationData {

    private static final String SONAR_DUPLICATION_CLOSING_TAG = "\"/>";
    private static final String SONAR_TARGET_RESOURCE_ATTRIBUTE = "\" target-resource=\"";
    private static final String SONAR_DUPLICATION_DUPLICATION_LINES_TAG = "<duplication lines=\"";
    private static final String SONAR_DUPLICATION_TARGET_START_TAG = "\" target-start=\"";
    private static final String SONAR_DUPLICATION_START_ATTRIBUTE = "\" start=\"";
    private static final String SONAR_DUPLICATIONS_END_TAG = "</duplications>";
    private static final String SONAR_DUPLICATION_START_TAG = "<duplications>";
    private double duplicatedLines;
    private double duplicatedBlocks;

    private Resource resource;
    private SensorContext context;
    private List<StringBuilder> duplicationXMLEntries = new ArrayList<StringBuilder>();

    /**
     * Constructor.
     * 
     * @param resource
     * @param context
     */
    private ClassDuplicationData(Resource resource, SensorContext context) {
      this.context = context;
      this.resource = resource;
    }

    /**
     * Build XML for sonar dashboard to display duplicated code.
     * 
     * @param target
     * @param targetDuplicationStartLine
     * @param duplicationStartLine
     * @param duplicatedLines
     */

    protected void cumulate(Resource target, Double targetDuplicationStartLine, Double duplicationStartLine, Double duplicatedLines) {
      Resource resolvedResource = context.getResource(target);
      if (resolvedResource != null) {
        StringBuilder xml = new StringBuilder(SONAR_DUPLICATION_DUPLICATION_LINES_TAG);
        xml.append(duplicatedLines.intValue()).append(SONAR_DUPLICATION_START_ATTRIBUTE).append(duplicationStartLine.intValue());
        xml.append(SONAR_DUPLICATION_TARGET_START_TAG).append(targetDuplicationStartLine.intValue());
        xml.append(SONAR_TARGET_RESOURCE_ATTRIBUTE).append(resolvedResource.getEffectiveKey()).append(SONAR_DUPLICATION_CLOSING_TAG);

        duplicationXMLEntries.add(xml);
        this.duplicatedLines += duplicatedLines;
        this.duplicatedBlocks++;
      }
    }

    /**
     * Save the metrics values and the duplicated code as XML.
     * 
     * @param context
     */
    protected void saveUsing(SensorContext context) {
      context.saveMeasure(resource, DUPLICATED_FILES, 1d);
      context.saveMeasure(resource, DUPLICATED_LINES, duplicatedLines);
      context.saveMeasure(resource, DUPLICATED_BLOCKS, duplicatedBlocks);
      context.saveMeasure(resource, new Measure(DUPLICATIONS_DATA, getDuplicationXMLData()));
    }

    private String getDuplicationXMLData() {
      StringBuilder duplicationXML = new StringBuilder(SONAR_DUPLICATION_START_TAG);
      for (StringBuilder xmlEntry : duplicationXMLEntries) {
        duplicationXML.append(xmlEntry);
      }
      duplicationXML.append(SONAR_DUPLICATIONS_END_TAG);
      return duplicationXML.toString();
    }
  }

}
