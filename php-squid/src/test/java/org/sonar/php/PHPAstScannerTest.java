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
package org.sonar.php;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.sonar.sslr.impl.ast.AstXmlPrinter;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.junit.Test;
import org.sonar.php.api.PHPMetric;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceProject;
import org.sonar.squidbridge.indexer.QueryByType;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class PHPAstScannerTest {

  @Test
  public void files() {
    AstScanner<LexerlessGrammar> scanner = PHPAstScanner.create(new PHPConfiguration(Charsets.UTF_8));
    scanner.scanFiles(ImmutableList.of(new File("src/test/resources/metrics/lines.php"), new File("src/test/resources/metrics/lines_of_code.php")));
    SourceProject project = (SourceProject) scanner.getIndex().search(new QueryByType(SourceProject.class)).iterator().next();
    assertThat(project.getInt(PHPMetric.FILES)).isEqualTo(2);
  }

  @Test
  public void lines() {
    SourceFile file = PHPAstScanner.scanSingleFile(new File("src/test/resources/metrics/lines.php"));
    assertThat(file.getInt(PHPMetric.LINES)).isEqualTo(17);
  }

  @Test
  public void lines_of_code() {
    SourceFile file = PHPAstScanner.scanSingleFile(new File("src/test/resources/metrics/lines_of_code.php"));
    assertThat(file.getInt(PHPMetric.LINES_OF_CODE)).isEqualTo(4);
  }

  @Test
  public void functions() {
    SourceFile file = PHPAstScanner.scanSingleFile(new File("src/test/resources/metrics/functions.php"));
    assertThat(file.getInt(PHPMetric.FUNCTIONS)).isEqualTo(4);
  }

  @Test
  public void classes() {
    SourceFile file = PHPAstScanner.scanSingleFile(new File("src/test/resources/metrics/classes.php"));
    assertThat(file.getInt(PHPMetric.CLASSES)).isEqualTo(4);
  }

  @Test
  public void statements() {
    SourceFile file = PHPAstScanner.scanSingleFile(new File("src/test/resources/metrics/statements.php"));
    assertThat(file.getInt(PHPMetric.STATEMENTS)).isEqualTo(29);
  }

  @Test
  public void comments() {
    SourceFile file = PHPAstScanner.scanSingleFile(new File("src/test/resources/metrics/comments.php"));
    assertThat(file.getInt(PHPMetric.COMMENT_LINES)).isEqualTo(3);
    assertThat(file.getNoSonarTagLines()).contains(14);
    assertThat(file.getNoSonarTagLines().size()).isEqualTo(1);
  }

  @Test
  public void complexity() {
    SourceFile file = PHPAstScanner.scanSingleFile(new File("src/test/resources/metrics/complexity.php"));
    assertThat(file.getInt(PHPMetric.COMPLEXITY)).isEqualTo(17);
  }
}
