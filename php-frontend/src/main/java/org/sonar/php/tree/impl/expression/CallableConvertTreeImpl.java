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
package org.sonar.php.tree.impl.expression;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.CallableConvertTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class CallableConvertTreeImpl extends PHPTree implements CallableConvertTree {

  private ExpressionTree expression;
  private final SyntaxToken openParenthesisToken;
  private final SyntaxToken ellipsisToken;
  private final SyntaxToken closeParenthesisToken;

  public CallableConvertTreeImpl(SyntaxToken openParenthesisToken, SyntaxToken ellipsisToken, SyntaxToken closeParenthesisToken) {
    this.openParenthesisToken = openParenthesisToken;
    this.ellipsisToken = ellipsisToken;
    this.closeParenthesisToken = closeParenthesisToken;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  @Override
  public SyntaxToken ellipsisToken() {
    return ellipsisToken;
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesisToken;
  }

  public CallableConvertTreeImpl complete(ExpressionTree expression) {
    this.expression = expression;
    return this;
  }

  @Override
  public Kind getKind() {
    return Kind.CALLABLE_CONVERT;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(expression, openParenthesisToken, ellipsisToken, closeParenthesisToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitCallableConvert(this);
  }
}
