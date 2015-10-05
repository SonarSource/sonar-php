/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.impl.expression;

import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExitTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;

public class ExitTreeImpl extends PHPTree implements ExitTree {

  private static final Kind KIND = Kind.EXIT_EXPRESSION;
  private InternalSyntaxToken wordToken;
  @Nullable
  private final InternalSyntaxToken openParenthesis;
  @Nullable
  private final ExpressionTree expression;
  @Nullable
  private final InternalSyntaxToken closeParenthesis;

  public ExitTreeImpl(InternalSyntaxToken openParenthesis, ExpressionTree expression, InternalSyntaxToken closeParenthesis) {
    this.openParenthesis = openParenthesis;
    this.expression = expression;
    this.closeParenthesis = closeParenthesis;
  }

  public ExitTreeImpl(InternalSyntaxToken wordToken) {
    this.wordToken = wordToken;
    this.openParenthesis = null;
    this.expression = null;
    this.closeParenthesis = null;
  }

  public ExitTreeImpl complete(InternalSyntaxToken workToken) {
    this.wordToken = workToken;

    return this;
  }

  @Override
  public SyntaxToken wordToken() {
    return wordToken;
  }

  @Nullable
  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesis;
  }

  @Nullable
  @Override
  public ExpressionTree parameterExpression() {
    return expression;
  }

  @Nullable
  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesis;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(wordToken, openParenthesis, expression, closeParenthesis);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitExit(this);
  }

}
