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

package org.sonar.plugins.php.cpd;

import org.junit.Test;
import static org.mockito.Mockito.*;
import org.sonar.commons.resources.Resource;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.metrics.CoreMetrics;
import org.sonar.plugins.php.Php;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

public class CpdExecutorTest {

  @Test
  public void shouldGenerateDuplicationMeasuresFromTwoFiles() throws Exception {
    ProjectContext context = mock(ProjectContext.class);

    File file1 = new File(getClass().getResource("/org/sonar/plugins/php/cpd/CpdExecutorTest/dir/sample.php").toURI());
    File file2 = new File(getClass().getResource("/org/sonar/plugins/php/cpd/CpdExecutorTest/dir/sample2.php").toURI());

    CpdExecutor executor = new CpdExecutor(context, Arrays.asList(file1, file2), Arrays.asList("/org/sonar/plugins/php/cpd/CpdExecutorTest"),
      50);
    executor.execute();

    Resource phpFile1 = Php.newFile("dir/sample.php");
    Resource phpFile2 = Php.newFile("dir/sample2.php");

    verify(context).addMeasure(phpFile1, CoreMetrics.DUPLICATED_FILES, 1d);
    verify(context).addMeasure(phpFile1, CoreMetrics.DUPLICATED_LINES, 10d);
    verify(context).addMeasure(phpFile1, CoreMetrics.DUPLICATED_BLOCKS, 1d);

    verify(context).addMeasure(phpFile2, CoreMetrics.DUPLICATED_FILES, 1d);
    verify(context).addMeasure(phpFile2, CoreMetrics.DUPLICATED_LINES, 10d);
    verify(context).addMeasure(phpFile2, CoreMetrics.DUPLICATED_BLOCKS, 1d);

    verify(context, atLeastOnce()).getResourceKey(phpFile1);
    verify(context, atLeastOnce()).getResourceKey(phpFile2);
  }

  @Test
  public void shouldDoNothingIfNoData() throws Exception {
    ProjectContext context = mock(ProjectContext.class);

    CpdExecutor executor = new CpdExecutor(context, Collections.<File>emptyList(), Arrays.asList("/org/sonar/plugins/php/cpd/CpdExecutorTest"),
      50);
    executor.execute();

    verify(context, never()).addMeasure(any(Resource.class), eq(CoreMetrics.DUPLICATED_FILES), anyDouble());
    verify(context, never()).addMeasure(any(Resource.class), eq(CoreMetrics.DUPLICATED_LINES), anyDouble());
    verify(context, never()).addMeasure(any(Resource.class), eq(CoreMetrics.DUPLICATED_BLOCKS), anyDouble());
  }

}
