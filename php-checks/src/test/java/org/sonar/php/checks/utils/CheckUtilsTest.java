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
package org.sonar.php.checks.utils;

import com.sonar.sslr.api.typed.ActionParser;
import com.sonarsource.checks.coverage.UtilityClass;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckUtilsTest {

  private ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);

  @Test
  public void utility_class() throws Exception {
    UtilityClass.assertGoodPractice(CheckUtils.class);
  }

  @Test
  public void skipParenthesis() throws Exception {
    ExpressionTree expr;

    expr = expressionFromStatement("42;");
    assertThat(CheckUtils.skipParenthesis(expr)).isEqualTo(expr);

    expr = expressionFromStatement("(42);");
    assertThat(CheckUtils.skipParenthesis(expr)).isEqualTo(((ParenthesisedExpressionTree) expr).expression());

    expr = expressionFromStatement("((((((42))))));");
    assertThat(CheckUtils.skipParenthesis(expr).is(Tree.Kind.NUMERIC_LITERAL)).isTrue();
    assertThat(((LiteralTree) CheckUtils.skipParenthesis(expr)).value()).isEqualTo("42");
  }

  @Test
  public void function_name() throws Exception {
    ExpressionTree root = expressionFromStatement("A::run(2);");
    assertThat(root.is(Tree.Kind.FUNCTION_CALL)).isTrue();
    FunctionCallTree call = (FunctionCallTree) root;
    assertThat(CheckUtils.getFunctionName(call)).isEqualTo("A::run");
  }

  @Test
  public void no_function_name() throws Exception {
    ExpressionTree root = expressionFromStatement("$name(2);");
    assertThat(root.is(Tree.Kind.FUNCTION_CALL)).isTrue();
    FunctionCallTree call = (FunctionCallTree) root;
    assertThat(CheckUtils.getFunctionName(call)).isNull();
  }

  @Test
  public void previous_sibling() throws Exception {
    BinaryExpressionTree expression = (BinaryExpressionTree) expressionFromStatement("1 + 1;");
    assertThat(CheckUtils.findPreviousSibling(expression.rightOperand())).isSameAs(expression.operator());
    assertThat(CheckUtils.findPreviousSibling(expression.operator())).isSameAs(expression.leftOperand());
    assertThat(CheckUtils.findPreviousSibling(expression.leftOperand())).isNull();
    assertThat(CheckUtils.findPreviousSibling(expression.getParent())).isNull();
  }

  @Test
  public void previous_token() throws Exception {
    BinaryExpressionTree expression = (BinaryExpressionTree) expressionFromStatement("1 + 1;");
    assertThat(CheckUtils.findPreviousToken(expression.rightOperand())).isSameAs(expression.operator());
  }

  @Test
  public void echo_tag() throws Exception {
    Map<String, Boolean> actual = new HashMap<>();
    PHPParserBuilder.createParser().parse(
      "<?php\n"
        + " ?>Title <?= 'A' ?><?= 'B', 'C' ?> <?php\n"
        + " echo 'D';\n"
        + " ?><?= 'E' ?> <?php\n"
        + " echo 'F', 'G';\n"
        + " 'H';"
        + "")
      .accept(new PHPVisitorCheck() {
        @Override
        public void visitLiteral(LiteralTree tree) {
          actual.put(tree.token().text(), CheckUtils.isDisguisedShortEchoStatement(tree.getParent()));
          super.visitLiteral(tree);
        }
      });
    Map<String, Boolean> expected = new HashMap<>();
    expected.put("'A'", true);
    expected.put("'B'", true);
    expected.put("'C'", true);
    expected.put("'D'", false);
    expected.put("'E'", true);
    expected.put("'F'", false);
    expected.put("'G'", false);
    expected.put("'H'", false);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void not_after_echo_tag() throws Exception {
    Tree tree = PHPParserBuilder.createParser().parse("<?php ?><?php 'str'; return 0;");
    List<StatementTree> statements = ((CompilationUnitTree) tree).script().statements();
    assertThat(CheckUtils.isDisguisedShortEchoStatement(statements.get(0))).isEqualTo(false);
    assertThat(CheckUtils.isDisguisedShortEchoStatement(statements.get(1))).isEqualTo(false);

    tree = PHPParserBuilder.createParser(PHPLexicalGrammar.STATEMENT).parse("'str';");
    assertThat(CheckUtils.isDisguisedShortEchoStatement(tree)).isEqualTo(false);
  }

  @Test
  public void start_with_echo_tag() throws Exception {
    Tree tree = PHPParserBuilder.createParser().parse("<?= 2 ?>");
    List<StatementTree> statements = ((CompilationUnitTree) tree).script().statements();
    assertThat(CheckUtils.isDisguisedShortEchoStatement(statements.get(0))).isEqualTo(true);

    tree = PHPParserBuilder.createParser().parse("<?php 2 ?>");
    statements = ((CompilationUnitTree) tree).script().statements();
    assertThat(CheckUtils.isDisguisedShortEchoStatement(statements.get(0))).isEqualTo(false);
  }

  @Test
  public void for_condition() throws Exception {
    Tree tree = PHPParserBuilder.createParser().parse("<?= for(;;) {} ?>");
    ForStatementTree forStatement = (ForStatementTree) ((CompilationUnitTree) tree).script().statements().get(0);
    assertThat(CheckUtils.getForCondition(forStatement)).isNull();

    tree = PHPParserBuilder.createParser().parse("<?= for(;true;) {} ?>");
    forStatement = (ForStatementTree) ((CompilationUnitTree) tree).script().statements().get(0);
    assertThat(CheckUtils.getForCondition(forStatement).getKind()).isEqualTo(Tree.Kind.BOOLEAN_LITERAL);

    tree = PHPParserBuilder.createParser().parse("<?= for(;$a == 0, true;) {} ?>");
    forStatement = (ForStatementTree) ((CompilationUnitTree) tree).script().statements().get(0);
    assertThat(CheckUtils.getForCondition(forStatement).getKind()).isEqualTo(Tree.Kind.BOOLEAN_LITERAL);
  }

  private ExpressionTree expressionFromStatement(String statement) {
    return ((ExpressionStatementTree) parse(statement)).expression();
  }

  private Tree parse(String toParse) {
    return parser.parse(toParse);
  }

}
