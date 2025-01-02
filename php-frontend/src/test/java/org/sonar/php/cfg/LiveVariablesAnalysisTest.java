/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.cfg;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.cfg.ControlFlowGraph;
import org.sonar.plugins.php.api.cfg.ControlFlowGraphTest;
import org.sonar.plugins.php.api.cfg.ExpectedCfgStructure;
import org.sonar.plugins.php.api.cfg.Validator;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This Live Variable Analysis Test uses a meta-language to specify the expected LVA values for each basic block.
 *
 * Convention:
 *
 * 1. the metadata is specified as a function call with the form:
 *
 * {@code block1( succ = [block2, END], liveIn = [$x, $y], liveOut = [$y], gen = [$x, $y], kill = [$x] ); }
 * where the arguments are assignments to:
 * - 'succ' is a bracketed array of expected successor ids. For branching blocks, the true successor must be first.
 * - 'liveIn'  - the live variables that enter the block
 * - 'liveOut' - the live variables that exit the block
 * - 'gen'     - the variables that are consumed by the block
 * - 'kill'    - the variables that are killed (overwritten) by the block
 *
 * 2. each basic block must contain a function call with this structure as the first statement
 * - exception: a Label is before the block function call
 *
 * 3. the name of the function is the identifier of the basic block
 *
 * Also check {@link ExpectedCfgStructure} and {@link ControlFlowGraphTest}
 */
class LiveVariablesAnalysisTest extends PHPTreeModelTest {

  @Test
  void testSimpleKill() {
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [], liveOut = [], gen = [], kill = [foo, bar, qix]);" +
      "$foo = 1;" +
      "$bar = bar();" +
      "$qix = 1 + 2;");
  }

  @Test
  void testNullCoalescing() {
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [foo], liveOut = [], gen = [foo], kill = [foo, bar, qix]);" +
      "$foo ??= 1;" +
      "$bar = bar();" +
      "$qix = 1 + 2;");
  }

  @Test
  void testSimpleGen() {
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [foo, bar], liveOut = [], gen = [foo, bar]);" +
      "foo($foo, $bar);");
  }

  @Test
  void testComplexReadsAndWrites() {
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [a,b], liveOut = [], gen = [a,b], kill = []);" +
      "$a[$b] = 1;");

    // R, R
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [a], liveOut = [], gen = [a], kill = []);" +
      "read($a);" +
      "read($a);");

    // RW, R
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [a], liveOut = [], gen = [a], kill = [a]);" +
      "$a = read($a);" +
      "read($a);");

    // RW, W
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [a], liveOut = [], gen = [a], kill = [a]);" +
      "$a = read($a);" +
      "$a = 1;");

    // R, W
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [a], liveOut = [], gen = [a], kill = [a]);" +
      "read($a);" +
      "$a = 1;");

    // W, R
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [], liveOut = [], gen = [], kill = [a]);" +
      "$a = 1;" +
      "read($a);");

    // W, W
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [], liveOut = [], gen = [], kill = [a]);" +
      "$a = 1;" +
      "$a = 1;");

    // R, W, R
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [a], liveOut = [], gen = [a], kill = [a]);" +
      "read($a);" +
      "$a = 1;" +
      "read($a);");

    // R, RW, R
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [a], liveOut = [], gen = [a], kill = [a]);" +
      "read($a);" +
      "$a = read($a);" +
      "read($a);");

    // W, R, W
    verifyLiveVariableAnalysis("" +
      "block( succ = [END], liveIn = [], liveOut = [], gen = [], kill = [a]);" +
      "$a = 1;" +
      "read($a);" +
      "$a = 1;");
  }

  @Test
  void testWriteBeforeRead() {
    verifyLiveVariableAnalysis("" +
      "condition( succ = [body, END], liveIn = [], liveOut = [], gen = [], kill = [a]);" +
      "$a = 1;" +
      "foo($a);" +
      "if (true) {" +
      "  body( succ = [END], liveIn = [], liveOut = [], gen = [], kill = [a]);" +
      "  $a = 1;" +
      "  foo($a);" +
      "}");
  }

  @Test
  void testGenKill() {
    verifyLiveVariableAnalysis("" +
      "condition( succ = [body, END], liveIn = [x], liveOut = [], gen = [x], kill = [a]);" +
      "$a = $x + 1;" +
      "foo($a);" +
      "if (true) {" +
      "  body( succ = [END], liveIn = [], liveOut = [], gen = [], kill = [x]);" +
      "  $x = 1;" +
      "}");

    verifyLiveVariableAnalysis("" +
      "condition( succ = [body, END], liveIn = [x,a], liveOut = [], gen = [a,x], kill = [a]);" +
      "foo($a);" +
      "$a = $x + 1;" +
      "if (true) {" +
      "  body( succ = [END], liveIn = [], liveOut = [], gen = [], kill = [x]);" +
      "  $x = 1;" +
      "}");
  }

  @Test
  void testDoWhile() {
    verifyLiveVariableAnalysis("" +
      "beforeDo( succ = [body], liveIn = [x], liveOut = [a], gen = [x], kill = [a]);" +
      "$a = $x + 1;" +
      "do {" +
      "  body( succ = [cond], liveIn = [a], liveOut = [a], gen = [a], kill = []);" +
      "  foo ($a);" +
      "} while(cond( succ = [body, afterDo], liveIn = [a], liveOut = [a], gen = [], kill = []) );" +
      "afterDo( succ = [END], liveIn = [], liveOut = [], gen = [], kill = [a]);" +
      "$a = 0;");
  }

  @Test
  void testWithIgnoredParam() {
    verifyLiveVariableAnalysis("$a", "" +
      "condition( succ = [insideIf], liveIn = [foo, bar], liveOut = [x,a], gen = [foo, bar], kill = [x,y,z,w,a]);" +
      "$a = 42;" +
      "foo($a);" +
      "list(, , $x, $y) = array();" +
      "list(foo()) = array();" +
      "list($z => $w ) = array('a' => 32);" +
      "foo($foo, $bar, $x);" +
      "if ($y) {" +
      "  insideIf (succ = [END], liveIn = [x,a], liveOut = [], gen = [x,a]);" +
      "  foo($a, $x);" +
      "}");
  }

  @Test
  void compoundAssignments() {
    verifyLiveVariableAnalysis("" +
      "condition( succ = [insideIf], liveIn = [y], liveOut = [x,y], gen = [y], kill = [x,y]);" +
      "$x = 0;" +
      "$x += 20;" +
      "$y += 20;" +
      "if ($y) {" +
      "  insideIf (succ = [END], liveIn = [x,y], liveOut = [], gen = [x,y]);" +
      "  foo($x, $y);" +
      "}");
  }

  @Test
  void prefixPostfixIncrementDecrementExpressions() {
    verifyLiveVariableAnalysis("" +
      "condition( succ = [insideIf], liveIn = [a, a1, a2], liveOut = [a], gen = [a, a1, a2], kill = [a]);" +
      "--$a;" +
      "++$a;" +
      "++$a1[$a2];" +
      "$a++;" +
      "$a--;" +
      "if ($a) {" +
      "  insideIf (succ = [END], liveIn = [a], liveOut = [], gen = [a]);" +
      "  foo($a);" +
      "}");
  }

  @Test
  void unaryMinusPlusExpression() {
    verifyLiveVariableAnalysis("" +
      "condition( succ = [insideIf], liveIn = [a], liveOut = [a], gen = [a], kill = []);" +
      "foo($a);" +
      "-$a;" +
      "+$a;" +
      "if (cond) {" +
      "  insideIf (succ = [END], liveIn = [a], liveOut = [], gen = [a]);" +
      "  foo($a);" +
      "}");
  }

  @Test
  void testWithArrayAssignment() {
    verifyLiveVariableAnalysis("" +
      "condition( succ = [insideIf], liveIn = [], liveOut = [x], gen = [], kill = [x,y]);" +
      "list($x, $y) = array();" +
      "if ($y) {" +
      "  insideIf (succ = [END], liveIn = [x], liveOut = [], gen = [x]);" +
      "  foo($x);" +
      "}");
  }

  @Test
  void readSymbols() {
    String body = "" +
      "$foo = 1;" +
      "$bar = bar();" +
      "$bar += bar();" +
      "read($bar);" +
      "$qix += 1 + 2;";
    CompilationUnitTree cut = parse("<?php function f() { " + body + " }", PHPLexicalGrammar.COMPILATION_UNIT);
    SymbolTableImpl symbolTable = SymbolTableImpl.create(cut);
    FunctionDeclarationTree functionTree = (FunctionDeclarationTree) cut.script().statements().get(0);
    ControlFlowGraph cfg = ControlFlowGraph.build(functionTree.body());
    LiveVariablesAnalysis analysis = LiveVariablesAnalysis.analyze(cfg, symbolTable);
    Set<Symbol> readSymbols = analysis.getReadSymbols();
    assertThat(readSymbols).extracting("name").containsExactlyInAnyOrder("$bar", "$qix");
  }

  private void verifyLiveVariableAnalysis(String body) {
    verifyLiveVariableAnalysis("", body);
  }

  private void verifyLiveVariableAnalysis(String argsList, String body) {
    CompilationUnitTree cut = parse("<?php function f(" + argsList + ") { " + body + " }", PHPLexicalGrammar.COMPILATION_UNIT);
    SymbolTableImpl symbolTable = SymbolTableImpl.create(cut);
    FunctionDeclarationTree functionTree = (FunctionDeclarationTree) cut.script().statements().get(0);
    ControlFlowGraph cfg = ControlFlowGraph.build(functionTree.body());
    LiveVariablesAnalysis analysis = LiveVariablesAnalysis.analyze(cfg, symbolTable);
    Validator.assertLiveVariables(cfg, analysis);
  }

}
