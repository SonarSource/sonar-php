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

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import org.junit.Test;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MetricsVisitorTest {

  @Test
  public void test() {
    String relativePath = "src/test/resources/metrics/lines_of_code.php";
    File file = new File(relativePath);

    FileLinesContext linesContext = mock(FileLinesContext.class);

    MetricsVisitor metricsVisitor = new MetricsVisitor();

    ActionParser<Tree> parser = PHPParserBuilder.createParser(Charsets.UTF_8);
    Tree tree = parser.parse(file);
    FileMeasures fileMeasures = metricsVisitor.getFileMeasures(file, (CompilationUnitTree) tree, linesContext);

    // fixme : finish this test
    assertThat(fileMeasures.getFileComplexity()).isEqualTo(1.0);
    assertThat(fileMeasures.getFunctionNumber()).isEqualTo(0.0);
    assertThat(fileMeasures.getStatementNumber()).isEqualTo(0.0);
    assertThat(fileMeasures.getClassNumber()).isEqualTo(0.0);
  }
}
