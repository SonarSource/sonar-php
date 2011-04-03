/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Sonar PHP Plugin
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.api.measures.CoreMetrics.DUPLICATED_BLOCKS;
import static org.sonar.api.measures.CoreMetrics.DUPLICATED_FILES;
import static org.sonar.api.measures.CoreMetrics.DUPLICATED_LINES;
import static org.sonar.plugins.php.MockUtils.getMockProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.PhpFile;

public class PhpCpdResultParserTest {

  /**
   * 
   * @throws MalformedURLException
   */
  @Test
  public void shouldThrowExceptionWhenReportFileDoesNotExist() throws MalformedURLException {
    File reportFile = mock(File.class);
    SensorContext context = mock(SensorContext.class);
    when(reportFile.exists()).thenReturn(Boolean.FALSE);
    PhpCpdResultParser parser = new PhpCpdResultParser(getMockProject(), context);
    parser.parse(reportFile);
    verify(context, never()).saveMeasure(Mockito.any(Measure.class));
    verify(context, never()).saveMeasure(Mockito.any(Resource.class), Mockito.any(Measure.class));
    verify(context, never()).saveMeasure(Mockito.any(Resource.class), Mockito.any(Metric.class), Mockito.any(Double.class));
  }

  /**
   * 
   * @throws MalformedURLException
   */
  @Test(expected = SonarException.class)
  public void shouldThrowExceptionWhenReportFileIsInvalid() throws MalformedURLException {
    File reportFile = mock(File.class);
    SensorContext context = mock(SensorContext.class);
    when(reportFile.exists()).thenReturn(Boolean.TRUE);
    PhpCpdResultParser parser = new PhpCpdResultParser(getMockProject(), context);
    parser.parse(reportFile);
  }

  @Test
  public void shouldParse() throws URISyntaxException, XMLStreamException {
    SensorContext context = mock(SensorContext.class);
    Project project = getMockProject("C:/php/math-php-test/source/src/");
    PhpCpdResultParser parser = new PhpCpdResultParser(project, context);
    String reportFile = "/org/sonar/plugins/php/cpd/php-cpd.xml";
    File xmlFile = FileUtils.toFile(getClass().getResource(reportFile));
    parser.parse(xmlFile);

    PhpFile mathPhp = new PhpFile("Math.php");
    verify(context).saveMeasure(mathPhp, DUPLICATED_FILES, 1.0);
    verify(context).saveMeasure(mathPhp, DUPLICATED_LINES, 0.0);
    verify(context).saveMeasure(mathPhp, DUPLICATED_BLOCKS, 0.0);

  }
}
