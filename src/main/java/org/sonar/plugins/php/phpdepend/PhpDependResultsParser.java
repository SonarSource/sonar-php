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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhpDependResultsParser {

  private static final Logger LOG = LoggerFactory.getLogger(PhpDependResultsParser.class);

  private PhpDependConfiguration config;
  private ProjectContext context;
  private List<String> sourcesDir;
  private DirectoryMeasures directoryMeasures;
  private Map<Metric, String> attributeByMetrics;

  public PhpDependResultsParser(PhpDependConfiguration config, ProjectContext context) {
    this.config = config;
    this.context = context;
    this.sourcesDir = Arrays.asList(config.getSourceDir().getAbsolutePath());
    directoryMeasures = new DirectoryMeasures();
    initAttributeByMetrics();
  }

  protected PhpDependResultsParser(PhpDependConfiguration config, ProjectContext context, Map<Metric, String> attributeByMetrics, List<String> sourcesDir) {
    this.config = config;
    this.context = context;
    this.attributeByMetrics = attributeByMetrics;
    this.sourcesDir = sourcesDir;
    directoryMeasures = new DirectoryMeasures();
    attributeByMetrics = new HashMap<Metric, String>();
  }

  private void initAttributeByMetrics() {
    attributeByMetrics = new HashMap<Metric, String>();
    attributeByMetrics.put(CoreMetrics.LOC, "loc");
    attributeByMetrics.put(CoreMetrics.NCLOC, "locExecutable");
    attributeByMetrics.put(CoreMetrics.COMMENT_LINES, "cloc");
    attributeByMetrics.put(CoreMetrics.CLASSES_COUNT, "classes");
    attributeByMetrics.put(CoreMetrics.FUNCTIONS_COUNT, "nom");
    attributeByMetrics.put(CoreMetrics.FILES_COUNT, "files");
    attributeByMetrics.put(CoreMetrics.COMPLEXITY, "ccn");
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

    while (reader.next() != XMLStreamConstants.END_DOCUMENT) {
      if (reader.isStartElement()) {
        String elementName = reader.getLocalName();
        if (elementName.equals("metrics")) {
          collectProjectMeasures(reader);
        } else if (elementName.equals("file")) {
          collectFileMeasures(reader);
        }
      }
    }
    reader.closeCompletely();

    collectDirectoryMeasures();
  }

  private void collectProjectMeasures(XMLStreamReader2 reader) throws ParseException {
    for (Map.Entry<Metric, String> attributeByMetric : attributeByMetrics.entrySet()) {
      Metric metric = attributeByMetric.getKey();
      String attribute = attributeByMetric.getValue();
      String value = reader.getAttributeValue(null, attribute);
      // Classes must not be added to the project measures now because we must also add the interfaces
      if (value != null && metric != CoreMetrics.CLASSES_COUNT) {
        addProjectMeasure(metric, value);
      }
    }
    collectProjectClassesMeasure(reader);
  }

  private void collectProjectClassesMeasure(XMLStreamReader2 reader) throws ParseException {
    String classes = reader.getAttributeValue(null, "classes");
    String interfaces = reader.getAttributeValue(null, "interfs");
    Double classesCount = extractValue(classes) + extractValue(interfaces);
    addProjectMeasure(CoreMetrics.CLASSES_COUNT, classesCount);
  }

  private void collectFileMeasures(XMLStreamReader2 reader) throws ParseException, XMLStreamException {
    String name = reader.getAttributeValue(null, "name");
    Resource file = Php.newFileFromAbsolutePath(name, sourcesDir);

    for (Map.Entry<Metric, String> attributeByMetric : attributeByMetrics.entrySet()) {
      Metric metric = attributeByMetric.getKey();
      String attribute = attributeByMetric.getValue();

      String value = reader.getAttributeValue(null, attribute);
      if (value != null) {
        addFileMeasure(file, metric, value);
      }
    }
    collectFunctionsCount(file, reader);
    collectFilesCountMeasure(file);
  }

  private void collectFunctionsCount(Resource file, XMLStreamReader2 reader) throws XMLStreamException, ParseException {
    Double functionCount = 0.0;
    String elementName;
    boolean isNotAtFileEndTag = reader.next() != XMLStreamConstants.END_DOCUMENT;
    while (isNotAtFileEndTag) {
      if (reader.isStartElement()) {
        elementName = reader.getLocalName();
        if (elementName.equals("class")) {
          String value = reader.getAttributeValue(null, "nom");
          functionCount += extractValue(Integer.parseInt(value));
          reader.skipElement();
        } else if (elementName.equals("function")) {
          functionCount += 1;
          reader.skipElement();
        }
      } else if (reader.isEndElement()) {
        elementName = reader.getLocalName();
        if (elementName.equals("file")) {
          isNotAtFileEndTag = false;
        }
      }
      isNotAtFileEndTag &= reader.next() != XMLStreamConstants.END_DOCUMENT;
    }
    addFileMeasure(file, CoreMetrics.FUNCTIONS_COUNT, functionCount);
  }

  private void collectDirectoryMeasures() throws ParseException {
    for (Metric metric : directoryMeasures.getKeys()) {
      for (Resource resource : directoryMeasures.getResources(metric)) {
        Double measure = directoryMeasures.getMeasure(metric, resource);
        context.addMeasure(resource, metric, measure);
      }
    }
  }

  private void addProjectMeasure(Metric metric, String value) throws ParseException {
    context.addMeasure(metric, extractValue(value));
  }

  private void addProjectMeasure(Metric metric, Double value) throws ParseException {
    context.addMeasure(metric, value);
  }

  private void addFileMeasure(Resource file, Metric metric, Double value) throws ParseException {
    context.addMeasure(file, metric, value);
    addFileMeasureToParent(file, metric, value);
  }

  private void addFileMeasure(Resource file, Metric metric, String value) throws ParseException {
    addFileMeasure(file, metric, extractValue(value));
  }

  private void addFileMeasureToParent(Resource file, Metric metric, Double value) {
    Resource parent = new Php().getParent(file);
    if (parent != null) {
      directoryMeasures.add(value, metric, parent);
    }
  }

  private void collectFilesCountMeasure(Resource file) {
    Resource parent = new Php().getParent(file);
    if (parent != null) {
      directoryMeasures.add(1.0, CoreMetrics.FILES_COUNT, parent);
    }
  }

  private double extractValue(String value) throws ParseException {
    return MavenCollectorUtils.parseNumber(value);
  }

  private double extractValue(Integer value) throws ParseException {
    return MavenCollectorUtils.parseNumber(Integer.toString(value));
  }


}
