/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.metrics;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.google.common.io.Files;
import org.sonar.duplications.internal.pmd.TokensLine;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;

public class CpdVisitorTest {

  private static final Charset CHARSET = Charsets.UTF_8;

  private final ActionParser<Tree> p = PHPParserBuilder.createParser(CHARSET);

  private DefaultInputFile inputFile;
  private SensorContextTester sensorContext;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void test() throws Exception {
    scan("<?php $x = 1;\n$y = 'str' + $x;\n");
    List<TokensLine> cpdTokenLines = sensorContext.cpdTokens("moduleKey:" + inputFile.file().getName());
    assertThat(cpdTokenLines).hasSize(2);
    TokensLine firstTokensLine = cpdTokenLines.get(0);
    assertThat(firstTokensLine.getValue()).isEqualTo("<?php$x=$NUMBER;");
    assertThat(firstTokensLine.getStartLine()).isEqualTo(1);
    assertThat(firstTokensLine.getStartUnit()).isEqualTo(1);
    assertThat(firstTokensLine.getEndLine()).isEqualTo(1);
    assertThat(firstTokensLine.getEndUnit()).isEqualTo(5);

    TokensLine secondTokensLine = cpdTokenLines.get(1);
    assertThat(secondTokensLine.getValue()).isEqualTo("$y=$CHARS+$x;");
    assertThat(secondTokensLine.getStartLine()).isEqualTo(2);
    assertThat(secondTokensLine.getStartUnit()).isEqualTo(6);
    assertThat(secondTokensLine.getEndLine()).isEqualTo(2);
    assertThat(secondTokensLine.getEndUnit()).isEqualTo(11);
  }

  @Test
  public void test_use() throws Exception {
    scan("<?php use a\\b;\n");
    List<TokensLine> cpdTokenLines = sensorContext.cpdTokens("moduleKey:" + inputFile.file().getName());
    assertThat(cpdTokenLines).hasSize(1);
    TokensLine firstTokensLine = cpdTokenLines.get(0);
    assertThat(firstTokensLine.getValue()).isEqualTo("<?php");
  }

  @Test
  public void test_expandable_string() throws Exception {
    scan("<?php \"abc$x!abc\";");
    List<TokensLine> cpdTokenLines = sensorContext.cpdTokens("moduleKey:" + inputFile.file().getName());
    assertThat(cpdTokenLines).hasSize(1);
    TokensLine firstTokensLine = cpdTokenLines.get(0);
    assertThat(firstTokensLine.getValue()).isEqualTo("<?php\"$CHARS$x$CHARS\";");
    assertThat(firstTokensLine.getStartLine()).isEqualTo(1);
    assertThat(firstTokensLine.getStartUnit()).isEqualTo(1);
    assertThat(firstTokensLine.getEndLine()).isEqualTo(1);
    assertThat(firstTokensLine.getEndUnit()).isEqualTo(7);
  }

  @Test
  public void test_heredoc_string() throws Exception {
    scan("<?php <<<EOF\nabc$x!abc\nabc\nEOF;");
    List<TokensLine> cpdTokenLines = sensorContext.cpdTokens("moduleKey:" + inputFile.file().getName());
    assertThat(cpdTokenLines).hasSize(3);
    TokensLine firstTokensLine = cpdTokenLines.get(0);
    assertThat(firstTokensLine.getValue()).isEqualTo("<?php<<<EOF");
    assertThat(firstTokensLine.getStartLine()).isEqualTo(1);
    assertThat(firstTokensLine.getStartUnit()).isEqualTo(1);
    assertThat(firstTokensLine.getEndLine()).isEqualTo(1);
    assertThat(firstTokensLine.getEndUnit()).isEqualTo(2);

    TokensLine secondTokensLine = cpdTokenLines.get(1);
    assertThat(secondTokensLine.getValue()).isEqualTo("$CHARS$x$CHARS");
    assertThat(secondTokensLine.getStartLine()).isEqualTo(2);
    assertThat(secondTokensLine.getStartUnit()).isEqualTo(3);
    assertThat(secondTokensLine.getEndLine()).isEqualTo(2);
    assertThat(secondTokensLine.getEndUnit()).isEqualTo(5);

    TokensLine thirdTokensLine = cpdTokenLines.get(2);
    assertThat(thirdTokensLine.getValue()).isEqualTo("EOF;");
    assertThat(thirdTokensLine.getStartLine()).isEqualTo(4);
    assertThat(thirdTokensLine.getStartUnit()).isEqualTo(6);
    assertThat(thirdTokensLine.getEndLine()).isEqualTo(4);
    assertThat(thirdTokensLine.getEndUnit()).isEqualTo(7);
  }

  private void scan(String source) throws IOException {
    File file = tempFolder.newFile();
    Files.write(source, file, CHARSET);

    inputFile = new DefaultInputFile("moduleKey", file.getName())
      .initMetadata(new FileMetadata().readMetadata(file, CHARSET));

    sensorContext = SensorContextTester.create(tempFolder.getRoot().toPath());
    sensorContext.fileSystem().add(inputFile);

    CpdVisitor cpdVisitor = new CpdVisitor(sensorContext);
    CompilationUnitTree tree = (CompilationUnitTree)p.parse(file);
    cpdVisitor.analyze(file, tree);
  }
}
