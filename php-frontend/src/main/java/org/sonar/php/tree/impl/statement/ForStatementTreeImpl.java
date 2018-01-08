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
package org.sonar.php.tree.impl.statement;

import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ForStatementTreeImpl extends PHPTree implements ForStatementTree {

  private final Kind kind;

  private final ForStatementHeader header;
  private final InternalSyntaxToken colonToken;
  private final List<StatementTree> statements;
  private final InternalSyntaxToken endForToken;
  private final InternalSyntaxToken eosToken;

  private ForStatementTreeImpl(
      Kind kind,
      ForStatementHeader header,
      @Nullable InternalSyntaxToken colonToken, List<StatementTree> statements,
      @Nullable InternalSyntaxToken endForToken, @Nullable InternalSyntaxToken eosToken
  ) {
    this.header = header;
    this.colonToken = colonToken;
    this.endForToken = endForToken;
    this.statements = statements;
    this.eosToken = eosToken;

    this.kind = kind;
  }

  public ForStatementTreeImpl(
      ForStatementHeader header, InternalSyntaxToken colonToken,
      List<StatementTree> statements,
      InternalSyntaxToken endForToken, InternalSyntaxToken eosToken
  ) {
    this(Kind.ALTERNATIVE_FOR_STATEMENT, header, colonToken, statements, endForToken, eosToken);
  }

  public ForStatementTreeImpl(ForStatementHeader header, StatementTree statement) {
    this(Kind.FOR_STATEMENT, header, null, Collections.singletonList(statement), null,  null);
  }

  @Override
  public SyntaxToken forToken() {
    return header.forToken();
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return header.openParenthesisToken();
  }

  @Override
  public SeparatedListImpl<ExpressionTree> init() {
    return header.init();
  }

  @Override
  public SyntaxToken firstSemicolonToken() {
    return header.firstSemicolonToken();
  }

  @Override
  public SeparatedListImpl<ExpressionTree> condition() {
    return header.condition();
  }

  @Override
  public SyntaxToken secondSemicolonToken() {
    return header.secondSemicolonToken();
  }

  @Override
  public SeparatedListImpl<ExpressionTree> update() {
    return header.update();
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return header.closeParenthesisToken();
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
  public SyntaxToken endforToken() {
    return endForToken;
  }

  @Nullable
  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
        Iterators.forArray(forToken(), openParenthesisToken()),
        init().elementsAndSeparators(),
        Iterators.singletonIterator(firstSemicolonToken()),
        condition().elementsAndSeparators(),
        Iterators.singletonIterator(secondSemicolonToken()),
        update().elementsAndSeparators(),
        Iterators.forArray(closeParenthesisToken(), colonToken),
        statements.iterator(),
        Iterators.forArray(endForToken, eosToken));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitForStatement(this);
  }

  /**
   * Utility class hidden from API (it's mainly created to avoid duplication in grammar)
   */
  public static class ForStatementHeader {

    private final InternalSyntaxToken forToken;
    private final InternalSyntaxToken openParenthesisToken;
    private final SeparatedListImpl<ExpressionTree> init;
    private final InternalSyntaxToken firstSemicolonToken;
    private final SeparatedListImpl<ExpressionTree> condition;
    private final InternalSyntaxToken secondSemicolonToken;
    private final SeparatedListImpl<ExpressionTree> update;
    private final InternalSyntaxToken closeParenthesisToken;

    public ForStatementHeader(
        InternalSyntaxToken forToken, InternalSyntaxToken openParenthesisToken,
        SeparatedListImpl<ExpressionTree> init, InternalSyntaxToken firstSemicolonToken,
        SeparatedListImpl<ExpressionTree> condition, InternalSyntaxToken secondSemicolonToken,
        SeparatedListImpl<ExpressionTree> update, InternalSyntaxToken closeParenthesisToken
    ) {
      this.forToken = forToken;
      this.openParenthesisToken = openParenthesisToken;
      this.init = init;
      this.firstSemicolonToken = firstSemicolonToken;
      this.condition = condition;
      this.secondSemicolonToken = secondSemicolonToken;
      this.update = update;
      this.closeParenthesisToken = closeParenthesisToken;
    }

    public InternalSyntaxToken forToken() {
      return forToken;
    }

    public InternalSyntaxToken openParenthesisToken() {
      return openParenthesisToken;
    }

    public SeparatedListImpl<ExpressionTree> init() {
      return init;
    }

    public InternalSyntaxToken firstSemicolonToken() {
      return firstSemicolonToken;
    }

    public SeparatedListImpl<ExpressionTree> condition() {
      return condition;
    }

    public InternalSyntaxToken secondSemicolonToken() {
      return secondSemicolonToken;
    }

    public SeparatedListImpl<ExpressionTree> update() {
      return update;
    }

    public InternalSyntaxToken closeParenthesisToken() {
      return closeParenthesisToken;
    }
  }
}
