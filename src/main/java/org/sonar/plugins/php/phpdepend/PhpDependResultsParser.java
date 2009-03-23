/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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

package org.sonar.plugins.php.phpdepend;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.commons.Metric;
import org.sonar.commons.resources.Resource;
import org.sonar.plugins.api.maven.MavenCollectorUtils;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.maven.xml.XmlParserException;
import org.sonar.plugins.api.metrics.CoreMetrics;
import org.sonar.plugins.php.Php;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class PhpDependResultsParser {

  private static final Logger LOG = LoggerFactory.getLogger(PhpDependResultsParser.class);

  private PhpDependConfiguration config;
  private ProjectContext context;
  private List<String> sourcesDir;
  private ResourcesManager resourcesManager;

  public PhpDependResultsParser(PhpDependConfiguration config, ProjectContext context) {
    this.config = config;
    this.context = context;
    this.sourcesDir = Arrays.asList(config.getSourceDir().getAbsolutePath());
    resourcesManager = new ResourcesManager();
  }

  protected PhpDependResultsParser(PhpDependConfiguration config, ProjectContext context, List<String> sourcesDir) {
    this.config = config;
    this.context = context;
    this.sourcesDir = sourcesDir;
    resourcesManager = new ResourcesManager();
  }

  public void parse() {
    File reportXml = config.getReportFile();
    if (!reportXml.exists()) {
      throw new PhpDependExecutionException("Result file not found : " + reportXml.getAbsolutePath());
    }
    try {
      LOG.info("Collecting measures...");
      collectMeasures(reportXml);
    } catch (Exception e) {
      throw new XmlParserException(e);
    }
  }

  protected void collectMeasures(File reportXml) throws Exception {
    XMLInputFactory2 xmlFactory = (XMLInputFactory2) XMLInputFactory2.newInstance();
    InputStream input = new FileInputStream(reportXml);
    XMLStreamReader2 reader = (XMLStreamReader2) xmlFactory.createXMLStreamReader(input);

    Resource currentResourceFile = null;
    while (reader.next() != XMLStreamConstants.END_DOCUMENT) {
      if (reader.isStartElement()) {
        String elementName = reader.getLocalName();
        if (elementName.equals("metrics")) {
          collectMeasuresFromMetricsTag(reader);
        } else if (elementName.equals("file")) {
          String filePath = reader.getAttributeValue(null, "name");
          currentResourceFile = Php.newFileFromAbsolutePath(filePath, sourcesDir);
          collectMeasuresFromFileTag(reader, currentResourceFile);
        } else if (elementName.equals("class")) {
          collectMeasuresFromClassTag(reader, currentResourceFile);
        } else if (elementName.equals("function")) {
          collectMeasuresFromFunctionTag(reader, currentResourceFile);
        }
      }
    }
    reader.closeCompletely();

    saveMeasures();
  }

  private void collectMeasuresFromMetricsTag(XMLStreamReader2 reader) throws ParseException, XMLStreamException {
    Double loc = addBasicMeasureFromAttribute(reader, null, CoreMetrics.LOC, "loc");
    addBasicMeasureFromAttribute(reader, null, CoreMetrics.NCLOC, "locExecutable");
    addBasicMeasureFromAttribute(reader, null, CoreMetrics.FUNCTIONS_COUNT, "nom");
    Double cloc = addBasicMeasureFromAttribute(reader, null, CoreMetrics.COMMENT_LINES, "cloc");
    addBasicMeasureFromAttribute(reader, null, CoreMetrics.FILES_COUNT, "files");
    addBasicMeasureFromAttribute(reader, null, CoreMetrics.COMPLEXITY, "ccn");

    collectProjectClassesMeasure(reader, null);
    addCommentRatioMeasure(null, loc, cloc);
  }

  private void collectMeasuresFromFileTag(XMLStreamReader2 reader, Resource file) throws ParseException, XMLStreamException {
    Double loc = addBasicMeasureFromAttribute(reader, file, CoreMetrics.LOC, "loc");
    addBasicMeasureFromAttribute(reader, file, CoreMetrics.NCLOC, "locExecutable");
    Double cloc = addBasicMeasureFromAttribute(reader, file, CoreMetrics.COMMENT_LINES, "cloc");
    addBasicMeasureFromAttribute(reader, file, CoreMetrics.CLASSES_COUNT, "classes");

    addFileMeasure(file, CoreMetrics.FILES_COUNT, 1.0);
    addCommentRatioMeasure(file, loc, cloc);
  }

  private void collectMeasuresFromClassTag(XMLStreamReader2 reader, Resource file) throws ParseException {
    String value = reader.getAttributeValue(null, "nom");
    addFileMeasure(file, CoreMetrics.FUNCTIONS_COUNT, value);
  }

  private void collectMeasuresFromFunctionTag(XMLStreamReader2 reader, Resource file) throws ParseException {
    addFileMeasure(file, CoreMetrics.FUNCTIONS_COUNT, 1.0);
  }

  private void collectProjectClassesMeasure(XMLStreamReader2 reader, Resource resource) throws ParseException {
    String classes = reader.getAttributeValue(null, "classes");
    String interfaces = reader.getAttributeValue(null, "interfs");
    Double classesCount = extractValue(classes) + extractValue(interfaces);
    addProjectMeasure(CoreMetrics.CLASSES_COUNT, classesCount);
  }

  private void addCommentRatioMeasure(Resource resource, Double loc, Double cloc) throws ParseException {
    Double commentRatio = cloc / loc * 100;
    addBasicMeasure(resource, CoreMetrics.COMMENT_RATIO, commentRatio);
  }


  private void saveMeasures() throws ParseException {
    for (Resource resource : resourcesManager.getResources()) {
      for (Metric metric : resourcesManager.getMetrics(resource)) {
        Double measure = resourcesManager.getMeasure(metric, resource);
        if (resource != null) {
          context.addMeasure(resource, metric, measure);
        } else {
          context.addMeasure(metric, measure);
        }
      }
    }
  }

  private Double addBasicMeasureFromAttribute(XMLStreamReader2 reader, Resource resource, Metric metric, String attribute) throws ParseException {
    String value = reader.getAttributeValue(null, attribute);
    if (value != null) {
      Double doubleValue = extractValue(value);
      addBasicMeasure(resource, metric, doubleValue);
      return doubleValue;
    }
    return null;
  }

  private void addBasicMeasure(Resource resource, Metric metric, Double value) throws ParseException {
    if (resource != null) {
      addFileMeasure(resource, metric, value);
    } else {
      addProjectMeasure(metric, value);
    }
  }

  private void addProjectMeasure(Metric metric, String value) throws ParseException {
    resourcesManager.addProject(extractValue(value), metric);
  }

  private void addProjectMeasure(Metric metric, Double value) throws ParseException {
    resourcesManager.addProject(value, metric);
  }

  private void addFileMeasure(Resource file, Metric metric, Double value) throws ParseException {
    resourcesManager.addFile(value, metric, file);
  }

  private void addFileMeasure(Resource file, Metric metric, String value) throws ParseException {
    resourcesManager.addFile(extractValue(value), metric, file);
  }

  private double extractValue(String value) throws ParseException {
    return MavenCollectorUtils.parseNumber(value);
  }

}