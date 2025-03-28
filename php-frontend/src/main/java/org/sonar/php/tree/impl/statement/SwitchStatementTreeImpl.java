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
package org.sonar.php.tree.impl.statement;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class SwitchStatementTreeImpl extends PHPTree implements SwitchStatementTree {

  private final Kind kind;

  private final InternalSyntaxToken switchToken;
  private final ParenthesisedExpressionTree expression;
  private final InternalSyntaxToken openCurlyBraceToken;
  private final InternalSyntaxToken colonToken;
  private final InternalSyntaxToken semicolonToken;
  private final List<SwitchCaseClauseTree> cases;
  private final InternalSyntaxToken closeCurlyBraceToken;
  private final InternalSyntaxToken endswitchToken;
  private final InternalSyntaxToken eosToken;

  public SwitchStatementTreeImpl(
    InternalSyntaxToken switchToken, ParenthesisedExpressionTree expression, InternalSyntaxToken openCurlyBraceToken,
    @Nullable InternalSyntaxToken semicolonToken,
    List<SwitchCaseClauseTree> cases,
    InternalSyntaxToken closeCurlyBraceToken) {
    this.kind = Kind.SWITCH_STATEMENT;

    this.switchToken = switchToken;
    this.expression = expression;
    this.openCurlyBraceToken = openCurlyBraceToken;
    this.semicolonToken = semicolonToken;
    this.cases = cases;
    this.closeCurlyBraceToken = closeCurlyBraceToken;

    this.colonToken = null;
    this.endswitchToken = null;
    this.eosToken = null;
  }

  public SwitchStatementTreeImpl(
    InternalSyntaxToken switchToken, ParenthesisedExpressionTree expression, InternalSyntaxToken colonToken,
    @Nullable InternalSyntaxToken semicolonToken,
    List<SwitchCaseClauseTree> cases,
    InternalSyntaxToken endswitchToken, InternalSyntaxToken eosToken) {
    this.kind = Kind.ALTERNATIVE_SWITCH_STATEMENT;

    this.switchToken = switchToken;
    this.expression = expression;
    this.openCurlyBraceToken = null;
    this.semicolonToken = semicolonToken;
    this.cases = cases;
    this.closeCurlyBraceToken = null;

    this.colonToken = colonToken;
    this.endswitchToken = endswitchToken;
    this.eosToken = eosToken;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(switchToken, expression, openCurlyBraceToken, colonToken, semicolonToken),
      cases.iterator(),
      IteratorUtils.iteratorOf(closeCurlyBraceToken, endswitchToken, eosToken));
  }

  @Override
  public SyntaxToken switchToken() {
    return switchToken;
  }

  @Override
  public ParenthesisedExpressionTree expression() {
    return expression;
  }

  @Nullable
  @Override
  public SyntaxToken openCurlyBraceToken() {
    return openCurlyBraceToken;
  }

  @Nullable
  @Override
  public SyntaxToken colonToken() {
    return colonToken;
  }

  @Nullable
  @Override
  public SyntaxToken semicolonToken() {
    return semicolonToken;
  }

  @Override
  public List<SwitchCaseClauseTree> cases() {
    return cases;
  }

  @Nullable
  @Override
  public SyntaxToken closeCurlyBraceToken() {
    return closeCurlyBraceToken;
  }

  @Nullable
  @Override
  public SyntaxToken endswitchToken() {
    return endswitchToken;
  }

  @Nullable
  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitSwitchStatement(this);
  }
}
