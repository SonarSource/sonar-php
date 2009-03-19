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

package org.sonar.plugins.php.phpcodesniffer;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.commons.resources.Resource;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.maven.xml.XmlParserException;
import org.sonar.plugins.php.Php;
import org.sonar.plugins.php.phpdepend.PhpDependExecutionException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class PhpCodeSnifferResultsParser {

  private static final Logger LOG = LoggerFactory.getLogger(PhpCodeSnifferResultsParser.class);
  private PhpCodeSnifferConfiguration configuration;
  private ProjectContext context;
  private List<String> sourcesDir;

  public PhpCodeSnifferResultsParser(PhpCodeSnifferConfiguration configuration, ProjectContext context) {
    this.configuration = configuration;
    this.context = context;
    this.sourcesDir = Arrays.asList(configuration.getSourceDir().getAbsolutePath());
  }

  protected PhpCodeSnifferResultsParser(PhpCodeSnifferConfiguration configuration) {
    this.configuration = configuration;
  }

  public void parse() {
    File reportXml = configuration.getReportFile();
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

  protected XMLStreamReader2 initReader(File reportXml) throws FileNotFoundException, XMLStreamException {
    XMLInputFactory2 xmlFactory = (XMLInputFactory2) XMLInputFactory2.newInstance();
    InputStream input = new FileInputStream(reportXml);
    return (XMLStreamReader2) xmlFactory.createXMLStreamReader(input);
  }

  protected void collectMeasures(File reportXml) throws FileNotFoundException, XMLStreamException {
    XMLStreamReader2 reader = initReader(reportXml);

    while (reader.next() != XMLStreamConstants.END_DOCUMENT) {
      if (reader.isStartElement()) {
        String elementName = reader.getLocalName();
        if (elementName.equals("file")) {
          collectFileMeasures(reader);
        }
      }
    }
    reader.closeCompletely();

  }

  private void collectFileMeasures(XMLStreamReader2 reader) throws XMLStreamException {
    String name = reader.getAttributeValue(null, "name");
    Resource file = Php.newFileFromAbsolutePath(name, sourcesDir);
    collectViolations(file, reader);
  }

  private void collectViolations(Resource file, XMLStreamReader2 reader) throws XMLStreamException {
    boolean isNotAtFileEndTag = reader.next() != XMLStreamConstants.END_DOCUMENT;
    String elementName;
    while (isNotAtFileEndTag) {
      if (reader.isStartElement()) {
        elementName = reader.getLocalName();
        if (elementName.equals("error")) {
          Violation violation = new Violation(file, "error",
            reader.getAttributeValue(null, "line"),
            reader.getAttributeValue(null, "column"),
            reader.getAttributeValue(null, "source"),
            reader.getText()
            );
          violation.createViolation();

          reader.skipElement();
        } else if (elementName.equals("warning")) {
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
  }

  class Violation {

    private Resource file;
    private String level;
    private String line;
    private String column;
    private String key;
    private String message;

    public Violation(Resource file, String level, String line, String column, String key, String message) {
      this.file = file;
      this.level = level;
      this.line = line;
      this.column = column;
      this.key = key;
      this.message = message;
    }

    public void createViolation(){
      
      
    }
  }
}
