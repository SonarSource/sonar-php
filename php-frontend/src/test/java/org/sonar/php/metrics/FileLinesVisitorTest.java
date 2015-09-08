/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.php.PHPAstScanner;
import org.sonar.test.TestUtils;

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
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);

    File file = TestUtils.getResource("/metrics/lines.php");
    FileLinesVisitor visitor = new FileLinesVisitor(newFileSystem(file), fileLinesContextFactory);
    PHPAstScanner.scanSingleFile(file, visitor);

    verify(fileLinesContext, times(4)).setIntValue(eq(CoreMetrics.NCLOC_DATA_KEY), anyInt(), eq(1));
    verify(fileLinesContext, times(6)).setIntValue(eq(CoreMetrics.COMMENT_LINES_DATA_KEY), anyInt(), eq(1));
  }

  private FileSystem newFileSystem(File file) {
    DefaultFileSystem fs = new DefaultFileSystem();

    fs.add(new DefaultInputFile(file.getName())
      .setAbsolutePath(file.getAbsolutePath())
      .setType(InputFile.Type.MAIN));

    return fs;
  }

}
