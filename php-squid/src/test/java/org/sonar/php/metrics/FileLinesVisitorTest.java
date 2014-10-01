/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
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
package org.sonar.php.metrics;

import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.php.PHPAstScanner;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileLinesVisitorTest {

  @Test
  public void test() {
    FileLinesContextFactory fileLinesContextFactory = Mockito.mock(FileLinesContextFactory.class);

    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(Resource.class))).thenReturn(fileLinesContext);

    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(fs.getBasedir()).thenReturn(new File("src/test/resources/"));

    Project project = new Project("key");
    project.setFileSystem(fs);

    FileLinesVisitor visitor = new FileLinesVisitor(project, fileLinesContextFactory);
    PHPAstScanner.scanSingleFile(new File("src/test/resources/metrics/lines.php"), visitor);

    verify(fileLinesContext, times(4)).setIntValue(eq(CoreMetrics.NCLOC_DATA_KEY), anyInt(), eq(1));
    verify(fileLinesContext, times(6)).setIntValue(eq(CoreMetrics.COMMENT_LINES_DATA_KEY), anyInt(), eq(1));
  }

}
