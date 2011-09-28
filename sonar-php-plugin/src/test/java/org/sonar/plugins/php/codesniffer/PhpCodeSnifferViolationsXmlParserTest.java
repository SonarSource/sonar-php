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
package org.sonar.plugins.php.codesniffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.sonar.api.utils.SonarException;

public class PhpCodeSnifferViolationsXmlParserTest {

  @Test
  public void shouldReturnViolationsFromExistingFile() throws URISyntaxException, XMLStreamException {
    PhpCodeSnifferViolationsXmlParser parser = new PhpCodeSnifferViolationsXmlParser();
    String reportFile = "/org/sonar/plugins/php/codesniffer/violations/parser/codesniffer-simple-result.xml";
    File xmlFile = FileUtils.toFile(getClass().getResource(reportFile));
    List<PhpCodeSnifferViolation> violations = parser.getViolations(xmlFile);
    assertTrue(violations != null && !violations.isEmpty());
    assertEquals(violations.size(), 2);
  }

  @Test(expected = SonarException.class)
  public void shouldThrowExceptionOnNonExistingFile() throws URISyntaxException, XMLStreamException {
    PhpCodeSnifferViolationsXmlParser parser = new PhpCodeSnifferViolationsXmlParser();
    String reportFile = "BLABLALBLALBA.XML";
    File xmlFile = FileUtils.toFile(getClass().getResource(reportFile));
    parser.getViolations(xmlFile);
  }

}
