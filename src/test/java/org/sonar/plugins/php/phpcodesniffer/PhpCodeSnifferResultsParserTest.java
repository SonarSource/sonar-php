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

import org.codehaus.stax2.XMLStreamReader2;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import org.sonar.commons.resources.Resource;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.php.Php;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.Arrays;

public class PhpCodeSnifferResultsParserTest {

  private ProjectContext context;
  private PhpCodeSnifferConfiguration config;
  private File xmlReport;

  private void init() {
    try {
      xmlReport = new File(getClass().getResource("/org/sonar/plugins/php/phpcodesniffer/PhpCodeSnifferResultsParserTest/phpcodesniffer-report.xml").toURI());
      context = mock(ProjectContext.class);
      config = mock(PhpCodeSnifferConfiguration.class);
      when(config.getReportFile()).thenReturn(xmlReport);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void shouldCollectFileMeasures() throws Exception {
    init();
    final int[] nbFileMeasures = new int[]{0};
    PhpCodeSnifferResultsParser parser = new PhpCodeSnifferResultsParser(config) {
      @Override
      protected void collectFileMeasures(XMLStreamReader2 reader) throws XMLStreamException {
        nbFileMeasures[0]++;
      }
    };
    parser.collectMeasures(xmlReport);
    assertThat(nbFileMeasures[0], is(3));
  }

  @Test
  public void shouldCollectErrorAndWarningViolationLevel() throws Exception {
    final int[] nbViolations = new int[]{0};
    PhpCodeSnifferResultsParser parser = new PhpCodeSnifferResultsParser(config) {
      @Override
      protected void createViolation(Resource file, XMLStreamReader2 reader, String level) {
        nbViolations[0]++;
      }
    };
    XMLStreamReader2 reader = mock(XMLStreamReader2.class);
    when(reader.isStartElement()).thenReturn(true, true, false);
    when(reader.next()).thenReturn(XMLStreamConstants.CHARACTERS);
    when(reader.getLocalName()).thenReturn("error", "warning", "file");
    when(reader.isEndElement()).thenReturn(false, true, true);

    parser.collectFileMeasures(reader);
    assertThat(nbViolations[0], is(2));
  }

  @Test
  public void shouldCollectSomeViolations() throws Exception {
    init();
    ViolationsManager violationsManager = mock(ViolationsManager.class);
    PhpCodeSnifferResultsParser parser = new PhpCodeSnifferResultsParser(config, violationsManager, Arrays.asList("C:\\projets\\_PHP\\_Money\\src"));

    parser.collectMeasures(xmlReport);
    verify(violationsManager).createViolation(eq(Php.newFile("Common", "IMoney.php")), eq("error"), eq("37"),
      eq("PEAR.Commenting.FileCommentSniff"), anyString());
    verify(violationsManager).createViolation(eq(Php.newFile("Common", "IMoney.php")), eq("warning"), eq("45"),
      eq("PEAR.Commenting.FileCommentSniff"), anyString());
  }
}
