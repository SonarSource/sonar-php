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
package org.sonar.php.tree.impl.statement;

import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ForEachStatementTreeImpl extends PHPTree implements ForEachStatementTree {

  private final Kind KIND;

  private final InternalSyntaxToken forEachToken;
  private final InternalSyntaxToken openParenthesisToken;
  private final ExpressionTree expression;
  private final InternalSyntaxToken asToken;
  private final ExpressionTree key;
  private final InternalSyntaxToken doubleArrowToken;
  private final ExpressionTree value;
  private final InternalSyntaxToken closeParenthesisToken;
  private final InternalSyntaxToken colonToken;
  private final List<StatementTree> statement;
  private final InternalSyntaxToken endforeachToken;
  private final InternalSyntaxToken eosToken;

  public ForEachStatementTreeImpl(
      InternalSyntaxToken forEachToken, InternalSyntaxToken openParenthesisToken,
      ExpressionTree expression, InternalSyntaxToken asToken, @Nullable ExpressionTree key, @Nullable InternalSyntaxToken doubleArrowToken, ExpressionTree value,
      InternalSyntaxToken closeParenthesisToken, StatementTree statement
  ) {
    this(
        Kind.FOREACH_STATEMENT,
        forEachToken, openParenthesisToken,
        expression, asToken, key, doubleArrowToken, value,
        closeParenthesisToken,
        null, Collections.singletonList(statement), null, null
    );
  }

  public ForEachStatementTreeImpl(
      InternalSyntaxToken forEachToken, InternalSyntaxToken openParenthesisToken,
      ExpressionTree expression, InternalSyntaxToken asToken, @Nullable ExpressionTree key, @Nullable InternalSyntaxToken doubleArrowToken, ExpressionTree value,
      InternalSyntaxToken closeParenthesisToken,
      InternalSyntaxToken colonToken, List<StatementTree> statements, InternalSyntaxToken endForEachToken, InternalSyntaxToken eosToken
  ) {
    this(
        Kind.ALTERNATIVE_FOREACH_STATEMENT,
        forEachToken, openParenthesisToken,
        expression, asToken, key, doubleArrowToken, value,
        closeParenthesisToken,
        colonToken, statements, endForEachToken, eosToken
    );
  }

  private ForEachStatementTreeImpl(
      Kind kind,
      InternalSyntaxToken forEachToken, InternalSyntaxToken openParenthesisToken,
      ExpressionTree expression, InternalSyntaxToken asToken, @Nullable ExpressionTree key, @Nullable InternalSyntaxToken doubleArrowToken, ExpressionTree value,
      InternalSyntaxToken closeParenthesisToken, @Nullable InternalSyntaxToken colonToken,
      List<StatementTree> statements, @Nullable InternalSyntaxToken endForEachToken, @Nullable InternalSyntaxToken eosToken
  ) {
    this.forEachToken = forEachToken;
    this.openParenthesisToken = openParenthesisToken;
    this.expression = expression;
    this.asToken = asToken;
    this.key = key;
    this.doubleArrowToken = doubleArrowToken;
    this.value = value;
    this.closeParenthesisToken = closeParenthesisToken;

    this.colonToken = colonToken;
    this.statement = statements;
    this.endforeachToken = endForEachToken;
    this.eosToken = eosToken;

    this.KIND = kind;
  }

  @Override
  public SyntaxToken foreachToken() {
    return forEachToken;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public SyntaxToken asToken() {
    return asToken;
  }

  @Nullable
  @Override
  public ExpressionTree key() {
    return key;
  }

  @Nullable
  @Override
  public SyntaxToken doubleArrowToken() {
    return doubleArrowToken;
  }

  @Override
  public ExpressionTree value() {
    return value;
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesisToken;
  }

  @Nullable
  @Override
  public SyntaxToken colonToken() {
    return colonToken;
  }

  @Override
  public List<StatementTree> statement() {
    return statement;
  }

  @Nullable
  @Override
  public SyntaxToken endforeachToken() {
    return endforeachToken;
  }

  @Nullable
  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
        Iterators.forArray(forEachToken, openParenthesisToken, expression, asToken, key, doubleArrowToken, value, closeParenthesisToken, colonToken),
        statement.iterator(),
        Iterators.forArray(endforeachToken, eosToken));
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitForEachStatement(this);
  }
}
