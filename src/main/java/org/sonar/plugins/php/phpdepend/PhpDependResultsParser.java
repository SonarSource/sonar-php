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
import org.sonar.plugins.php.ResourcesBag;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhpDependResultsParser {

  private static final Logger LOG = LoggerFactory.getLogger(PhpDependResultsParser.class);

  private PhpDependConfiguration config;
  private ProjectContext context;
  private List<String> sourcesDir;
  private ResourcesBag resourcesBag;

  private Set<Metric> metrics;

  public PhpDependResultsParser(PhpDependConfiguration config, ProjectContext context) {
    this.config = config;
    this.context = context;
    this.sourcesDir = Arrays.asList(config.getSourceDir().getAbsolutePath());
    resourcesBag = new ResourcesBag();
    metrics = getMetrics();
  }

  protected PhpDependResultsParser(PhpDependConfiguration config, ProjectContext context, List<String> sourcesDir, Set<Metric> metrics) {
    this.config = config;
    this.context = context;
    this.sourcesDir = sourcesDir;
    this.metrics = metrics;
    resourcesBag = new ResourcesBag();
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

  private Set<Metric> getMetrics() {
    Set<Metric> metrics = new HashSet<Metric>();
    metrics.add(CoreMetrics.LOC);
    metrics.add(CoreMetrics.NCLOC);
    metrics.add(CoreMetrics.FUNCTIONS_COUNT);
    metrics.add(CoreMetrics.COMMENT_LINES);
    metrics.add(CoreMetrics.FILES_COUNT);
    metrics.add(CoreMetrics.COMPLEXITY);
    metrics.add(CoreMetrics.CLASSES_COUNT);
    metrics.add(CoreMetrics.COMMENT_RATIO);
    metrics.add(CoreMetrics.COMPLEXITY_AVG_BY_FUNCTION);
    metrics.add(CoreMetrics.COMPLEXITY_AVG_BY_CLASS);
    return metrics;
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
          collectProjectMeasures(reader);
        } else if (elementName.equals("file")) {
          String filePath = reader.getAttributeValue(null, "name");
          currentResourceFile = Php.newFileFromAbsolutePath(filePath, sourcesDir);
          collectFileMeasures(reader, currentResourceFile);
        } else if (elementName.equals("class")) {
          collectClassMeasures(reader, currentResourceFile);
        } else if (elementName.equals("function")) {
          collectFunctionMeasures(reader, currentResourceFile);
        }
      }
    }
    reader.closeCompletely();

    saveMeasures();
  }

  private void collectProjectMeasures(XMLStreamReader2 reader) throws ParseException, XMLStreamException {
    addMeasureFromAttribute(reader, null, CoreMetrics.LOC, "loc");
    addMeasureFromAttribute(reader, null, CoreMetrics.NCLOC, "locExecutable");
    addMeasureFromAttribute(reader, null, CoreMetrics.FUNCTIONS_COUNT, "nom");
    addMeasureFromAttribute(reader, null, CoreMetrics.COMMENT_LINES, "cloc");
    addMeasureFromAttribute(reader, null, CoreMetrics.FILES_COUNT, "files");
    addMeasureFromAttribute(reader, null, CoreMetrics.COMPLEXITY, "ccn");

    addMeasure(null, CoreMetrics.CLASSES_COUNT, getProjectClassesCountMeasure(reader));
    addMeasure(null, CoreMetrics.COMMENT_RATIO, getCommentRatioMeasure(null));
  }

  private void collectFileMeasures(XMLStreamReader2 reader, Resource file) throws ParseException, XMLStreamException {
    addMeasureFromAttribute(reader, file, CoreMetrics.LOC, "loc");
    addMeasureFromAttribute(reader, file, CoreMetrics.NCLOC, "locExecutable");
    addMeasureFromAttribute(reader, file, CoreMetrics.COMMENT_LINES, "cloc");
    addMeasureFromAttribute(reader, file, CoreMetrics.CLASSES_COUNT, "classes");

    addMeasure(file, CoreMetrics.FILES_COUNT, 1.0);
    addMeasure(file, CoreMetrics.COMMENT_RATIO, getCommentRatioMeasure(file));
  }

  private void collectClassMeasures(XMLStreamReader2 reader, Resource file) throws ParseException {
    addMeasureFromAttribute(reader, file, CoreMetrics.FUNCTIONS_COUNT, "nom");
    addMeasureFromAttribute(reader, file, CoreMetrics.COMPLEXITY, "wmc");
  }

  private void collectFunctionMeasures(XMLStreamReader2 reader, Resource file) throws ParseException {
    addMeasure(file, CoreMetrics.FUNCTIONS_COUNT, 1.0);
    addMeasureFromAttribute(reader, file, CoreMetrics.COMPLEXITY, "ccn");
  }

  private void saveMeasures() throws ParseException {
    for (Resource resource : resourcesBag.getResources()) {
      for (Metric metric : resourcesBag.getMetrics(resource)) {
        if (metrics.contains(metric)) {
          Double measure = resourcesBag.getMeasure(metric, resource);
          recordMeasure(resource, metric, measure);
        }
      }
      calculateComplexMeasure(resource);
    }
  }

  private void calculateComplexMeasure(Resource resource) throws ParseException {
    recordComplexMeasure(CoreMetrics.COMPLEXITY_AVG_BY_FUNCTION, resource, getComplexityPerMethodMeasure(resource));
    recordComplexMeasure(CoreMetrics.COMPLEXITY_AVG_BY_CLASS, resource, getComplexityPerClassMeasure(resource));
  }

  private void recordMeasure(Resource resource, Metric metric, Double measure) {
    if (resource != null) {
      context.addMeasure(resource, metric, measure);
    } else {
      context.addMeasure(metric, measure);
    }
  }

  private void recordComplexMeasure(Metric metric, Resource resource, Double value) throws ParseException {
    if (metrics.contains(metric)) {
      if (value != null) {
        recordMeasure(resource, metric, value);
      }
    }
  }

  private Double getProjectClassesCountMeasure(XMLStreamReader2 reader) throws ParseException {
    String classes = reader.getAttributeValue(null, "classes");
    String interfaces = reader.getAttributeValue(null, "interfs");
    return extractValue(classes) + extractValue(interfaces);
  }

  private Double getCommentRatioMeasure(Resource resource) throws ParseException {
    Double cloc = resourcesBag.getMeasure(CoreMetrics.COMMENT_LINES, resource);
    Double loc = resourcesBag.getMeasure(CoreMetrics.LOC, resource);
    if (cloc != null && loc != null && loc != 0) {
      return cloc / loc * 100;
    }
    return null;
  }

  private Double getComplexityPerMethodMeasure(Resource resource) throws ParseException {
    Double ccn = resourcesBag.getMeasure(CoreMetrics.COMPLEXITY, resource);
    Double functions = resourcesBag.getMeasure(CoreMetrics.FUNCTIONS_COUNT, resource);
    if (ccn != null && functions != null && functions != 0) {
      return ccn / functions;
    }
    return null;
  }

  private Double getComplexityPerClassMeasure(Resource resource) throws ParseException {
    Double ccn = resourcesBag.getMeasure(CoreMetrics.COMPLEXITY, resource);
    Double classes = resourcesBag.getMeasure(CoreMetrics.CLASSES_COUNT, resource);
    if (ccn != null && classes != null && classes != 0) {
      return ccn / classes;
    }
    return null;
  }

  private Double addMeasureFromAttribute(XMLStreamReader2 reader, Resource resource, Metric metric, String attribute) throws ParseException {
    String value = reader.getAttributeValue(null, attribute);
    if (value != null) {
      Double doubleValue = extractValue(value);
      addMeasure(resource, metric, doubleValue);
      return doubleValue;
    }
    return null;
  }

  private void addMeasure(Resource file, Metric metric, Double value) throws ParseException {
    if (value != null) {
      if (file != null) {
        addAFileMeasureAndItsParentIfExisting(file, metric, value);
      } else {
        addAProjectMeasure(metric, value);
      }
    }
  }

  private void addAProjectMeasure(Metric metric, Double value) {
    resourcesBag.add(value, metric, null);
  }

  private void addAFileMeasureAndItsParentIfExisting(Resource file, Metric metric, Double value) {
    resourcesBag.add(value, metric, file);
    Resource parent = new Php().getParent(file);
    if (parent != null) {
      resourcesBag.add(value, metric, parent);
    }
  }

  private double extractValue(String value) throws ParseException {
    return MavenCollectorUtils.parseNumber(value);
  }

}