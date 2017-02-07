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

import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.fs.internal.DefaultTextRange;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;

public class SymbolHighlighterTest {

  private static final ActionParser<Tree> PARSER = PHPParserBuilder.createParser();

  private File file;

  private DefaultInputFile inputFile;

  private SensorContextTester context;

  private NewSymbolTable newSymbolTable;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Before
  public void setUp() throws IOException {
    DefaultFileSystem fileSystem = new DefaultFileSystem(tempFolder.getRoot());
    fileSystem.setEncoding(StandardCharsets.UTF_8);
    file = tempFolder.newFile();
    inputFile = new DefaultInputFile("moduleKey", file.getName())
      .setLanguage("php")
      .setType(Type.MAIN);
    fileSystem.add(inputFile);

    context = SensorContextTester.create(tempFolder.getRoot());

    newSymbolTable = context.newSymbolTable().onFile(inputFile);
  }

  @Test
  public void test_empty_input() throws Exception {
    highlight("<?php ");

    checkNoSymbolExists(1, 1);
  }

  @Test
  public void test_no_usages() throws Exception {
    highlight("<?php   $a = 1; ");

    checkNoSymbolExists(1, 7); // (blank)
    checkSymbolExistence(1, 8); // $
    checkSymbolExistence(1, 9); // a
    checkNoSymbolExists(1, 10); // (blank)

    checkSymbolReferences(1, 8, new LinkedList<>());
  }

  @Test
  public void test_usages() throws Exception {
    highlight("<?php   $a = 1; echo $a; $a = 4; ");

    checkSymbolReferences(1, 8, ImmutableList.of(
      textRange(1, 21, 1, 23),
      textRange(1, 25, 1, 27)));
  }

  @Test
  public void test_compound_variable() throws Exception {
    highlight("<?php   $a = 1; echo \"${a}\"; echo \"$a\";");

    checkSymbolReferences(1, 8, ImmutableList.of(textRange(1, 35, 1, 37)));
  }

  @Test
  public void test_use_clause() throws Exception {
    highlight("<?php $b = 42; $f = function() use($b) { echo $b; };");

    // there are 3 symbols: global $b, local $b, $f
    checkSymbolExistence(1, 15); // $f

    checkSymbolReferences(1, 6, ImmutableList.of(textRange(1, 35, 1, 37))); // global $b
    checkSymbolReferences(1, 35, ImmutableList.of(textRange(1, 46, 1, 48))); // local $b
  }

  private void highlight(String s) {
    inputFile.initMetadata(s);
    Tree tree = PARSER.parse(s);
    new SymbolHighlighter().highlight(SymbolTableImpl.create((CompilationUnitTree) tree), newSymbolTable);
    newSymbolTable.save();
  }

  private TextRange textRange(int startLine, int startColumn, int endLine, int endColumn) {
    return new DefaultTextRange(new DefaultTextPointer(startLine, startColumn), new DefaultTextPointer(endLine, endColumn));
  }

  private void checkSymbolExistence(int line, int column) {
    String componentKey = "moduleKey:" + file.getName();
    new SymbolChecker(componentKey).checkSymbolExistence(context, line, column);
  }

  private void checkNoSymbolExists(int line, int column) {
    String componentKey = "moduleKey:" + file.getName();
    new SymbolChecker(componentKey).checkNoSymbolExists(context, line, column);
  }

  private void checkSymbolReferences(int line, int column, List<? extends TextRange> referenceRanges) {
    String componentKey = "moduleKey:" + file.getName();
    new SymbolChecker(componentKey).checkSymbolReferences(context, line, column, referenceRanges);
  }

}
