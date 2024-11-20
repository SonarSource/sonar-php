/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = UselessExpressionStatementCheck.KEY)
public class UselessExpressionStatementCheck extends PHPVisitorCheck {

  public static final String KEY = "S905";
  private static final String MESSAGE = "Remove or refactor this statement.";
  private static final String MESSAGE_PARTIAL = "This statement part is useless, remove or refactor it.";
  private static final String MESSAGE_OPERATOR = "This binary operation is useless, remove it.";

  private static final Pattern STRING_LITERAL_EXCEPTION_PATTERN = Pattern.compile("@phan-var");
  private static final Tree.Kind[] BINARY_KINDS = {
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
    Kind.CONCATENATION,

    // Not supported binary operations:
    // - BITWISE_AND
    // - BITWISE_XOR
    // - BITWISE_OR
    // - CONDITIONAL_AND
    // - ALTERNATIVE_CONDITIONAL_AND
    // - CONDITIONAL_OR
    // - ALTERNATIVE_CONDITIONAL_OR
    // - INSTANCE_OF
  };
  private static final Tree.Kind[] USELESS_KINDS = {
    Kind.ARROW_FUNCTION_EXPRESSION,
    Kind.FUNCTION_EXPRESSION,

    Kind.UNARY_MINUS,
    Kind.UNARY_PLUS,
    Kind.LOGICAL_COMPLEMENT,

    Kind.EXPANDABLE_STRING_LITERAL,
    Kind.NAME_IDENTIFIER,
    Kind.NUMERIC_LITERAL,
    Kind.NULL_LITERAL,
    Kind.BOOLEAN_LITERAL
  };

  private boolean fileContainsHTML;
  private List<Tree> uselessNodes;
  private List<Pair<Tree, Tree>> uselessPartialNodes;
  private List<Tree> uselessOperatorNodes;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    this.fileContainsHTML = false;
    uselessNodes = new ArrayList<>();
    uselessPartialNodes = new ArrayList<>();
    uselessOperatorNodes = new ArrayList<>();

    super.visitCompilationUnit(tree);

    if (!fileContainsHTML) {
      for (Tree uselessNode : uselessNodes) {
        context().newIssue(this, uselessNode, MESSAGE);
      }
      for (Pair<Tree, Tree> uselessPartialNode : uselessPartialNodes) {
        context().newIssue(this, uselessPartialNode.getLeft(), uselessPartialNode.getRight(), MESSAGE_PARTIAL);
      }
      for (Tree uselessOperatorNode : uselessOperatorNodes) {
        context().newIssue(this, uselessOperatorNode, MESSAGE_OPERATOR);
      }
    }
  }

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    ExpressionTree expression = tree.expression();
    if (expression.is(BINARY_KINDS)) {
      checkBinaryExpression((BinaryExpressionTree) expression);
    }

    if (expression.is(USELESS_KINDS)) {
      uselessNodes.add(tree);
    }

    if (expression.is(Kind.REGULAR_STRING_LITERAL) && !STRING_LITERAL_EXCEPTION_PATTERN.matcher(((LiteralTree) expression).value()).find()) {
      uselessNodes.add(tree);
    }

    super.visitExpressionStatement(tree);
  }

  private void checkBinaryExpression(BinaryExpressionTree binaryExpression) {
    boolean isLeftUseless = isUseless(binaryExpression.leftOperand());
    boolean isRightUseless = isUseless(binaryExpression.rightOperand());
    if (isLeftUseless && isRightUseless) {
      uselessNodes.add(binaryExpression);
    } else if (isLeftUseless) {
      uselessPartialNodes.add(Pair.of(binaryExpression.leftOperand(), binaryExpression.operator()));
    } else if (isRightUseless) {
      uselessPartialNodes.add(Pair.of(binaryExpression.operator(), binaryExpression.rightOperand()));
    } else {
      uselessOperatorNodes.add(binaryExpression.operator());
    }
  }

  private static boolean isUseless(ExpressionTree expression) {
    if (expression.is(Kind.FUNCTION_CALL)) {
      return false;
    }
    if (expression.is(BINARY_KINDS)) {
      BinaryExpressionTree binaryExpression = (BinaryExpressionTree) expression;
      return isUseless(binaryExpression.leftOperand()) && isUseless(binaryExpression.rightOperand());
    }
    return true;
  }

  @Override
  public void visitToken(SyntaxToken token) {
    if (token.is(Kind.INLINE_HTML_TOKEN) && !CheckUtils.isClosingTag(token)) {
      fileContainsHTML = true;
    }
  }
}
