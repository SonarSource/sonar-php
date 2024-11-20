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
package org.sonar.php.tree.impl.statement;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.UnsetVariableStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class UnsetVariableStatementTreeImpl extends PHPTree implements UnsetVariableStatementTree {

  private static final Kind KIND = Kind.UNSET_VARIABLE_STATEMENT;

  private final InternalSyntaxToken unsetToken;
  private final InternalSyntaxToken openParenthesisToken;
  private final SeparatedListImpl<ExpressionTree> variables;
  private final InternalSyntaxToken closeParenthesisToken;
  private final InternalSyntaxToken eosToken;

  public UnsetVariableStatementTreeImpl(
    InternalSyntaxToken unsetToken, InternalSyntaxToken openParenthesisToken,
    SeparatedListImpl<ExpressionTree> variables,
    InternalSyntaxToken closeParenthesisToken, InternalSyntaxToken eosToken) {
    this.unsetToken = unsetToken;
    this.openParenthesisToken = openParenthesisToken;
    this.variables = variables;
    this.closeParenthesisToken = closeParenthesisToken;
    this.eosToken = eosToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(unsetToken, openParenthesisToken),
      variables.elementsAndSeparators(),
      IteratorUtils.iteratorOf(closeParenthesisToken, eosToken));
  }

  @Override
  public SyntaxToken unsetToken() {
    return unsetToken;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  @Override
  public SeparatedListImpl<ExpressionTree> variables() {
    return variables;
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesisToken;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitUnsetVariableStatement(this);

  }
}
