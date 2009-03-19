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

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.sonar.plugins.api.maven.ProjectContext;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;

public class PhpCodeSnifferResultsParserTest {

  private ProjectContext context;
  private PhpCodeSnifferConfiguration config;
  private File xmlReport;

  private void init() {
    try {
      xmlReport = new File(getClass().getResource("/org/sonar/plugins/php/phpcodesniffer/PhpCodeSnifferResultsParserTest/phpcodesniffer-report.xml").toURI());
      context = mock(ProjectContext.class);
      config = mock(PhpCodeSnifferConfiguration.class);
      stub(config.getReportFile()).toReturn(xmlReport);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void shouldCollectAFileMeasure() throws Exception {
    init();
    final boolean[] hasCollectAFileMeasure = new boolean[]{false};
    PhpCodeSnifferResultsParser parser = new PhpCodeSnifferResultsParser(config){
      @Override
      protected void collectMeasures(File reportXml) throws FileNotFoundException, XMLStreamException {
        hasCollectAFileMeasure[0] = true;
      }
    };
    parser.collectMeasures(xmlReport);
    assertTrue(hasCollectAFileMeasure[0]);
  }
}
