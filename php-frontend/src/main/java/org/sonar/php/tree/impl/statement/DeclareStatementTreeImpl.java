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
package org.sonar.php.tree.impl.statement;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.DeclareStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class DeclareStatementTreeImpl extends PHPTree implements DeclareStatementTree {

  private static final Kind KIND = Kind.DECLARE_STATEMENT;

  private final DeclareStatementHead declareStatementHead;

  private final SyntaxToken colonToken;
  private final List<StatementTree> statements;
  private final SyntaxToken endDeclareToken;
  private final SyntaxToken eosToken;

  public DeclareStatementTreeImpl(DeclareStatementHead declareStatementHead, InternalSyntaxToken eosToken) {
    this.declareStatementHead = declareStatementHead;

    this.colonToken = null;
    this.statements = Collections.emptyList();
    this.endDeclareToken = null;

    this.eosToken = eosToken;
  }

  public DeclareStatementTreeImpl(DeclareStatementHead declareStatementHead, StatementTree statement) {
    this.declareStatementHead = declareStatementHead;
    this.statements = Collections.singletonList(statement);

    this.colonToken = null;
    this.endDeclareToken = null;
    this.eosToken = null;
  }

  public DeclareStatementTreeImpl(
    DeclareStatementHead declareStatementHead, InternalSyntaxToken colonToken,
    List<StatementTree> statements,
    InternalSyntaxToken enddeclareToken, InternalSyntaxToken eosToken) {
    this.declareStatementHead = declareStatementHead;
    this.colonToken = colonToken;
    this.statements = statements;
    this.endDeclareToken = enddeclareToken;
    this.eosToken = eosToken;
  }

  @Override
  public SyntaxToken declareToken() {
    return declareStatementHead.declareToken();
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return declareStatementHead.openParenthesisToken();
  }

  @Override
  public SeparatedListImpl<VariableDeclarationTree> directives() {
    return declareStatementHead.directives();
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return declareStatementHead.closeParenthesisToken();
  }

  @Nullable
  @Override
  public SyntaxToken colonToken() {
    return colonToken;
  }

  @Override
  public List<StatementTree> statements() {
    return statements;
  }

  @Nullable
  @Override
  public SyntaxToken endDeclareToken() {
    return endDeclareToken;
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
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(declareStatementHead.declareToken(), declareStatementHead.openParenthesisToken()),
      declareStatementHead.directives().elementsAndSeparators(),
      IteratorUtils.iteratorOf(declareStatementHead.closeParenthesisToken(), colonToken),
      statements.iterator(),
      IteratorUtils.iteratorOf(endDeclareToken, eosToken));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitDeclareStatement(this);
  }

  public static class DeclareStatementHead {

    private final SyntaxToken declareToken;
    private final SyntaxToken openParenthesisToken;
    private final SeparatedListImpl<VariableDeclarationTree> directives;
    private final SyntaxToken closeParenthesisToken;

    public DeclareStatementHead(
      SyntaxToken declareToken,
      SyntaxToken openParenthesisToken,
      SeparatedListImpl<VariableDeclarationTree> directives,
      SyntaxToken closeParenthesisToken) {
      this.declareToken = declareToken;
      this.openParenthesisToken = openParenthesisToken;
      this.directives = directives;
      this.closeParenthesisToken = closeParenthesisToken;
    }

    public SyntaxToken declareToken() {
      return declareToken;
    }

    public SyntaxToken openParenthesisToken() {
      return openParenthesisToken;
    }

    public SeparatedListImpl<VariableDeclarationTree> directives() {
      return directives;
    }

    public SyntaxToken closeParenthesisToken() {
      return closeParenthesisToken;
    }
  }
}
