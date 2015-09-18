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
package org.sonar.php.parser;

import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.typed.Optional;
import org.sonar.php.tree.impl.SeparatedList;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.tree.impl.declaration.NamespaceNameTreeImpl;
import org.sonar.php.tree.impl.expression.IdentifierTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.impl.statement.BlockTreeImpl;
import org.sonar.php.tree.impl.statement.BreakStatementTreeImpl;
import org.sonar.php.tree.impl.statement.CatchBlockTreeImpl;
import org.sonar.php.tree.impl.statement.ContinueStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ElseClauseTreeImpl;
import org.sonar.php.tree.impl.statement.ElseifClauseTreeImpl;
import org.sonar.php.tree.impl.statement.EmptyStatementImpl;
import org.sonar.php.tree.impl.statement.ExpressionStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ForEachStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ForEachStatementTreeImpl.ForEachStatementHeader;
import org.sonar.php.tree.impl.statement.ForStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ForStatementTreeImpl.ForStatementHeader;
import org.sonar.php.tree.impl.statement.GotoStatementTreeImpl;
import org.sonar.php.tree.impl.statement.IfStatementTreeImpl;
import org.sonar.php.tree.impl.statement.LabelTreeImpl;
import org.sonar.php.tree.impl.statement.ReturnStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ThrowStatementTreeImpl;
import org.sonar.php.tree.impl.statement.TryStatementImpl;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.EmptyStatementTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.LabelTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TreeFactory {

  private <T extends Tree> List<T> optionalList(Optional<List<T>> list) {
    if (list.isPresent()) {
      return list.get();
    } else {
      return Collections.emptyList();
    }
  }

  private <T extends Tree> SeparatedList<T> optionalSeparatedList(Optional<SeparatedList<T>> list) {
    if (list.isPresent()) {
      return list.get();
    } else {
      return new SeparatedList<>(new LinkedList<T>(), new LinkedList<InternalSyntaxToken>());
    }
  }

  /**
   * [ START ] Statement
   */
  public ReturnStatementTree returnStatement(InternalSyntaxToken returnToken, Optional<ExpressionTree> expression, InternalSyntaxToken eos) {
    return new ReturnStatementTreeImpl(returnToken, expression.orNull(), eos);
  }

  public ContinueStatementTree continueStatement(InternalSyntaxToken continueToken, Optional<ExpressionTree> expression, InternalSyntaxToken eos) {
    return new ContinueStatementTreeImpl(continueToken, expression.orNull(), eos);
  }

  public BreakStatementTree breakStatement(InternalSyntaxToken breakToken, Optional<ExpressionTree> expression, InternalSyntaxToken eos) {
    return new BreakStatementTreeImpl(breakToken, expression.orNull(), eos);
  }

  public BlockTree block(InternalSyntaxToken lbrace, Optional<List<StatementTree>> statements, InternalSyntaxToken rbrace) {
    return new BlockTreeImpl(lbrace, optionalList(statements), rbrace);
  }

  public GotoStatementTree gotoStatement(InternalSyntaxToken gotoToken, InternalSyntaxToken identifier, InternalSyntaxToken eos) {
    return new GotoStatementTreeImpl(gotoToken, new IdentifierTreeImpl(identifier), eos);
  }

  public ExpressionStatementTree expressionStatement(ExpressionTree expression, InternalSyntaxToken eos) {
    return new ExpressionStatementTreeImpl(expression, eos);
  }

  public LabelTree label(InternalSyntaxToken identifier, InternalSyntaxToken colon) {
    return new LabelTreeImpl(new IdentifierTreeImpl(identifier), colon);
  }


  public TryStatementTree tryStatement(InternalSyntaxToken tryToken, BlockTree blockTree, Optional<List<CatchBlockTree>> catchBlocks, Optional<Tuple<InternalSyntaxToken, BlockTree>> finallyBlock) {
    if (finallyBlock.isPresent()) {
      return new TryStatementImpl(
          tryToken,
          blockTree,
          optionalList(catchBlocks),
          finallyBlock.get().first(),
          finallyBlock.get().second()
      );
    } else {
      return new TryStatementImpl(
          tryToken,
          blockTree,
          optionalList(catchBlocks)
      );
    }
  }

  public NamespaceNameTree namespaceName(
      Optional<InternalSyntaxToken> separator,
      Optional<List<Tuple<InternalSyntaxToken, InternalSyntaxToken>>> listOptional,
      InternalSyntaxToken name
  ) {
    return namespaceName(separator.orNull(), null, null, listOptional, name);
  }


  public NamespaceNameTree namespaceName(
      InternalSyntaxToken namespaceToken,
      InternalSyntaxToken separator,
      Optional<List<Tuple<InternalSyntaxToken, InternalSyntaxToken>>> listOptional,
      InternalSyntaxToken name
  ) {
    return namespaceName(null, namespaceToken, separator, listOptional, name);
  }

  private NamespaceNameTree namespaceName(
      @Nullable InternalSyntaxToken absoluteSeparator,
      @Nullable InternalSyntaxToken namespaceToken,
      @Nullable InternalSyntaxToken separator,
      Optional<List<Tuple<InternalSyntaxToken, InternalSyntaxToken>>> listOptional,
      InternalSyntaxToken name
  ) {

    ImmutableList.Builder<IdentifierTree> elements = ImmutableList.builder();
    ImmutableList.Builder<InternalSyntaxToken> separators = ImmutableList.builder();

    if (namespaceToken != null && separator != null) {
      elements.add(new IdentifierTreeImpl(namespaceToken));
      separators.add(separator);
    }

    if (listOptional.isPresent()) {
      for (Tuple<InternalSyntaxToken, InternalSyntaxToken> tuple : listOptional.get()) {
        elements.add(new IdentifierTreeImpl(tuple.first()));
        separators.add(tuple.second());
      }
    }

    return new NamespaceNameTreeImpl(absoluteSeparator, new SeparatedList(elements.build(), separators.build()), new IdentifierTreeImpl(name));

  }

  public CatchBlockTree catchBlock(InternalSyntaxToken catchToken, InternalSyntaxToken lParenthesis, NamespaceNameTree exceptionType, InternalSyntaxToken variable, InternalSyntaxToken rParenthsis, BlockTree block) {
    return new CatchBlockTreeImpl(
        catchToken,
        lParenthesis,
        exceptionType,
        new VariableIdentifierTreeImpl(new IdentifierTreeImpl(variable)),
        rParenthsis,
        block
    );
  }

  public EmptyStatementTree emptyStatement(InternalSyntaxToken semicolonToken) {
    return new EmptyStatementImpl(semicolonToken);
  }

  public ThrowStatementTree throwStatement(InternalSyntaxToken throwToken, ExpressionTree expression, InternalSyntaxToken eosToken) {
    return new ThrowStatementTreeImpl(throwToken, expression, eosToken);
  }

  public ForEachStatementTree forEachStatement(ForEachStatementHeader header, StatementTree statement) {
    return new ForEachStatementTreeImpl(header, statement);
  }

  public ForEachStatementTree forEachStatementAlternative(
      ForEachStatementHeader header,
      InternalSyntaxToken colonToken, Optional<List<StatementTree>> statements, InternalSyntaxToken endForEachToken, InternalSyntaxToken eosToken
  ) {
    return new ForEachStatementTreeImpl(header, colonToken, optionalList(statements), endForEachToken, eosToken);
  }

  public ForEachStatementHeader forEachStatementHeader(
      InternalSyntaxToken forEachToken, InternalSyntaxToken openParenthesisToken,
      ExpressionTree expression, InternalSyntaxToken asToken, Optional<Tuple<ExpressionTree, InternalSyntaxToken>> optionalKey, ExpressionTree value,
      InternalSyntaxToken closeParenthesisToken
  ) {
    return new ForEachStatementHeader(
        forEachToken, openParenthesisToken,
        expression, asToken, getForEachKey(optionalKey), getForEachArrow(optionalKey), value,
        closeParenthesisToken
    );
  }

  @Nullable
  private ExpressionTree getForEachKey(Optional<Tuple<ExpressionTree, InternalSyntaxToken>> optionalKey) {
    if (optionalKey.isPresent()) {
      return optionalKey.get().first();
    } else {
      return null;
    }
  }

  @Nullable
  private InternalSyntaxToken getForEachArrow(Optional<Tuple<ExpressionTree, InternalSyntaxToken>> optionalKey) {
    if (optionalKey.isPresent()) {
      return optionalKey.get().second();
    } else {
      return null;
    }
  }

  public ForStatementHeader forStatementHeader(
      InternalSyntaxToken forToken, InternalSyntaxToken lParenthesis,
      Optional<SeparatedList<ExpressionTree>> init, InternalSyntaxToken semicolon1,
      Optional<SeparatedList<ExpressionTree>> condition, InternalSyntaxToken semicolon2,
      Optional<SeparatedList<ExpressionTree>> update, InternalSyntaxToken rParenthesis
  ) {
    return new ForStatementHeader(
        forToken, lParenthesis,
        optionalSeparatedList(init),
        semicolon1,
        optionalSeparatedList(condition),
        semicolon2,
        optionalSeparatedList(update),
        rParenthesis
    );
  }

  public ForStatementTree forStatement(ForStatementHeader forStatementHeader, StatementTree statement) {
    return new ForStatementTreeImpl(forStatementHeader, statement);
  }

  public ForStatementTree forStatementAlternative(
      ForStatementHeader forStatementHeader, InternalSyntaxToken colonToken,
      Optional<List<StatementTree>> statements, InternalSyntaxToken endForToken, InternalSyntaxToken eos
  ) {
    return new ForStatementTreeImpl(forStatementHeader, colonToken, optionalList(statements), endForToken, eos);
  }

  public SeparatedList<ExpressionTree> forExpr(ExpressionTree expression, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> listOptional) {
    ImmutableList.Builder<ExpressionTree> elements = ImmutableList.builder();
    ImmutableList.Builder<InternalSyntaxToken> separators = ImmutableList.builder();

    elements.add(expression);

    if (listOptional.isPresent()) {
      for (Tuple<InternalSyntaxToken, ExpressionTree> tuple : listOptional.get()) {
        separators.add(tuple.first());
        elements.add(tuple.second());
      }
    }

    return new SeparatedList(elements.build(), separators.build());
  }

  public ElseClauseTree elseClause(InternalSyntaxToken elseToken, StatementTree statement) {
    return new ElseClauseTreeImpl(elseToken, statement);
  }

  public IfStatementTree ifStatement(
      InternalSyntaxToken ifToken, ExpressionTree expression, StatementTree statement,
      Optional<List<ElseifClauseTree>> elseIfClauses, Optional<ElseClauseTree> elseClause
  ) {
    return new IfStatementTreeImpl(ifToken, expression, statement, optionalList(elseIfClauses), elseClause.orNull());
  }

  public ElseifClauseTree elseifClause(InternalSyntaxToken elseifToken, ExpressionTree condition, StatementTree statement) {
    return new ElseifClauseTreeImpl(elseifToken, condition, statement);
  }

  public IfStatementTree alternativeIfStatement(
      InternalSyntaxToken ifToken, ExpressionTree condition, InternalSyntaxToken colonToken,
      Optional<List<StatementTree>> statements, Optional<List<ElseifClauseTree>> elseifClauses, Optional<ElseClauseTree> elseClause,
      InternalSyntaxToken endIfToken, InternalSyntaxToken eosToken
  ) {
    return new IfStatementTreeImpl(
        ifToken,
        condition,
        colonToken,
        optionalList(statements),
        optionalList(elseifClauses),
        elseClause.orNull(),
        endIfToken,
        eosToken
    );
  }

  public ElseClauseTree alternativeElseClause(InternalSyntaxToken elseToken, InternalSyntaxToken colonToken, Optional<List<StatementTree>> statements) {
    return new ElseClauseTreeImpl(
        elseToken,
        colonToken,
        optionalList(statements)
    );
  }

  public ElseifClauseTree alternativeElseifClause(
      InternalSyntaxToken elseifToken, ExpressionTree condition, InternalSyntaxToken colonToken,
      Optional<List<StatementTree>> statements
  ) {
    return new ElseifClauseTreeImpl(
        elseifToken,
        condition,
        colonToken,
        optionalList(statements)
    );
  }

  /**
   * [ END ] Statement
   */

  public ExpressionTree expression(InternalSyntaxToken token) {
    return new VariableIdentifierTreeImpl(new IdentifierTreeImpl(token));
  }

  /**
   * [ START ] Expression
   */

  /**
   * [ END ] Expression
   */

  public static class Tuple<T, U> {

    private final T first;
    private final U second;

    public Tuple(T first, U second) {
      super();

      this.first = first;
      this.second = second;
    }

    public T first() {
      return first;
    }

    public U second() {
      return second;
    }
  }

  private <T, U> Tuple<T, U> newTuple(T first, U second) {
    return new Tuple<T, U>(first, second);
  }

  public <T, U> Tuple<T, U> newTuple1(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple2(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple3(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple4(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple5(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple6(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple7(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple8(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple9(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple10(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple11(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple12(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple13(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple14(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple15(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple16(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple17(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple18(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple19(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple20(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple21(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple22(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple23(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple24(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple25(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple26(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple27(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple28(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple29(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple30(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple50(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple51(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple52(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple53(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple54(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple55(T first, U second) {
    return newTuple(first, second);
  }

}
