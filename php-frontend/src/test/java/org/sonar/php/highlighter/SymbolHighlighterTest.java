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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.fs.internal.DefaultTextRange;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;

class SymbolHighlighterTest {

  private static final ActionParser<Tree> PARSER = PHPParserBuilder.createParser();

  private File file;

  private SensorContextTester context;

  @TempDir
  public File tempFolder;

  @BeforeEach
  public void setUp() throws IOException {
    file = new File(tempFolder, "file");
  }

  @Test
  void testEmptyInput() {
    highlight("<?php ");

    checkNoSymbolExists(1, 1);
  }

  @Test
  void testNoUsages() {
    highlight("<?php   $a = 1; ");

    checkNoSymbolExists(1, 7); // (blank)
    checkSymbolExistence(1, 8); // $
    checkSymbolExistence(1, 9); // a
    checkNoSymbolExists(1, 10); // (blank)

    checkSymbolReferences(1, 8, new LinkedList<>());
  }

  @Test
  void testUsages() {
    highlight("<?php   $a = 1; echo $a; $a = 4; ");

    checkSymbolReferences(1, 8, Arrays.asList(
      textRange(1, 21, 1, 23),
      textRange(1, 25, 1, 27)));
  }

  @Test
  void testCompoundVariable() {
    highlight("<?php   $a = 1; echo \"${a}\"; echo \"$a\";");

    checkSymbolReferences(1, 8, Arrays.asList(textRange(1, 24, 1, 25), textRange(1, 35, 1, 37)));
  }

  @Test
  void testUseClause() {
    highlight("<?php $b = 42; $f = function() use($b) { echo $b; };");

    // there are 3 symbols: global $b, local $b, $f
    checkSymbolExistence(1, 15); // $f

    // global $b
    checkSymbolReferences(1, 6, Collections.singletonList(textRange(1, 35, 1, 37)));
    // local $b
    checkSymbolReferences(1, 35, Collections.singletonList(textRange(1, 46, 1, 48)));
  }

  @Test
  void testArrowFunction() {
    highlight("<?php $a = 1; $b = 2; $f = fn($b) => $a + $b; foo($a); foo($b);");
    // global $a
    checkSymbolReferences(1, 6, Arrays.asList(
      textRange(1, 37, 1, 39),
      textRange(1, 50, 1, 52)));
    // global $b
    checkSymbolReferences(1, 14, Collections.singletonList(
      textRange(1, 59, 1, 61)));
    // local $b
    checkSymbolReferences(1, 30, Collections.singletonList(
      textRange(1, 42, 1, 44)));
  }

  private void highlight(String s) {
    DefaultInputFile inputFile = TestInputFileBuilder.create("moduleKey", file.getName())
      .setLanguage("php")
      .setType(Type.MAIN)
      .initMetadata(s)
      .build();

    // TODO: Look if I work
    // fileSystem.add(inputFile);

    context = SensorContextTester.create(tempFolder);

    NewSymbolTable newSymbolTable = context.newSymbolTable().onFile(inputFile);
    Tree tree = PARSER.parse(s);
    SymbolHighlighter.highlight(SymbolTableImpl.create((CompilationUnitTree) tree), newSymbolTable);
    newSymbolTable.save();
  }

  private static TextRange textRange(int startLine, int startColumn, int endLine, int endColumn) {
    return new DefaultTextRange(new DefaultTextPointer(startLine, startColumn), new DefaultTextPointer(endLine, endColumn));
  }

  private void checkSymbolExistence(int line, int column) {
    new SymbolChecker(componentKey()).checkSymbolExistence(context, line, column);
  }

  private void checkNoSymbolExists(int line, int column) {
    new SymbolChecker(componentKey()).checkNoSymbolExists(context, line, column);
  }

  private void checkSymbolReferences(int line, int column, List<? extends TextRange> referenceRanges) {
    new SymbolChecker(componentKey()).checkSymbolReferences(context, line, column, referenceRanges);
  }

  private String componentKey() {
    return "moduleKey:" + file.getName();
  }

}
