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

package org.sonar.plugins.php.pmd;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.sonar.api.utils.SonarException;

/**
 * The Class PhpmdViolationsXmlParserTest.
 */
public class PhpmdViolationsXmlParserTest {

  /**
   * Should get valid suffixe option.
   * 
   * @throws MalformedURLException
   */
  @Test(expected = SonarException.class)
  public void shouldThrowExceptionWhenReportFileDoesNotExist() throws MalformedURLException {
    File reportFile = mock(File.class);
   // when(reportFile.exists()).thenReturn(Boolean.FALSE);
    PhpmdViolationsXmlParser parser = new PhpmdViolationsXmlParser(reportFile.toURL());
    parser.getViolations();
  }

  /**
   * Should get violations from the file.
   */
  @Test
  public void parserTest() {
    URL reportFile = getClass().getResource("/org/sonar/plugins/php/pmd/php-pmd-result.xml");
    PhpmdViolationsXmlParser parser = new PhpmdViolationsXmlParser(reportFile);
    List<PhpmdViolation> violations = parser.getViolations();
    assertThat(violations).isNotEmpty();
    assertThat(violations).hasSize(30);
  }

}
