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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.sonar.api.utils.SonarException;

/**
 * The Class PhpmdViolationsXmlParserTest.
 */
public class PhpmdViolationsXmlParserTest {

  /**
   * Should get valid suffixe option.
   */
  @Test(expected = SonarException.class)
  public void shouldThrowExceptionWhenReportFileDoesNotExist() {
    File reportFile = mock(File.class);
    when(reportFile.exists()).thenReturn(Boolean.FALSE);
    new PhpmdViolationsXmlParser(reportFile);
  }

}
