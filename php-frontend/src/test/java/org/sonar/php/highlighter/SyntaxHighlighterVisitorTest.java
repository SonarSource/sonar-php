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
package org.sonar.php.highlighter;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;

public class SyntaxHighlighterVisitorTest {

  private static final ActionParser<Tree> PARSER = PHPParserBuilder.createParser(Charsets.UTF_8);

  private File file;

  private DefaultInputFile inputFile;

  private SensorContextTester context;

  private NewHighlighting highlighting;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Before
  public void setUp() throws IOException {
    DefaultFileSystem fileSystem = new DefaultFileSystem(tempFolder.getRoot());
    fileSystem.setEncoding(Charsets.UTF_8);
    file = tempFolder.newFile();
    inputFile = new DefaultInputFile("moduleKey",  file.getName())
      .setLanguage("php")
      .setType(Type.MAIN);
    fileSystem.add(inputFile);

    context = SensorContextTester.create(tempFolder.getRoot());

    highlighting = context.newHighlighting().onFile(inputFile);
  }

  @Test
  public void empty_input() throws Exception {
    highlight("<?php ");

    assertThat(context.highlightingTypeAt("moduleKey:" + file.getName(), 1, 0)).isEmpty();
  }

  @Test
  public void single_line_comment() throws Exception {
    highlight("<?php   //Comment ");

    checkOnRange(1, 8, 10, TypeOfText.COMMENT);
  }

  @Test
  public void multiline_comment_on_one_line() throws Exception {
    highlight("<?php   /*Comment*/ ");

    checkOnRange(1, 8, 11, TypeOfText.COMMENT);
  }

  @Test
  public void multiline_comment_on_three_lines() throws Exception {
    highlight("<?php   /*Comment line 1\n  Comment line 2\n  Comment line 3*/ ");
    
    // line 1
    check(1, 7, null);
    check(1, 8, TypeOfText.COMMENT);
    check(1, 23, TypeOfText.COMMENT);

    // line 2
    check(2, 0, TypeOfText.COMMENT);
    check(2, 5, TypeOfText.COMMENT);
    check(2, 5, TypeOfText.COMMENT);

    // line 3
    check(3, 0, TypeOfText.COMMENT);
    check(3, 17, TypeOfText.COMMENT);
    check(3, 18, null);
  }

  @Test
  public void shell_style_comment() throws Exception {
    highlight("<?php   #Comment ");

    checkOnRange(1, 8, 9, TypeOfText.COMMENT);
  }

  @Test
  public void phpdoc_comment() throws Exception {
    highlight("<?php   /**Comment*/ ");

    checkOnRange(1, 8, 12, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  public void keyword() throws Exception {
    highlight("<?php eval(\"1\");");

    checkOnRange(1, 6, 4, TypeOfText.KEYWORD);
    checkOnRange(1, 11, 3, TypeOfText.STRING);
  }

  @Test
  public void php_reserved_variables() throws Exception {
    highlight("<?php $a = $this; $b = __LINE__;");

    checkOnRange(1, 11, 5, TypeOfText.KEYWORD);
    checkOnRange(1, 23, 8, TypeOfText.KEYWORD);
  }

  @Test
  public void string() throws Exception {
    highlight("<?php $x = \"a\";");

    checkOnRange(1, 11, 3, TypeOfText.STRING);
  }

  @Test
  public void expandable_string() throws Exception {
    highlight("<?php \"Hello $name!\";");

    checkOnRange(1, 6, 7, TypeOfText.STRING);    // "Hello_
    checkOnRange(1, 18, 2, TypeOfText.STRING);   // !"
  }

  @Test
  public void numbers() throws Exception {
    highlight("<?php $x = 1; $y = 1.0;");

    checkOnRange(1, 11, 1, TypeOfText.CONSTANT);
    checkOnRange(1, 19, 3, TypeOfText.CONSTANT);
  }

  private void highlight(String s) {
    inputFile.initMetadata(s);
    Tree tree = PARSER.parse(s);
    SyntaxHighlighterVisitor.highlight(tree, highlighting);
    highlighting.save();
  }

  /**
   * Checks the highlighting of a range of columns. The first column of a line has index 0.
   * The range is the columns of the token.
   */
  private void checkOnRange(int line, int firstColumn, int length, TypeOfText expectedTypeOfText) {
    String componentKey = "moduleKey:" + file.getName();
    new HighlightChecker(componentKey).checkOnRange(context, line, firstColumn, length, expectedTypeOfText);
  }

  /**
   * Checks the highlighting of one column. The first column of a line has index 0.
   */
  private void check(int line, int column, TypeOfText expectedTypeOfText) {
    String componentKey = "moduleKey:" + file.getName();
    new HighlightChecker(componentKey).check(context, line, column, expectedTypeOfText);
  }

}
