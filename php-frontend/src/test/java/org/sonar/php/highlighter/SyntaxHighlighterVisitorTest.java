/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.highlighter;

import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;

class SyntaxHighlighterVisitorTest {

  private static final ActionParser<Tree> PARSER = PHPParserBuilder.createParser();

  private File file;

  private SensorContextTester context;

  @TempDir
  public File tempFolder;

  @BeforeEach
  void setUp() {
    file = new File(tempFolder, "file");
  }

  @Test
  void emptyInput() {
    highlight("<?php ");

    assertThat(context.highlightingTypeAt(componentKey(), 1, 0)).isEmpty();
  }

  @Test
  void singleLineComment() {
    highlight("<?php   //Comment ");

    checkOnRange(1, 8, 10, TypeOfText.COMMENT);
  }

  @Test
  void multilineCommentOnOneLine() {
    highlight("<?php   /*Comment*/ ");

    checkOnRange(1, 8, 11, TypeOfText.COMMENT);
  }

  @Test
  void multilineCommentOnThreeLines() {
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
  void shellStyleComment() {
    highlight("<?php   #Comment ");

    checkOnRange(1, 8, 9, TypeOfText.COMMENT);
  }

  @Test
  void phpdocComment() {
    highlight("<?php   /**Comment*/ ");

    checkOnRange(1, 8, 12, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  void keyword() {
    highlight("<?php eval(\"1\");");

    checkOnRange(1, 6, 4, TypeOfText.KEYWORD);
    checkOnRange(1, 11, 3, TypeOfText.STRING);
  }

  @Test
  void phpReservedVariables() {
    highlight("<?php $a = $this; $b = __LINE__;");

    checkOnRange(1, 11, 5, TypeOfText.KEYWORD);
    checkOnRange(1, 23, 8, TypeOfText.KEYWORD);
  }

  @Test
  void string() {
    highlight("<?php $x = \"a\";");

    checkOnRange(1, 11, 3, TypeOfText.STRING);
  }

  @Test
  void expandableString() {
    highlight("<?php \"Hello $name!\";");

    checkOnRange(1, 6, 7, TypeOfText.STRING); // "Hello_
    checkOnRange(1, 18, 2, TypeOfText.STRING); // !"
  }

  @Test
  void numbers() {
    highlight("<?php $x = 1; $y = 1.0;");

    checkOnRange(1, 11, 1, TypeOfText.CONSTANT);
    checkOnRange(1, 19, 3, TypeOfText.CONSTANT);
  }

  private void highlight(String s) {
    DefaultInputFile inputFile = TestInputFileBuilder.create("moduleKey", file.getName())
      .setLanguage("php")
      .setType(Type.MAIN)
      .initMetadata(s)
      .build();

    // TODO: Check that it works
    // fileSystem.add(inputFile);

    context = SensorContextTester.create(tempFolder);

    NewHighlighting highlighting = context.newHighlighting().onFile(inputFile);
    Tree tree = PARSER.parse(s);
    SyntaxHighlighterVisitor.highlight(tree, highlighting);
    highlighting.save();
  }

  /**
   * Checks the highlighting of a range of columns. The first column of a line has index 0.
   * The range is the columns of the token.
   */
  private void checkOnRange(int line, int firstColumn, int length, @Nullable TypeOfText expectedTypeOfText) {
    new HighlightChecker(componentKey()).checkOnRange(context, line, firstColumn, length, expectedTypeOfText);
  }

  /**
   * Checks the highlighting of one column. The first column of a line has index 0.
   */
  private void check(int line, int column, @Nullable TypeOfText expectedTypeOfText) {
    new HighlightChecker(componentKey()).check(context, line, column, expectedTypeOfText);
  }

  private String componentKey() {
    return "moduleKey:" + file.getName();
  }

}
