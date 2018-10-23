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
package org.sonar.php.cfg;

import com.sonar.sslr.api.RecognitionException;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.tree.impl.CompilationUnitTreeImpl;
import org.sonar.php.tree.visitors.PHPCheckContext;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This CFG Test uses a meta-language to specify the expected structure of the CFG.
 * <p>
 * Convention:
 * <p>
 * 1. the metadata is specified as a function call with the form:
 * <p>
 * {@code block1( succ = [block2, END], pred = [block0], elem = 1 ); }
 * where the argument are assignments:
 * - 'succ' is a bracketed array of expected successor ids. For branching blocks, the true successor must be first.
 * - 'pred' [optional] is a bracketed array of expected predecessor ids
 * - 'elem' [optional] is the number of expected elements in the block
 * <p>
 * 2. each basic block must contain a function call with this structure as the first statement
 * - exception: a Label is before the block function call
 * <p>
 * 3. the name of the function is the identifier of the basic block
 * <p>
 * Also check {@link ExpectedCfgStructure}
 */
public class ControlFlowGraphTest extends PHPTreeModelTest {

  @org.junit.Rule
  public LogTester logTester = new LogTester();

  private CheckContext checkContext;

  @Before
  public void setUp() throws Exception {
    PhpFile file = mock(PhpFile.class);
    when(file.toString()).thenReturn("mock.php");
    checkContext = new PHPCheckContext(file, mock(CompilationUnitTreeImpl.class), mock(SymbolTable.class));
  }

  @Test
  public void different_statements() {
    // empty statement
    verifyBlockCfg("" +
      "block( succ = [END], elem = 1 ); ;");

    // yield statement
    verifyBlockCfg("" +
      "block( succ = [END], elem = 2 ); yield 42;");

    // global statement
    verifyBlockCfg("" +
      "block( succ = [END], elem = 2 ); global $x;");

    // static statement
    verifyBlockCfg("" +
      "block( succ = [END], elem = 2 ); static $x;");

    // echo statement
    verifyBlockCfg("" +
      "block( succ = [END], elem = 2 ); echo($x);");

    // declare statement
    verifyBlockCfg("" +
      "block( succ = [END], elem = 2 ); declare (x);");

    // inline html
    verifyBlockCfg("" +
      "block( succ = [END], elem = 1 ); ?> <html> <?php");

    // unset statement
    verifyBlockCfg("" +
      "block( succ = [END], elem = 2 ); unset($x);");

    // statement list
    verifyBlockCfg("" +
      "block( succ = [END], elem = 2 ); 1, 2;");

    // function declaration
    verifyBlockCfg("" +
      "block( succ = [END], elem = 2 ); function foo(){}");

    // class declaration
    verifyBlockCfg("" +
      "block( succ = [END], elem = 1 ); class Foo{}");

    // interface declaration
    verifyBlockCfg("" +
      "block( succ = [END], elem = 2 ); interface Foo{}");

    // trait declaration
    verifyBlockCfg("" +
      "block( succ = [END], elem = 2 ); trait Foo{}");

    // namespace
    verifyScriptTreeCfg("" +
      "block( succ = [END], elem = 2 ); namespace NS;");

    // use
    verifyScriptTreeCfg("" +
      "block( succ = [END], elem = 2 ); use function foo;");

    // group use
    verifyScriptTreeCfg("" +
      "block( succ = [END], elem = 2 ); use My\\Project\\{Class1, Class2};");

    // const declaration
    verifyScriptTreeCfg("" +
      "block( succ = [END], elem = 2 ); const A = 1;");
  }

  @Test
  public void not_remove_empty_with_many_successors() {
    ControlFlowGraph cfg = cfgForBlock("" +
      "try {" +
      "  tryBody( succ = [catchBody] );" +
      "  throw $e;" +
      "} catch (Type $e) {" +
      "  catchBody1( succ = [finallyBody] );" +
      "}");

    for (CfgBlock cfgBlock : cfg.blocks()) {
      if (cfgBlock.syntacticSuccessor() != null) {
        assertThat(cfg.blocks()).contains(cfgBlock.syntacticSuccessor());
      }
    }
  }

  @Test
  public void replace_empty_start() {
    ControlFlowGraph cfg = cfgForBlock("" +
      "foreach($x as $y) {" +
      "  return $y;" +
      "}");

    assertThat(cfg.blocks()).contains(cfg.start());
    assertThat(cfg.start().elements()).isNotEmpty();
  }

  @Test
  public void infinite_for() throws Exception {
    ControlFlowGraph cfg = cfgForBlock("" +
      "for(;;) {" +
      " " +
      "}");

    assertThat(cfg.blocks()).hasSize(2);
    assertThat(cfg.start().successors()).containsOnly(cfg.start(), cfg.end());
    assertThat(cfg.start().elements()).isEmpty();
  }

  @Test
  public void try_stmt() {
    verifyBlockCfg("" +
      "try {" +
      "  tryBody( succ = [catchBody1, catchBody2, finallyBody] );" +
      "} catch (Type1 $e) {" +
      "  catchBody1( succ = [finallyBody] );" +
      "} catch (Type2 $e) {" +
      "  catchBody2( succ = [finallyBody] );" +
      "} finally {" +
      "  finallyBody( succ = [END] );" +
      "}");
  }

  @Test
  public void throw_outside_try() {
    verifyBlockCfg("body( succ = [END], elem = 2, syntSucc = END ); throw new Exception();");

    verifyBlockCfg("" +
      "while (cond( succ = [body, END])) {" +
      "  body( succ = [END], syntSucc = cond );" +
      "  throw e;" +
      "}");

    verifyBlockCfg("" +
      "before( succ = [body, after]);" +
      "if (condition) {" +
      "  body( succ = [END], syntSucc = after );" +
      "  throw e;" +
      "}" +
      "after( succ = [END]);");

    verifyBlockCfg("" +
      "block0( succ = [finallyBody] );" +
      "try {" +
      "} finally {" +
      "  finallyBody( succ = [throwBlock] );" +
      "}" +
      "throwBlock( succ = [END], syntSucc = dead );" +
      "throw $e;" +
      "dead( succ = [END]);");

    verifyBlockCfg("" +
      "block0( succ = [END], syntSucc = tryBody  );" +
      "throw $e;" +
      "try {" +
      "  tryBody( succ = [finallyBody]);" +
      "} finally {" +
      "  finallyBody( succ = [END] );" +
      "}");
  }

  @Test
  public void throw_inside_try() {
    verifyBlockCfg("" +
      "try {" +
      "  tryBody( succ = [catchBody1], syntSucc = dead );" +
      "  throw $e;" +
      "  dead( succ = [catchBody1, catchBody2, finallyBody] );" +
      "} catch (Type1 $e) {" +
      "  catchBody1( succ = [END], syntSucc = finallyBody );" +
      "  throw $e;" +
      "} catch (Type2 $e) {" +
      "  catchBody2( succ = [finallyBody] );" +
      "} finally {" +
      "  finallyBody( succ = [END], syntSucc = after );" +
      "  throw $e;" +
      "}" +
      "after( succ = [END]);");

    verifyBlockCfg("" +
      "try {" +
      "  try {" +
      "    innerTryBody( succ = [catchBody1], syntSucc = dead );" +
      "    throw $e;" +
      "    dead( succ = [catchBody1, outerTryBody]);" +
      "  } catch (Type $e) {" +
      "    catchBody1( succ = [outerTryBody] );" +
      "  }" +
      "  outerTryBody( succ = [catchBody2, END] );" +
      "} catch (Type $e) {" +
      "  catchBody2( succ = [END] );" +
      "}");
  }

  @Test
  public void return_stmt() {
    verifyBlockCfg("body( succ = [END], elem = 1 );");
    verifyBlockCfg("body( succ = [END], elem = 2, syntSucc = END ); return;");
    verifyBlockCfg("body( succ = [END], elem = 2, syntSucc = END ); return 42;");

    verifyBlockCfg("" +
      "while (cond( succ = [body, END])) {" +
      "  body( succ = [END], syntSucc = cond );" +
      "  return;" +
      "}");

    verifyBlockCfg("" +
      "before( succ = [body, after]);" +
      "if (condition) {" +
      "  body( succ = [END], syntSucc = after );" +
      "  return;" +
      "}" +
      "after( succ = [END]);");
  }

  @Test
  public void break_without_argument() {
    verifyBlockCfg("" +
      "while (cond( succ = [body, END], elem = 1 )) {" +
      "  body( succ = [END], elem = 2, syntSucc = cond );" +
      "  break;" +
      "}");

    verifyBlockCfg("" +
      "do {" +
      "  body( succ = [END], syntSucc = cond );" +
      "  break;" +
      "} while (cond( succ = [body, END] ));");

    verifyBlockCfg("" +
      "while (outerCond( succ = [innerCond, END] )) {" +
      "  while (innerCond( succ = [bodyInner, ifCond] )) {" +
      "    bodyInner( succ = [innerCond] );" +
      "  }" +
      "  if (ifCond( succ = [bodyIf, outerCond] )) {" +
      "    bodyIf( succ = [END], syntSucc = outerCond );" +
      "    break;" +
      "  }" +
      "}");

    verifyBlockCfg("" +
      "before(succ = [cond]);" +
      "for (; cond( succ = [body, END]) ; updateBlock(succ = [cond])) {" +
      "  body( succ = [END], syntSucc = updateBlock);" +
      "  break;" +
      "}");

    verifyBlockCfg("" +
      "before(succ = [cond], elem = 1);" +
      "foreach ( cond(succ = [body, END], elem = 1) as $foo) {" +
      "  body( succ = [END], elem = 2, syntSucc = cond );" +
      "  break;" +
      "}");
  }

  @Test
  public void continue_without_argument() {
    verifyBlockCfg("" +
      "while (cond( succ = [body, END], elem = 1 )) {" +
      "  body( succ = [cond], elem = 2, syntSucc = dead );" +
      "  continue;" +
      "  dead( succ = [cond], elem = 1);" +
      "}");

    verifyBlockCfg("" +
      "do {" +
      "  body( succ = [cond], syntSucc = cond );" +
      "  continue;" +
      "} while (cond( succ = [body, END] ));");

    verifyBlockCfg("" +
      "before(succ = [cond]);" +
      "for (; cond( succ = [body, END]); update( succ = [cond])) {" +
      "  body( succ = [update], syntSucc = dead);" +
      "  continue;" +
      "  dead( succ = [update]);" +
      "}");

    verifyBlockCfg("" +
      "before(succ = [cond], elem = 1);" +
      "foreach ( cond(succ = [body, END], elem = 1) as $foo) {" +
      "  body( succ = [cond], elem = 2, syntSucc = dead );" +
      "  continue;" +
      "  dead( succ = [cond], elem = 1);" +
      "}");
  }

  @Test
  public void break_with_argument() {
    String breakInnerLoop = "" +
      "while (outerCond( succ = [innerCond, END] )) {" +
      "  while (innerCond( succ = [ifCond, outerCond] )) {" +
      "    if (ifCond( succ = [body, innerCond] )) {" +
      "      body( succ = [outerCond], syntSucc = innerCond );" +
      "      break %s;" +
      "    }" +
      "  }" +
      "}";

    verifyBlockCfg(String.format(breakInnerLoop, "0"));
    verifyBlockCfg(String.format(breakInnerLoop, "1"));

    verifyBlockCfg("" +
      "while (cond( succ = [body, END], elem = 1 )) {" +
      "  body( succ = [END], elem = 2, syntSucc = cond );" +
      "  break (1);" +
      "}");

    verifyBlockCfg("" +
      "while (outerCond( succ = [innerCond, END] )) {" +
      "  while (innerCond( succ = [ifCond, outerCond] )) {" +
      "    if (ifCond( succ = [body, innerCond] )) {" +
      "      body( succ = [END], syntSucc = innerCond );" +
      "      break 2;" +
      "    }" +
      "  }" +
      "}");

    verifyBlockCfg("" +
      "startBlock(succ = [outerCond]);" +
      "for ($i=1 ; outerCond( succ = [forBody, END]); update(succ = [outerCond])) {" +
      "  forBody(succ = [doBody]);" +
      "  do {" +
      "    doBody( succ = [END], syntSucc = doDead);" +
      "    break 2;" +
      "    doDead( succ = [innerCond]);" +
      "  } while (innerCond( succ = [doBody, afterDo] ));" +
      "  afterDo( succ = [update]);" +
      "}");

    verifyBlockCfg("" +
      "do {" +
      "  doBody( succ = [innerCond]);" +
      "  foreach ( innerCond(succ = [forBody, afterForeach]) as $foo) {" +
      "    forBody( succ = [END], syntSucc = dead);" +
      "    break 2;" +
      "    dead( succ = [innerCond]);" +
      "  }" +
      "  afterForeach( succ = [outerCond]);" +
      "} while (outerCond( succ = [doBody, END] ));");
  }

  @Test
  public void continue_with_argument() {
    String continueInnerLoop = "" +
      "while (outerCond( succ = [innerCond, END] )) {" +
      "  while (innerCond( succ = [ifCond, outerCond] )) {" +
      "    if (ifCond( succ = [body, innerCond] )) {" +
      "      body( succ = [innerCond], syntSucc = innerCond );" +
      "      continue %s;" +
      "    }" +
      "  }" +
      "}";

    verifyBlockCfg(String.format(continueInnerLoop, "0"));
    verifyBlockCfg(String.format(continueInnerLoop, "1"));

    verifyBlockCfg("" +
      "do {" +
      "  body( succ = [cond], syntSucc = cond );" +
      "  continue (0);" +
      "} while (cond( succ = [body, END] ));");

    verifyBlockCfg("" +
      "while (outerCond( succ = [innerCond, END] )) {" +
      "  while (innerCond( succ = [ifCond, outerCond] )) {" +
      "    if (ifCond( succ = [body, innerCond] )) {" +
      "      body( succ = [outerCond], syntSucc = innerCond );" +
      "      continue 2;" +
      "    }" +
      "  }" +
      "}");

    verifyBlockCfg("" +
      "do {" +
      "  doBody( succ = [innerCond]);" +
      "  while (innerCond( succ = [ifCond, outerCond] )) {" +
      "    if (ifCond( succ = [whileBody, innerCond] )) {" +
      "      whileBody( succ = [outerCond], syntSucc = innerCond );" +
      "      continue 2;" +
      "    }" +
      "  }" +
      "} while (outerCond( succ = [doBody, END] ));");

    verifyBlockCfg("" +
      "startBlock(succ = [outerCond]);" +
      "for ($i=1 ; outerCond( succ = [forBody, END]); outerUpdate(succ = [outerCond])) {" +
      "  forBody(succ = [doBody]);" +
      "  do {" +
      "    doBody( succ = [outerUpdate], syntSucc = doDead );" +
      "    continue 2;" +
      "    doDead( succ = [innerCond]);" +
      "  } while (innerCond( succ = [doBody, afterDo] ));" +
      "  afterDo( succ = [outerUpdate]);" +
      "}");

    verifyBlockCfg("" +
      "do {" +
      "  doBody( succ = [innerCond]);" +
      "  foreach ( innerCond(succ = [ifCond, afterForeach]) as $foo) {" +
      "    if (ifCond( succ = [ifBody, afterIf])) {" +
      "      ifBody(succ = [outerCond], syntSucc = afterIf);" +
      "      continue 2;" +
      "    }" +
      "    afterIf( succ = [innerCond]);" +
      "  }" +
      "  afterForeach( succ = [outerCond]);" +
      "} while (outerCond( succ = [doBody, END] ));");
  }

  // supported by PHP <5.4
  @Test(expected = RecognitionException.class)
  public void break_unsupported_with_expression() {
    cfgForBlock("" +
      "while (cond) {" +
      "  break 2 - 1;" +
      "}");
  }

  @Test(expected = RecognitionException.class)
  public void break_outside_loop() {
    cfgForBlock("break 2;");
  }

  @Test(expected = RecognitionException.class)
  public void break_invalid_level() {
    cfgForBlock("" +
      "while (cond) {" +
      "  break 2;" +
      "}");
  }

  @Test(expected = RecognitionException.class)
  public void break_invalid_argument() {
    cfgForBlock("" +
      "while (cond) {" +
      "  break 2.1;" +
      "}");
  }

  @Test
  public void do_while() {
    verifyBlockCfg("" +
      "before( succ = [body] );" +
      "do {" +
      "  body( succ = [cond] );" +
      "} while (cond( succ = [body, after] ));" +
      "after( succ = [END] );");
  }

  @Test
  public void do_while_with_nested_if() {
    verifyBlockCfg("" +
      "before( succ = [ifCond] );" +
      "do {" +
      "  if (ifCond( succ = [ifBody, loopCond] )) {" +
      "    ifBody( succ = [loopCond] );" +
      "  }" +
      "} while (loopCond( succ = [ifCond, after] ));" +
      "after( succ = [END] );");
  }

  @Test
  public void simple_while() {
    verifyBlockCfg("" +
      "before( succ = [cond] );" +
      "while (cond( succ = [body, after] )) {" +
      "  body( succ = [cond] );" +
      "}" +
      "after( succ = [END] );");

    verifyBlockCfg("" +
      "before( succ = [cond] );" +
      "while (cond( succ = [body, after] )) :" +
      "  body( succ = [cond] );" +
      "endwhile;" +
      "after( succ = [END] );");
  }

  @Test
  public void while_with_nested_if() {
    verifyBlockCfg("" +
      "before( succ = [whileCond] );" +
      "while (whileCond( succ = [ifCond, after] )) {" +
      "  if (ifCond( succ = [ifBody, whileCond] )) {" +
      "    ifBody( succ = [whileCond] );" +
      "  }" +
      "}" +
      "after( succ = [END] );");
  }

  @Test
  public void if_with_nested_while() {
    verifyBlockCfg("" +
      "before( succ = [ifBody, after], elem = 2 );" +
      "if (condition) {" +
      "  ifBody( succ = [whileCond], elem = 1 );" +
      "  while (whileCond( succ = [whileBody, ifBodyTail], elem = 1 )) {" +
      "    whileBody( succ = [whileCond], elem = 1 );" +
      "  }" +
      "  ifBodyTail( succ = [after], elem = 1 );" +
      "}" +
      "after( succ = [END], elem = 1 );");
  }

  @Test
  public void test_start_is_first_block() {
    ControlFlowGraph cfg = cfgForBlock("" +
      "foo();" +
      "if (a) {" +
      "  $x = 1;" +
      "}");
    CfgBlock startBlock = cfg.start();
    assertThat(startBlock.elements()).isNotEmpty();
    Tree firstElement = startBlock.elements().get(0);
    assertThat(firstElement.getKind()).isEqualTo(Tree.Kind.EXPRESSION_STATEMENT);
    ExpressionStatementTree statement = (ExpressionStatementTree) firstElement;
    assertThat(statement.expression().getKind()).isEqualTo(Tree.Kind.FUNCTION_CALL);
  }

  @Test
  public void test_branching_tree() {
    ControlFlowGraph cfg = cfgForBlock("" +
      "if (a) {" +
      "  qix();" +
      "}");
    CfgBlock block = cfg.start();
    assertThat(block instanceof PhpCfgBranchingBlock).isTrue();
    PhpCfgBranchingBlock ifBlock = (PhpCfgBranchingBlock) block;
    assertThat(ifBlock.branchingTree().getKind()).isEqualTo(Tree.Kind.IF_STATEMENT);
  }

  @Test
  public void simple_for() {
    verifyBlockCfg("" +
      "before(succ = [cond], elem = 3);" +
      "for ($i=1, $j=1 ; cond(succ = [forBody, END], elem=3), $i < 1, $j < 1; update(succ = [cond], elem=2), $i++) {" +
      "  forBody( succ = [update], elem = 1 );" +
      "}");
    verifyBlockCfg("" +
      "for (before(succ = [cond], elem = 2), $i=1; cond(succ = [forBody, END], elem=2), $i < 10; update(succ = [cond], elem = 2), $i++ ) :" +
      "  forBody(succ = [update], elem=1 );" +
      "endfor;");
  }

  @Test
  public void for_with_nested_if() {
    verifyBlockCfg("" +
      "for ( ; forCond( succ = [ifCond, END]); ) {" +
      "  if (ifCond( succ = [ifBody, forCond] )) {" +
      "    ifBody( succ = [forCond] );" +
      "  }" +
      "}");
  }

  @Test
  public void for_with_nested_ifs() {
    verifyBlockCfg("" +
      "for ( ; forCond( succ = [ifCondOne, END]); ) {" +
      "  if (ifCondOne( succ = [ifBodyOne, ifCondTwo] )) {" +
      "    ifBodyOne( succ = [ifCondTwo] );" +
      "  }" +
      "ifCondTwo( succ = [ifBodyTwo, forCond]);" +
      "foo();" +
      "if (a) {" +
      "    ifBodyTwo( succ = [forCond] );" +
      "  }" +
      "}");
  }

  @Test
  public void if_with_nested_for() {
    verifyBlockCfg("" +
      "ifCond(succ = [forCond, END]);" +
      "if (a) {" +
      "  for ( ; forCond( succ = [innerIfCond, END]); ) {" +
      "    innerIfCond(succ = [body, forCond]);" +
      "    if (a) {" +
      "      body( succ = [forCond] );" +
      "    }" +
      "  }" +
      "}");
  }

  @Test
  public void if_with_nested_for_nested_do_while() {
    verifyBlockCfg("" +
      "ifCond(succ = [forCond, END]);" +
      "if (a) {" +
      "  for ( ; forCond( succ = [innerIfCond, END]); ) {" +
      "    innerIfCond(succ = [ifBody, doBody]);" +
      "    if (a) {" +
      "      ifBody( succ = [doBody] );" +
      "    }" +
      "    do {" +
      "      doBody( succ = [doCond] );" +
      "    } while (doCond( succ = [doBody, forCond] ));" +
      "  }" +
      "}");
  }

  @Test
  public void simple_foreach() {
    verifyBlockCfg("" +
      "before(succ = [cond], elem = 1);" +
      "foreach ( cond(succ = [body, END], elem = 1) as $foo) {" +
      "  body( succ = [cond], elem = 1 );" +
      "}");
    verifyBlockCfg("" +
      "foreach ( cond(succ = [body, END]) as $key => $value):" +
      "  body( succ = [cond] );" +
      "endforeach;");
  }

  @Test
  public void foreach_with_nested_if() {
    verifyBlockCfg("" +
      "before(succ = [cond]);" +
      "foreach ( cond(succ = [ifCond, END]) as $foo) {" +
      "  if (ifCond( succ = [ifBody, cond] )) {" +
      "    ifBody( succ = [cond] );" +
      "  }" +
      "}");
  }

  @Test
  public void if_with_nested_foreach() {
    verifyBlockCfg("" +
      "ifCond(succ = [forCond, END]);" +
      "if (a) {" +
      "  foreach ( forCond(succ = [innerIfCond, END]) as $foo) {" +
      "    innerIfCond(succ = [body, forCond]);" +
      "    if (a) {" +
      "      body( succ = [forCond] );" +
      "    }" +
      "  }" +
      "}");
  }

  @Test
  public void if_with_nested_foreach_nested_do_while() {
    verifyBlockCfg("" +
      "if (ifCond(succ = [forCond, END])) {" +
      "  foreach ( forCond(succ = [innerIfCond, END]) as $foo) {" +
      "    innerIfCond(succ = [ifBody, doBody]);" +
      "    if (a) {" +
      "      ifBody( succ = [doBody] );" +
      "    }" +
      "    do {" +
      "      doBody( succ = [doCond] );" +
      "    } while (doCond( succ = [doBody, forCond] ));" +
      "  }" +
      "}");
  }

  @Test
  public void test_empty_block_removal() {
    ControlFlowGraph cfg = cfgForBlock("" +
      "if (a) {" +
      "  bar();" +
      "  if (b) {" +
      "    qix();" +
      "  }" +
      "}");
    assertThat(cfg.end().predecessors()).hasSize(3);
  }

  @Test
  public void test_with_script_tree() {
    verifyScriptTreeCfg("" +
      "b0( succ = [b1, b2], elem = 3 );" +
      "foo();" +
      "if (a) {" +
      "  b1( succ = [b2], elem = 1 );" +
      "}" +
      "b2( succ = [END], elem = 1 );");
  }

  /**
   * The predecessors are constructed based on the successors, so we should not test them all the time
   */
  @Test
  public void if_then_test_predecessors() {
    verifyBlockCfg("" +
      "b0( succ = [b1, b2], pred = [] );" +
      "foo();" +
      "if (a) {" +
      "  b1( succ = [b2], pred = [b0] );" +
      "}" +
      "b2( succ = [END], pred = [b0, b1] );");
  }

  @Test
  public void if_nested() {
    verifyBlockCfg("" +
      "b0( succ = [b1, b6] );" +
      "if (a?b:c) {" +
      "  b1( succ = [b2, b5] );" +
      "  if (b) {" +
      "    b2( succ = [b3, b4] );" +
      "    if (c) {" +
      "      b3( succ = [b4] );" +
      "    }" +
      "    b4( succ = [b5] );" +
      "  }" +
      "  b5( succ = [b6] );" +
      "}" +
      "b6( succ = [END] );");

    verifyBlockCfg("" +
      "b0( succ = [b1, b6] );" +
      "if (a?b:c) :" +
      "  b1( succ = [b2, b5] );" +
      "  if (b) :" +
      "    b2( succ = [b3, b4] );" +
      "    if (c) :" +
      "      b3( succ = [b4] );" +
      "    endif;" +
      "    b4( succ = [b5] );" +
      "  endif;" +
      "  b5( succ = [b6] );" +
      "endif;" +
      "b6( succ = [END] );");
  }

  @Test
  public void if_multiple() {
    verifyBlockCfg("" +
      "b0( succ = [b1, b2] );" +
      "if (a) {" +
      "  b1( succ = [b2] );" +
      "}" +
      "b2( succ = [b3, b4] );" +
      "if (b) {" +
      "  b3( succ = [b4] );" +
      "}" +
      "b4( succ = [END] );");
  }

  @Test
  public void if_else() {
    verifyBlockCfg("" +
      "before( succ = [insideIf, insideElse] );" +
      "if (a) {" +
      "  insideIf( succ = [END] );" +
      "} else {" +
      "  insideElse( succ = [END] );" +
      "}");
  }

  @Test
  public void if_elseif() {
    verifyBlockCfg("" +
      "beforeIf( succ = [insideIf, elseIfCond] );" +
      "if (a) {" +
      "  insideIf( succ = [END] );" +
      "} elseif ( elseIfCond( succ = [insideElseIf, END] )) {" +
      "  insideElseIf( succ = [END] );" +
      "}");
    verifyBlockCfg("" +
      "beforeIf( succ = [insideIf, elseIfCond] );" +
      "if (a) :" +
      "  insideIf( succ = [END] );" +
      "elseif ( elseIfCond( succ = [insideElseIf, END] )) :" +
      "  insideElseIf( succ = [END] );" +
      "endif;");
  }

  @Test
  public void if_else_if() {
    verifyBlockCfg("" +
      "beforeIf( succ = [insideIf, else_if] );" +
      "if (a) {" +
      "  insideIf( succ = [END] );" +
      "} else if ( else_if( succ = [inside_else_if, END] )) {" +
      "  inside_else_if( succ = [END] );" +
      "}");
  }

  @Test
  public void if_elseif_else() {
    verifyBlockCfg("" +
      "beforeIf( succ = [insideIf, firstElseIf] );" +
      "if (a) {" +
      "  insideIf( succ = [END] );" +
      "} elseif ( firstElseIf( succ = [insideFirstElseIf, secondElseIf] )) {" +
      "  insideFirstElseIf( succ = [END] );" +
      "} elseif ( secondElseIf( succ = [insideSecondElseIf, insideElse] )) {" +
      "  insideSecondElseIf( succ = [END] );" +
      "} else {" +
      "  insideElse( succ = [END] );" +
      "}");
  }

  @Test
  public void if_elseif_with_nested_while() {
    verifyBlockCfg("" +
      "before( succ = [ifBody, else_if] );" +
      "if (condition) {" +
      "  ifBody( succ = [END] );" +
      "} elseif ( else_if( succ = [whileCond, insideElse] )) {" +
      "  while (whileCond( succ = [whileBody, END] )) {" +
      "    whileBody( succ = [whileCond] );" +
      "  }" +
      "} else {" +
      "  insideElse( succ = [END] );" +
      "}");
  }

  @Test
  public void empty_switch_statement() {
    verifyBlockCfg("" +
      "before(succ = [after]);" +
      "switch ($expr) {" +
      "}" +
      "after(succ = [END]);"
    );
  }

  @Test
  public void switch_statement_single_case() {
    verifyBlockCfg("" +
      "before(succ = [c1]);" +
      "switch ($expr) {" +
      "    case c1(succ = [case1,after]):" +
      "        case1(succ = [after]);" +
      "}" +
      "after(succ = [END]);"
    );
  }

  @Test
  public void switch_statement_more_case() {
    verifyBlockCfg("" +
      "before(succ = [case1]);" +
      "switch ($expr) {" +
      "    case case1(succ = [case1Body, case2]):" +
      "        case1Body(succ = [case2Body]);" +
      "    case case2(succ = [case2Body,case3]):" +
      "        case2Body(succ = [case3Body]);" +
      "    case case3(succ = [case3Body,after]):" +
      "        case3Body(succ = [after]);" +
      "}" +
      "after(succ = [END]);"
    );
  }

  @Test
  public void switch_statement_with_default() {
    verifyBlockCfg("" +
      "before(succ = [default_]);" +
      "switch ($expr) {" +
      "    default: " +
      "        default_(succ = [after]);" +
      "}" +
      "after(succ = [END]);"
    );
  }

  @Test
  public void switch_statement_with_default_and_case() {
    verifyBlockCfg("" +
      "before(succ = [case1]);" +
      "switch ($expr) {" +
      "    case case1(succ = [case1Body,case2]):" +
      "        case1Body(succ = [case2Body]);" +
      "    case case2(succ = [case2Body,default_]):" +
      "        case2Body(succ = [default_]);" +
      "    default: " +
      "        default_(succ = [after]);" +
      "}" +
      "after(succ = [END]);"
    );
  }

  @Test
  public void switch_statement_with_default_first() {
    verifyBlockCfg("" +
      "before(succ = [case1]);" +
      "switch ($expr) {" +
      "    default: " +
      "        default_(succ = [case1Body]);" +
      "    case case1(succ = [case1Body, case2]):" +
      "        case1Body(succ = [case2Body]);" +
      "    case case2(succ = [case2Body,default_]):" +
      "        case2Body(succ = [after]);" +
      "}" +
      "after(succ = [END]);"
    );
  }

  @Test
  public void switch_statement_case_without_body() {
    verifyBlockCfg("" +
      "before(succ = [case1]);" +
      "switch ($expr) {" +
      "    case case1(succ = [case2Body, case2]):" +
      "    case case2(succ = [case2Body,after]):" +
      "        case2Body(succ = [after]);" +
      "}" +
      "after(succ = [END]);"
    );
  }

  @Test
  public void switch_with_case_using_same_block_as_default() throws Exception {
    verifyBlockCfg("" +
      "before(succ = [case1]);" +
      "switch ($expr) {" +
      "    case case1(succ = [default_, case2]):" +
      "    default: " +
      "        default_(succ = [case2Body]);" +
      "    case case2(succ = [case2Body,default_]):" +
      "        case2Body(succ = [after]);" +
      "}" +
      "after(succ = [END]);");
  }

  @Test
  public void simple_goto() {
    verifyBlockCfg("" +
      "before( succ = [fooBlock], elem = 2, syntSucc = dead );" +
      "goto fooLabel;" +
      "dead ( succ = [fooBlock], elem = 1 );" +
      "fooLabel:" +
      "fooBlock( succ = [END], elem = 2);");

    verifyBlockCfg("" +
      "fooLabel:" +
      "fooBlock( succ = [fooBlock], elem = 3, syntSucc = dead );" +
      "goto fooLabel;" +
      "dead ( succ = [END], elem = 1  );");
  }

  @Test
  public void multiple_gotos_to_same_label() {
    verifyBlockCfg("" +
      "before( succ = [fooBlock], elem = 2, syntSucc = deadOne );" +
      "goto fooLabel;" +
      "deadOne( succ = [END], elem = 2, syntSucc = fooBlock );" +
      "return;" +
      "fooLabel:" +
      "fooBlock( succ = [fooBlock], elem = 3, syntSucc = deadTwo);" +
      "goto fooLabel;" +
      "deadTwo( succ = [END], elem = 1 );");
  }

  @Test
  public void goto_nested_one_level() {
    verifyBlockCfg("" +
      "while ( cond( succ = [body, afterWhile] )) {" +
      "  body( succ = [fooBlock], syntSucc = cond );" +
      "  goto fooLabel;" +
      "}" +
      "afterWhile( succ = [fooBlock] );" +
      "fooLabel:" +
      "fooBlock( succ = [END] );");

    verifyBlockCfg("" +
      "fooLabel:" +
      "fooBlock( succ = [body] );" +
      "do {" +
      "  body( succ = [fooBlock], syntSucc = cond );" +
      "  goto fooLabel;" +
      "} while (cond( succ = [body, END] ));");
  }

  @Test
  public void goto_nested_two_levels() {
    verifyBlockCfg("" +
      "while (outerCond( succ = [innerCond, fooBlock] )) {" +
      "  while (innerCond( succ = [bodyInner, ifCond] )) {" +
      "    bodyInner( succ = [fooBlock], syntSucc = innerCond );" +
      "    goto fooLabel;" +
      "  }" +
      "  if (ifCond( succ = [bodyIf, outerCond] )) {" +
      "    bodyIf( succ = [barBlock], syntSucc = outerCond );" +
      "    goto barLabel;" +
      "  }" +
      "}" +
      "fooLabel:" +
      "fooBlock( succ = [barBlock] );" +
      "barLabel:" +
      "barBlock( succ = [END] );" +
      "stmt();");

    verifyBlockCfg("" +
      "fooLabel:" +
      "fooBlock( succ = [innerCond] );" +
      "do {" +
      "  while (innerCond( succ = [bodyInner, ifCond] )) {" +
      "    bodyInner( succ = [qixBlock], syntSucc = innerCond );" +
      "    goto qixLabel;" +
      "  }" +
      "  if (ifCond( succ = [bodyIf, qixBlock] )) {" +
      "    bodyIf( succ = [barBlock], syntSucc = qixBlock );" +
      "    goto barLabel;" +
      "  }" +
      "  qixLabel:" +
      "  qixBlock( succ = [fooBlock], syntSucc = outerCond);" +
      "  goto fooLabel;" +
      "} while ( outerCond (succ = [innerCond, barBlock])); " +
      "barLabel:" +
      "barBlock( succ = [END] );" +
      "stmt();");
  }

  @Test
  public void switch_with_break() {
    verifyBlockCfg("" +
      "before(succ = [case1]);" +
      "switch ($expr) {" +
      "    case case1(succ = [case1Body,case2]):" +
      "        case1Body(succ = [after], syntSucc = case2Body);" +
      "        break;" +
      "    case case2(succ = [case2Body,default_]):" +
      "        case2Body(succ = [after], syntSucc = default_);" +
      "        break;" +
      "    default: " +
      "        default_(succ = [after]);" +
      "}" +
      "after(succ = [END]);");

    verifyBlockCfg("" +
      "before(succ = [case1]);" +
      "switch ($expr) :" +
      "    case case1(succ = [case1Body,case2]):" +
      "        case1Body(succ = [after], syntSucc = case2Body);" +
      "        break;" +
      "    case case2(succ = [case2Body,default_]):" +
      "        case2Body(succ = [after], syntSucc = default_);" +
      "        break;" +
      "    default: " +
      "        default_(succ = [after]);" +
      "endswitch;" +
      "after(succ = [END]);");
  }

  @Test
  public void switch_statement_with_default_first_break() {
    verifyBlockCfg("" +
      "before(succ = [case1]);" +
      "switch ($expr) {" +
      "    default: " +
      "        default_(succ = [after], syntSucc = case1Body);" +
      "        break;" +
      "    case case1(succ = [case1Body, case2]):" +
      "        case1Body(succ = [after], syntSucc = case2Body);" +
      "        break;" +
      "    case case2(succ = [case2Body,default_]):" +
      "        case2Body(succ = [after], syntSucc = after);" +
      "        break;" +
      "}" +
      "after(succ = [END]);"
    );
  }


  @Test
  public void switch_statement_with_loop_and_continue() {
    verifyBlockCfg("" +
      "before(succ = [whileCond]);" +
      "while (whileCond(succ=[whileBody,after])) {" +
      "    whileBody(succ=[case1]);" +
      "    switch ($expr) {" +
      "        default: " +
      "            default_(succ = [whileBodyEnd], syntSucc = case1Body);" +
      "            continue;" +
      "        case case1(succ = [case1Body, case2]):" +
      "            case1Body(succ = [whileBodyEnd], syntSucc = case2Body);" +
      "            break;" +
      "        case case2(succ = [case2Body,case3]):" +
      "            case2Body(succ = [whileBodyEnd], syntSucc = case3Body);" +
      "            break;" +
      "        case case3(succ = [case3Body,case4]):" +
      "            case3Body(succ = [whileCond], syntSucc = case4Body);" +
      "            continue 2;" +
      "        case case4(succ = [case4Body,default_]):" +
      "            case4Body(succ = [after], syntSucc = whileBodyEnd);" +
      "            break 2;" +
      "    }" +
      "    whileBodyEnd(succ=[whileCond]);" +
      "}" +
      "after(succ = [END]);"
    );
  }

  @Test
  public void test_buildCFG() {
    CompilationUnitTree tree = parse("<?php " +
        "function foo() {" +
        "    $expr = function() {echo 'Hello';};" +
        "}" +
        "echo 'Hello';",
      PHPLexicalGrammar.COMPILATION_UNIT);
    FunctionDeclarationTree func = (FunctionDeclarationTree) tree.script().statements().get(0);
    ExpressionStatementTree expr = (ExpressionStatementTree) func.body().statements().get(0);
    ControlFlowGraph cfg = ControlFlowGraph.build(func, checkContext);
    assertThat(cfg.start().elements().get(0)).isEqualTo(expr);

    FunctionExpressionTree funcExpr = ((FunctionExpressionTree) ((AssignmentExpressionTree) expr.expression()).value());
    StatementTree echo = funcExpr.body().statements().get(0);
    cfg = ControlFlowGraph.build(funcExpr, checkContext);
    assertThat(cfg.start().elements().get(0)).isEqualTo(echo);

    StatementTree scriptEcho = tree.script().statements().get(1);
    cfg = ControlFlowGraph.build(tree.script(), checkContext);
    assertThat(cfg.start().elements().get(0)).isEqualTo(func);
    assertThat(cfg.start().elements().get(1)).isEqualTo(scriptEcho);
  }

  @Test
  public void test_buildCFG_with_method() {
    CompilationUnitTree tree = parse("<?php " +
        "class A {" +
        "    function foo() {" +
        "        echo 'Hello';" +
        "    }" +
        "    abstract function bar();" +
        "}",
      PHPLexicalGrammar.COMPILATION_UNIT);
    ClassDeclarationTree cls = (ClassDeclarationTree) tree.script().statements().get(0);
    MethodDeclarationTree method = (MethodDeclarationTree) cls.members().get(0);
    StatementTree echo = ((BlockTree) method.body()).statements().get(0);

    ControlFlowGraph cfg = ControlFlowGraph.build(method, checkContext);
    assertThat(cfg.start().elements().get(0)).isEqualTo(echo);

    MethodDeclarationTree abstractMethod = (MethodDeclarationTree) cls.members().get(1);
    cfg = ControlFlowGraph.build(abstractMethod, checkContext);
    assertThat(cfg).isNull();
  }

  @Test
  public void test_cfg_failure_logs() {
    CompilationUnitTree tree = parse("<?php\n" +
        "function foo() {\n" +
        "     break;\n" +
        "}\n",
      PHPLexicalGrammar.COMPILATION_UNIT);
    FunctionDeclarationTree func = (FunctionDeclarationTree) tree.script().statements().get(0);
    ControlFlowGraph cfg = ControlFlowGraph.build(func, checkContext);
    assertThat(cfg).isNull();

    assertThat(logTester.logs(LoggerLevel.WARN)).contains("Failed to build control flow graph for file [mock.php] at line 2");
    assertThat(logTester.logs(LoggerLevel.WARN)).contains("Failed to build CFG");
    logTester.clear();

    // testing mechanism avoiding reporting failure multiple times for the same tree
    cfg = ControlFlowGraph.build(func, checkContext);
    assertThat(cfg).isNull();
    assertThat(logTester.logs()).isEmpty();
  }

  private void verifyBlockCfg(String functionBody) {
    Validator.assertCfgStructure(cfgForBlock(functionBody));
  }

  private void verifyScriptTreeCfg(String body) {
    ScriptTree tree = parse("<?php " + body, PHPLexicalGrammar.SCRIPT);
    Validator.assertCfgStructure(ControlFlowGraph.build(tree));
  }

  private ControlFlowGraph cfgForBlock(String functionBody) {
    FunctionTree functionTree = parse("function f() { " + functionBody + " }", PHPLexicalGrammar.FUNCTION_DECLARATION);
    return ControlFlowGraph.build((BlockTree) functionTree.body());
  }
}
