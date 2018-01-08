/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import com.sonar.sslr.api.typed.ActionParser;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.php.FileTestUtils;
import org.sonar.php.metrics.CpdVisitor.CpdToken;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;

public class CpdVisitorTest {

  private final ActionParser<Tree> p = PHPParserBuilder.createParser();

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void test() throws Exception {
    List<CpdToken> tokens = scan("<?php $x = 1;\n$y = 'str' + $x;\n");

    assertThat(getImagesList(tokens)).containsExactly("$x", "=", "$NUMBER", ";", "$y", "=", "$CHARS", "+", "$x", ";");
  }

  @Test
  public void test_use() throws Exception {
    List<CpdToken> tokens = scan("<?php use a\\b;\n");

    assertThat(getImagesList(tokens)).containsExactly();
  }

  @Test
  public void test_expandable_string() throws Exception {
    List<CpdToken> tokens = scan("<?php \"abc$x!abc\";");

    assertThat(getImagesList(tokens)).containsExactly("\"", "$CHARS", "$x", "$CHARS", "\"", ";");
  }

  @Test
  public void test_heredoc_string() throws Exception {
    List<CpdToken> tokens = scan("<?php <<<EOF\nabc$x!abc\nabc\nEOF;");

    assertThat(getImagesList(tokens)).containsExactly("<<<EOF\n", "$CHARS", "$x", "$CHARS", "\nEOF", ";");
  }

  @Test
  public void should_not_include_tags() throws Exception {
    List<CpdToken> tokens = scan("<a/><?php $x; ?><b/>\n");

    assertThat(getImagesList(tokens)).containsExactly("$x", ";");
  }

  private List<CpdToken> scan(String source) throws IOException {
    PhpFile testFile = FileTestUtils.getFile( tempFolder.newFile(), source);
    CpdVisitor cpdVisitor = new CpdVisitor();
    CompilationUnitTree tree = (CompilationUnitTree)p.parse(testFile.contents());
    return cpdVisitor.getCpdTokens(testFile, tree);
  }

  private static List<String> getImagesList(List<CpdToken> tokens) {
    return tokens.stream().map(CpdToken::image).collect(Collectors.toList());
  }
}
