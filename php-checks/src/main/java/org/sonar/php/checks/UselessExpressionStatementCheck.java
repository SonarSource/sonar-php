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
package org.sonar.php.checks;

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = UselessExpressionStatementCheck.KEY)
public class UselessExpressionStatementCheck extends PHPVisitorCheck {

  public static final String KEY = "S905";
  private static final String MESSAGE = "Remove or refactor this statement.";

  private static final Tree.Kind[] USELESS_KINDS = {
    Kind.FUNCTION_EXPRESSION,
    Kind.EQUAL_TO,
    Kind.STRICT_EQUAL_TO,
    Kind.NOT_EQUAL_TO,
    Kind.STRICT_NOT_EQUAL_TO,
    Kind.LESS_THAN,
    Kind.GREATER_THAN,
    Kind.LESS_THAN_OR_EQUAL_TO,
    Kind.GREATER_THAN_OR_EQUAL_TO,
    Kind.PLUS,
    Kind.MINUS,
    Kind.REMAINDER,
    Kind.MULTIPLY,
    Kind.DIVIDE,
    Kind.LEFT_SHIFT,
    Kind.RIGHT_SHIFT,
    Kind.INSTANCE_OF,
    Kind.ALTERNATIVE_NOT_EQUAL_TO,

    Kind.UNARY_MINUS,
    Kind.UNARY_PLUS,
    Kind.LOGICAL_COMPLEMENT,

    Kind.REGULAR_STRING_LITERAL,
    Kind.EXPANDABLE_STRING_LITERAL,
    Kind.CONCATENATION,
    Kind.NAME_IDENTIFIER,
    Kind.NUMERIC_LITERAL,
    Kind.NULL_LITERAL,
    Kind.BOOLEAN_LITERAL
  };

  private boolean fileContainsHTML;
  private List<Tree> uselessNodes;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    this.fileContainsHTML = false;
    uselessNodes = new ArrayList<>();

    super.visitCompilationUnit(tree);

    if (!fileContainsHTML) {
      for (Tree uselessNode : uselessNodes) {
        context().newIssue(this, uselessNode, MESSAGE);
      }
    }
  }

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    ExpressionTree expression = tree.expression();
    if (expression.is(USELESS_KINDS)) {
      uselessNodes.add(tree);
    }

    super.visitExpressionStatement(tree);
  }

  @Override
  public void visitToken(SyntaxToken token) {
    if (token.is(Kind.INLINE_HTML_TOKEN) && !CheckUtils.isClosingTag(token)) {
      fileContainsHTML = true;
    }
  }
}
