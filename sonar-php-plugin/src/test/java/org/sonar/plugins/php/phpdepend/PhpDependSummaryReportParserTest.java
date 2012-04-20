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
package org.sonar.plugins.php.phpdepend;

import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;

import java.io.File;

import static org.sonar.plugins.php.MockUtils.getMockProject;

/**
 * The Class PhpDependSummaryReportParserTest.
 */
public class PhpDependSummaryReportParserTest {

  /**
   * As this is not implemented yet, it should throw SonarException
   */
  @Test(expected = SonarException.class)
  public void testParse() {
    /*Configuration c = mock(Configuration.class);
    Project project = getMockProject("/path/to/sources", c);
    when(c.getString(PDEPEND_REPORT_FILE_NAME_KEY, PDEPEND_REPORT_FILE_NAME_DEFVALUE)).thenReturn(PDEPEND_REPORT_FILE_NAME_DEFVALUE);
    when(c.getString(PDEPEND_REPORT_TYPE, PDEPEND_REPORT_TYPE_DEFVALUE)).thenReturn("summary-xml");
    when(c.getString(PDEPEND_REPORT_FILE_RELATIVE_PATH_KEY, PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn(
        PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE);
    when(project.getConfiguration()).thenReturn(c);*/
    Project project = getMockProject();

    PhpDependSummaryReportParser parser = new PhpDependSummaryReportParser(project, null);
    parser.parse(new File("path/to/nowhere"));
  }
}
