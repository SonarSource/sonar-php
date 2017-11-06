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
package org.sonar.php.checks.utils;

import com.sonar.sslr.api.typed.ActionParser;
import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckUtilsTest {

  private ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);

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

  private ExpressionTree expressionFromStatement(String statement) {
    return ((ExpressionStatementTree) parse(statement)).expression();
  }

  private Tree parse(String toParse) {
    return parser.parse(toParse);
  }

}
