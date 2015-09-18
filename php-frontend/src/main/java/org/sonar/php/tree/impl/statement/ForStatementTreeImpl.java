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

import com.google.common.base.Functions;
import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedList;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ForStatementTreeImpl extends PHPTree implements ForStatementTree {

  private final Kind KIND;

  private final ForStatementHeader header;
  private final InternalSyntaxToken colonToken;
  private final List<StatementTree> statement;
  private final InternalSyntaxToken endForToken;
  private final InternalSyntaxToken eosToken;

  private ForStatementTreeImpl(
      Kind kind,
      ForStatementHeader header,
      @Nullable InternalSyntaxToken colonToken, List<StatementTree> statement,
      @Nullable InternalSyntaxToken endForToken, @Nullable InternalSyntaxToken eosToken
  ) {
    this.header = header;
    this.colonToken = colonToken;
    this.endForToken = endForToken;
    this.statement = statement;
    this.eosToken = eosToken;

    this.KIND = kind;
  }

  public ForStatementTreeImpl(ForStatementHeader header, InternalSyntaxToken colonToken, List<StatementTree> statement, InternalSyntaxToken endForToken, InternalSyntaxToken eosToken) {
    this(Kind.ALTERNATIVE_FOR_STATEMENT, header, colonToken, statement, endForToken, eosToken);
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
  public SeparatedList<ExpressionTree> init() {
    return header.init();
  }

  @Override
  public SyntaxToken firstSemicolonToken() {
    return header.firstSemicolonToken();
  }

  @Override
  public SeparatedList<ExpressionTree> condition() {
    return header.condition();
  }

  @Override
  public SyntaxToken secondSemicolonToken() {
    return header.secondSemicolonToken();
  }

  @Override
  public SeparatedList<ExpressionTree> update() {
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
  public List<StatementTree> statement() {
    return statement;
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
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
        Iterators.forArray(forToken(), openParenthesisToken()),
        init().elementsAndSeparators(Functions.<ExpressionTree>identity()),
        Iterators.singletonIterator(firstSemicolonToken()),
        condition().elementsAndSeparators(Functions.<ExpressionTree>identity()),
        Iterators.singletonIterator(secondSemicolonToken()),
        update().elementsAndSeparators(Functions.<ExpressionTree>identity()),
        Iterators.forArray(closeParenthesisToken(), colonToken),
        statement.iterator(),
        Iterators.forArray(endForToken, eosToken));
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitForStatement(this);
  }

  /**
   * Utility class hidden from API (it's mainly created to avoid duplication in grammar)
   */
  public static class ForStatementHeader implements Tree {

    private final InternalSyntaxToken forToken;
    private final InternalSyntaxToken openParenthesisToken;
    private final SeparatedList<ExpressionTree> init;
    private final InternalSyntaxToken firstSemicolonToken;
    private final SeparatedList<ExpressionTree> condition;
    private final InternalSyntaxToken secondSemicolonToken;
    private final SeparatedList<ExpressionTree> update;
    private final InternalSyntaxToken closeParenthesisToken;

    public ForStatementHeader(
        InternalSyntaxToken forToken, InternalSyntaxToken openParenthesisToken,
        SeparatedList<ExpressionTree> init, InternalSyntaxToken firstSemicolonToken,
        SeparatedList<ExpressionTree> condition, InternalSyntaxToken secondSemicolonToken,
        SeparatedList<ExpressionTree> update, InternalSyntaxToken closeParenthesisToken
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

    @Override
    public boolean is(Kind... kind) {
      return false;
    }

    @Override
    public void accept(TreeVisitor visitor) {
      throw new IllegalStateException("class ForStatementHeader is used only internally for building the tree and should not be used to tree visiting.");
    }

    public InternalSyntaxToken forToken() {
      return forToken;
    }

    public InternalSyntaxToken openParenthesisToken() {
      return openParenthesisToken;
    }

    public SeparatedList<ExpressionTree> init() {
      return init;
    }

    public InternalSyntaxToken firstSemicolonToken() {
      return firstSemicolonToken;
    }

    public SeparatedList<ExpressionTree> condition() {
      return condition;
    }

    public InternalSyntaxToken secondSemicolonToken() {
      return secondSemicolonToken;
    }

    public SeparatedList<ExpressionTree> update() {
      return update;
    }

    public InternalSyntaxToken closeParenthesisToken() {
      return closeParenthesisToken;
    }
  }
}
