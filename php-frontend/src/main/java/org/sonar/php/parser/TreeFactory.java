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
import com.google.common.collect.Lists;
import com.sonar.sslr.api.typed.Optional;
import org.sonar.php.tree.impl.SeparatedList;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.tree.impl.declaration.FunctionDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.MethodDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.NamespaceNameTreeImpl;
import org.sonar.php.tree.impl.declaration.ParameterListTreeImpl;
import org.sonar.php.tree.impl.declaration.ParameterTreeImpl;
import org.sonar.php.tree.impl.declaration.UseDeclarationTreeImpl;
import org.sonar.php.tree.impl.expression.ArrayAccessTreeImpl;
import org.sonar.php.tree.impl.expression.AssignmentExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.CompoundVariableTreeImpl;
import org.sonar.php.tree.impl.expression.ComputedVariableTreeImpl;
import org.sonar.php.tree.impl.expression.ExpandableStringCharactersTreeImpl;
import org.sonar.php.tree.impl.expression.ExpandableStringLiteralTreeImpl;
import org.sonar.php.tree.impl.expression.FunctionCallTreeImpl;
import org.sonar.php.tree.impl.expression.IdentifierTreeImpl;
import org.sonar.php.tree.impl.expression.LexicalVariablesTreeImpl;
import org.sonar.php.tree.impl.expression.ListExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.LiteralTreeImpl;
import org.sonar.php.tree.impl.expression.MemberAccessTreeImpl;
import org.sonar.php.tree.impl.expression.ParenthesizedExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.ReferenceVariableTreeImpl;
import org.sonar.php.tree.impl.expression.SpreadArgumentTreeImpl;
import org.sonar.php.tree.impl.expression.VariableVariableTreeImpl;
import org.sonar.php.tree.impl.expression.YieldExpressionTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.impl.statement.BlockTreeImpl;
import org.sonar.php.tree.impl.statement.BreakStatementTreeImpl;
import org.sonar.php.tree.impl.statement.CaseClauseTreeImpl;
import org.sonar.php.tree.impl.statement.CatchBlockTreeImpl;
import org.sonar.php.tree.impl.statement.ContinueStatementTreeImpl;
import org.sonar.php.tree.impl.statement.DeclareStatementTreeImpl;
import org.sonar.php.tree.impl.statement.DeclareStatementTreeImpl.DeclareStatementHead;
import org.sonar.php.tree.impl.statement.DefaultClauseTreeImpl;
import org.sonar.php.tree.impl.statement.DoWhileStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ElseClauseTreeImpl;
import org.sonar.php.tree.impl.statement.ElseifClauseTreeImpl;
import org.sonar.php.tree.impl.statement.EmptyStatementImpl;
import org.sonar.php.tree.impl.statement.ExpressionStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ForEachStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ForEachStatementTreeImpl.ForEachStatementHeader;
import org.sonar.php.tree.impl.statement.ForStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ForStatementTreeImpl.ForStatementHeader;
import org.sonar.php.tree.impl.statement.GlobalStatementTreeImpl;
import org.sonar.php.tree.impl.statement.GotoStatementTreeImpl;
import org.sonar.php.tree.impl.statement.IfStatementTreeImpl;
import org.sonar.php.tree.impl.statement.InlineHTMLTreeImpl;
import org.sonar.php.tree.impl.statement.LabelTreeImpl;
import org.sonar.php.tree.impl.statement.NamespaceStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ReturnStatementTreeImpl;
import org.sonar.php.tree.impl.statement.SwitchStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ThrowStatementTreeImpl;
import org.sonar.php.tree.impl.statement.TryStatementImpl;
import org.sonar.php.tree.impl.statement.UnsetVariableStatementTreeImpl;
import org.sonar.php.tree.impl.statement.UseStatementTreeImpl;
import org.sonar.php.tree.impl.statement.VariableDeclarationTreeImpl;
import org.sonar.php.tree.impl.statement.WhileStatementTreeImpl;
import org.sonar.php.tree.impl.statement.YieldStatementTreeImpl;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.UseDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.CompoundVariableTree;
import org.sonar.plugins.php.api.tree.expression.ComputedVariableTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.expression.ListExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ReferenceVariableTree;
import org.sonar.plugins.php.api.tree.expression.SpreadArgumentTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.expression.YieldExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.CaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;
import org.sonar.plugins.php.api.tree.statement.DeclareStatementTree;
import org.sonar.plugins.php.api.tree.statement.DefaultClauseTree;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.EmptyStatementTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.GlobalStatementTree;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.InlineHTMLTree;
import org.sonar.plugins.php.api.tree.statement.LabelTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.tree.statement.UnsetVariableStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.YieldStatementTree;

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
  
  private <T extends Tree> SeparatedList<T> separatedList(T firstElement, Optional<List<Tuple<InternalSyntaxToken, T>>> tuples) {
    ImmutableList.Builder<T> elements = ImmutableList.builder();
    ImmutableList.Builder<InternalSyntaxToken> separators = ImmutableList.builder();
    elements.add(firstElement);
    if (tuples.isPresent()) {
      for (Tuple<InternalSyntaxToken, T> tuple : tuples.get()) {
        separators.add(tuple.first());
        elements.add(tuple.second());
      }
    }
    return new SeparatedList<>(elements.build(), separators.build());
  }

  /**
   * [ START ] Declarations
   */

  public VariableDeclarationTree memberConstDeclaration(InternalSyntaxToken identifierToken, Optional<Tuple<InternalSyntaxToken, ExpressionTree>> optionalEqual) {
    if (optionalEqual.isPresent()) {
      return new VariableDeclarationTreeImpl(new IdentifierTreeImpl(identifierToken), optionalEqual.get().first(), optionalEqual.get().second());
    } else {
      return new VariableDeclarationTreeImpl(new IdentifierTreeImpl(identifierToken), null, null);
    }
  }

  public UseDeclarationTree useDeclaration(NamespaceNameTree namespaceName, Optional<Tuple<InternalSyntaxToken, InternalSyntaxToken>> alias) {
    if (alias.isPresent()) {
      IdentifierTreeImpl aliasName = new IdentifierTreeImpl(alias.get().second());
      return new UseDeclarationTreeImpl(namespaceName, alias.get().first(), aliasName);
    }
    return new UseDeclarationTreeImpl(namespaceName);
  }

  public MethodDeclarationTree methodDeclaration(
    Optional<List<SyntaxToken>> modifiers, 
    InternalSyntaxToken functionToken, 
    Optional<InternalSyntaxToken> referenceToken,
    IdentifierTree name, 
    ParameterListTree parameters, 
    Tree body
    ) {
    return new MethodDeclarationTreeImpl(optionalList(modifiers), functionToken, referenceToken.orNull(), name, parameters, body);
  }
  
  public FunctionDeclarationTree functionDeclaration(
    InternalSyntaxToken functionToken, 
    Optional<InternalSyntaxToken> referenceToken, 
    IdentifierTree name, 
    ParameterListTree parameters,
    BlockTree body
    ) {
    return new FunctionDeclarationTreeImpl(functionToken, referenceToken.orNull(), name, parameters, body);
  }
  
  public ParameterListTree parameterList(
    InternalSyntaxToken leftParenthesis, 
    Optional<Tuple<ParameterTree, Optional<List<Tuple<InternalSyntaxToken, ParameterTree>>>>> parameters,
    InternalSyntaxToken rightParenthesis
    ) {
    SeparatedList<ParameterTree> separatedList = SeparatedList.empty();
    if (parameters.isPresent()) {
      separatedList = separatedList(parameters.get().first(), parameters.get().second());
    }
    return new ParameterListTreeImpl(leftParenthesis, separatedList, rightParenthesis);
  }
  
  public ParameterTree parameter(
    Optional<Tree> classType, 
    Optional<InternalSyntaxToken> ampersand, 
    Optional<InternalSyntaxToken> ellipsis, 
    InternalSyntaxToken identifier,
    Optional<Tuple<InternalSyntaxToken, ExpressionTree>> eqAndInitValue
    ){
    InternalSyntaxToken eqToken = null;
    ExpressionTree initValue = null;
    if (eqAndInitValue.isPresent()) {
      eqToken = eqAndInitValue.get().first();
      initValue = eqAndInitValue.get().second();
    }
    VariableIdentifierTree varIdentifier = new VariableIdentifierTreeImpl(new IdentifierTreeImpl(identifier));
    return new ParameterTreeImpl(classType.orNull(), ampersand.orNull(), ellipsis.orNull(), varIdentifier, eqToken, initValue);
  }

  /**
   * [ END ] Declarations
   */

  
  /**
   * [ START ] Statement
   */

  public GlobalStatementTree globalStatement(InternalSyntaxToken globalToken, VariableTree variable, Optional<List<Tuple<InternalSyntaxToken, VariableTree>>> variableRest, InternalSyntaxToken eosToken) {
    List<VariableTree> variables = Lists.newArrayList();
    List<InternalSyntaxToken> commas = Lists.newArrayList();

    // First element
    variables.add(variable);

    // Rest of elements
    if (variableRest.isPresent()) {
      for (Tuple<InternalSyntaxToken, VariableTree> argumentRest : variableRest.get()) {
        commas.add(argumentRest.first());
        variables.add(argumentRest.second());
      }
    }

    return new GlobalStatementTreeImpl(
        globalToken,
        new SeparatedList<>(variables, commas),
        eosToken
    );
  }

  public UseStatementTree useStatement(
      InternalSyntaxToken useToken,
      Optional<InternalSyntaxToken> useTypeToken,
      UseDeclarationTree firstDeclaration,
      Optional<List<Tuple<InternalSyntaxToken, UseDeclarationTree>>> additionalDeclarations,
      InternalSyntaxToken eosToken
  ) {
    SeparatedList<UseDeclarationTree> declarations = separatedList(firstDeclaration, additionalDeclarations);
    return new UseStatementTreeImpl(useToken, useTypeToken.orNull(), declarations, eosToken);
  }

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
      InternalSyntaxToken ifToken, ParenthesisedExpressionTree expression, StatementTree statement,
      Optional<List<ElseifClauseTree>> elseIfClauses, Optional<ElseClauseTree> elseClause
  ) {
    return new IfStatementTreeImpl(ifToken, expression, statement, optionalList(elseIfClauses), elseClause.orNull());
  }

  public ElseifClauseTree elseifClause(InternalSyntaxToken elseifToken, ParenthesisedExpressionTree condition, StatementTree statement) {
    return new ElseifClauseTreeImpl(elseifToken, condition, statement);
  }

  public IfStatementTree alternativeIfStatement(
      InternalSyntaxToken ifToken, ParenthesisedExpressionTree condition, InternalSyntaxToken colonToken,
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
      InternalSyntaxToken elseifToken, ParenthesisedExpressionTree condition, InternalSyntaxToken colonToken,
      Optional<List<StatementTree>> statements
  ) {
    return new ElseifClauseTreeImpl(
        elseifToken,
        condition,
        colonToken,
        optionalList(statements)
    );
  }

  public DoWhileStatementTree doWhileStatement(InternalSyntaxToken doToken, StatementTree statement, InternalSyntaxToken whileToken, ParenthesisedExpressionTree condition, InternalSyntaxToken eosToken) {
    return new DoWhileStatementTreeImpl(
        doToken,
        statement,
        whileToken,
        condition,
        eosToken
    );
  }

  public WhileStatementTree whileStatement(InternalSyntaxToken whileToken, ParenthesisedExpressionTree condition, StatementTree statement) {
    return new WhileStatementTreeImpl(whileToken, condition, statement);
  }

  public WhileStatementTree alternativeWhileStatement(
      InternalSyntaxToken whileToken, ParenthesisedExpressionTree condition, InternalSyntaxToken colonToken,
      Optional<List<StatementTree>> statements, InternalSyntaxToken endwhileToken, InternalSyntaxToken eosToken) {
    return new WhileStatementTreeImpl(
        whileToken,
        condition,
        colonToken,
        optionalList(statements),
        endwhileToken,
        eosToken
    );
  }

  public SwitchStatementTree switchStatement(InternalSyntaxToken switchToken, ParenthesisedExpressionTree expression, InternalSyntaxToken openCurlyBraceToken, Optional<InternalSyntaxToken> semicolonToken, Optional<List<SwitchCaseClauseTree>> switchCaseClauses, InternalSyntaxToken closeCurlyBraceToken) {
    return new SwitchStatementTreeImpl(
        switchToken,
        expression,
        openCurlyBraceToken,
        semicolonToken.orNull(),
        optionalList(switchCaseClauses),
        closeCurlyBraceToken
    );
  }

  public SwitchStatementTree alternativeSwitchStatement(InternalSyntaxToken switchToken, ParenthesisedExpressionTree expression, InternalSyntaxToken colonToken, Optional<InternalSyntaxToken> semicolonToken, Optional<List<SwitchCaseClauseTree>> switchCaseClauses, InternalSyntaxToken endswitchToken, InternalSyntaxToken eosToken) {
    return new SwitchStatementTreeImpl(
        switchToken,
        expression,
        colonToken,
        semicolonToken.orNull(),
        optionalList(switchCaseClauses),
        endswitchToken,
        eosToken
    );
  }

  public CaseClauseTree caseClause(InternalSyntaxToken caseToken, ExpressionTree expression, InternalSyntaxToken caseSeparatorToken, Optional<List<StatementTree>> statements) {
    return new CaseClauseTreeImpl(
        caseToken,
        expression,
        caseSeparatorToken,
        optionalList(statements)
    );
  }

  public DefaultClauseTree defaultClause(InternalSyntaxToken defaultToken, InternalSyntaxToken caseSeparatorToken, Optional<List<StatementTree>> statements) {
    return new DefaultClauseTreeImpl(
        defaultToken,
        caseSeparatorToken,
        optionalList(statements)
    );
  }

  public YieldStatementTree yieldStatement(YieldExpressionTree yieldExpression, InternalSyntaxToken eosToken) {
    return new YieldStatementTreeImpl(yieldExpression, eosToken);
  }

  public UnsetVariableStatementTree unsetVariableStatement(
      InternalSyntaxToken unsetToken, InternalSyntaxToken openParenthesisToken,
      ExpressionTree expression, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> list,
      InternalSyntaxToken closeParenthesisToken, InternalSyntaxToken eosToken
  ) {

    ImmutableList.Builder<ExpressionTree> elements = ImmutableList.builder();
    ImmutableList.Builder<InternalSyntaxToken> separators = ImmutableList.builder();
    elements.add(expression);
    if (list.isPresent()) {
      for (Tuple<InternalSyntaxToken, ExpressionTree> tuple : list.get()) {
        separators.add(tuple.first());
        elements.add(tuple.second());
      }
    }

    return new UnsetVariableStatementTreeImpl(
        unsetToken,
        openParenthesisToken,
        new SeparatedList<>(elements.build(), separators.build()),
        closeParenthesisToken,
        eosToken
    );
  }

  public NamespaceStatementTree namespaceStatement(InternalSyntaxToken namespaceToken, NamespaceNameTree namespaceName, InternalSyntaxToken eosToken) {
    return new NamespaceStatementTreeImpl(
        namespaceToken,
        namespaceName,
        eosToken
    );
  }

  public NamespaceStatementTree blockNamespaceStatement(
      InternalSyntaxToken namespaceToken, Optional<NamespaceNameTree> namespaceName,
      InternalSyntaxToken openCurlyBrace, Optional<List<StatementTree>> statements, InternalSyntaxToken closeCurlyBrace
  ) {
    return new NamespaceStatementTreeImpl(
        namespaceToken,
        namespaceName.orNull(),
        openCurlyBrace,
        optionalList(statements),
        closeCurlyBrace
    );
  }

  public InlineHTMLTree inlineHTML(InternalSyntaxToken inlineHTMLToken) {
    return new InlineHTMLTreeImpl(inlineHTMLToken);
  }

  public DeclareStatementTree shortDeclareStatement(DeclareStatementHead declareStatementHead, InternalSyntaxToken eosToken) {
    return new DeclareStatementTreeImpl(declareStatementHead, eosToken);
  }

  public DeclareStatementHead declareStatementHead(
      InternalSyntaxToken declareToken, InternalSyntaxToken openParenthesisToken,
      VariableDeclarationTree firstDirective, Optional<List<Tuple<InternalSyntaxToken, VariableDeclarationTree>>> optionalDirectives,
      InternalSyntaxToken closeParenthesisToken
  ) {
    List<VariableDeclarationTree> directives = Lists.newArrayList();
    List<InternalSyntaxToken> commas = Lists.newArrayList();

    // First element
    directives.add(firstDirective);

    // Rest of elements
    if (optionalDirectives.isPresent()) {
      for (Tuple<InternalSyntaxToken, VariableDeclarationTree> directive : optionalDirectives.get()) {
        commas.add(directive.first());
        directives.add(directive.second());
      }
    }

    return new DeclareStatementHead(
        declareToken,
        openParenthesisToken,
        new SeparatedList<>(directives, commas),
        closeParenthesisToken
    );
  }

  public DeclareStatementTree declareStatementWithOneStatement(DeclareStatementHead declareStatementHead, StatementTree statement) {
    return new DeclareStatementTreeImpl(declareStatementHead, statement);
  }

  public DeclareStatementTree alternativeDeclareStatement(
      DeclareStatementHead declareStatementHead, InternalSyntaxToken colonToken,
      Optional<List<StatementTree>> statements,
      InternalSyntaxToken enddeclareToken, InternalSyntaxToken eosToken
  ) {
    return new DeclareStatementTreeImpl(declareStatementHead, colonToken, optionalList(statements), enddeclareToken, eosToken);
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

  public LiteralTree numericLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.NUMERIC_LITERAL, token);
  }

  public LiteralTree regularStringLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.REGULAR_STRING_LITERAL, token);
  }

  public LiteralTree booleanLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.BOOLEAN_LITERAL, token);
  }

  public LiteralTree nullLiteral(InternalSyntaxToken token) {
   return new LiteralTreeImpl(Tree.Kind.NULL_LITERAL, token);
  }

  public LiteralTree magicConstantLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.MAGIC_CONSTANT, token);
  }

  public LiteralTree heredocLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.HEREDOC_LITERAL, token);
  }

  public ExpandableStringCharactersTree expandableStringCharacters(InternalSyntaxToken token) {
    return new ExpandableStringCharactersTreeImpl(token);
  }

  public VariableIdentifierTree expandableStringVariableIdentifier(InternalSyntaxToken token) {
    return new VariableIdentifierTreeImpl(new IdentifierTreeImpl(token));
  }

  public IdentifierTree identifier(InternalSyntaxToken token) {
    return new IdentifierTreeImpl(token);
  }

  public ArrayAccessTree expandableArrayAccess(InternalSyntaxToken openBracket, ExpressionTree offset, InternalSyntaxToken closeBracket) {
    return new ArrayAccessTreeImpl(openBracket, offset, closeBracket);
  }

  public MemberAccessTree expandableObjectMemberAccess(InternalSyntaxToken arrow, IdentifierTree property) {
    return new MemberAccessTreeImpl(Kind.OBJECT_MEMBER_ACCESS, arrow, property);
  }

  public ExpressionTree encapsulatedSimpleVar(VariableIdentifierTree variableIdentifier, Optional<ExpressionTree> partial) {
    if (partial.isPresent()) {

      if (partial.get() instanceof ArrayAccessTree) {
        ((ArrayAccessTreeImpl) partial.get()).complete(variableIdentifier);
      } else {
        ((MemberAccessTreeImpl) partial.get()).complete(variableIdentifier);
      }
      return partial.get();
    }

    return variableIdentifier;
  }

  public ExpressionTree expressionRecovery(InternalSyntaxToken token) {
    return new IdentifierTreeImpl(token);
  }

  public ExpressionTree encapsulatedSemiComplexVariable(InternalSyntaxToken openDollarCurly, ExpressionTree expressionTree, InternalSyntaxToken closeCurly) {
    return new CompoundVariableTreeImpl(openDollarCurly, expressionTree, closeCurly);
  }

  public VariableIdentifierTree encapsulatedVariableIdentifier(InternalSyntaxToken spaces, InternalSyntaxToken variableIdentifier) {
    return new VariableIdentifierTreeImpl(new IdentifierTreeImpl(variableIdentifier));
  }

  public ExpressionTree encapsulatedComplexVariable(InternalSyntaxToken openCurly, Tree lookahead, ExpressionTree expression, InternalSyntaxToken closeCurly) {
    return new ComputedVariableTreeImpl(openCurly, expression, closeCurly);
  }

  public ExpandableStringLiteralTree expandableStringLiteral(Tree spacing, InternalSyntaxToken openDoubleQuote, List<ExpressionTree> expressions, InternalSyntaxToken closeDoubleQuote) {
    return new ExpandableStringLiteralTreeImpl(openDoubleQuote, expressions, closeDoubleQuote);
  }

  public YieldExpressionTree yieldExpression(InternalSyntaxToken yieldToken, ExpressionTree expr1, Optional<Tuple<InternalSyntaxToken, ExpressionTree>> expr2) {
    if (expr2.isPresent()) {
      return new YieldExpressionTreeImpl(yieldToken, expr1, expr2.get().first(), expr2.get().second());
    }
    return new YieldExpressionTreeImpl(yieldToken, expr1);
  }

  public ParenthesisedExpressionTree parenthesizedExpression(InternalSyntaxToken openParenthesis, ExpressionTree expression, InternalSyntaxToken closeParenthesis) {
   return new ParenthesizedExpressionTreeImpl(openParenthesis, expression, closeParenthesis);
  }

  public ListExpressionTree listExpression(InternalSyntaxToken listToken, InternalSyntaxToken openParenthesis, Optional<Tuple<ExpressionTree, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>>>> elements, InternalSyntaxToken closeParenthesis) {
    List<ExpressionTree> expressions = Lists.newArrayList();
    List<InternalSyntaxToken> commas = Lists.newArrayList();

    if (elements.isPresent()) {
      // First element
      expressions.add(elements.get().first());

      // Rest of elements
      if (elements.get().second().isPresent()) {
        for (Tuple<InternalSyntaxToken, ExpressionTree> commaElement : elements.get().second().get()) {
          commas.add(commaElement.first());
          expressions.add(commaElement.second());
        }
      }
    }
    return new ListExpressionTreeImpl(listToken, openParenthesis, new SeparatedList(expressions, commas), closeParenthesis);
  }

  public AssignmentExpressionTree listExpressionAssignment(ExpressionTree listExpression, InternalSyntaxToken equalToken, ExpressionTree expression) {
    return new AssignmentExpressionTreeImpl(Kind.ASSIGNMENT, listExpression, equalToken, expression);
  }

  public ComputedVariableTree computedVariableName(InternalSyntaxToken openCurly, ExpressionTree expression, InternalSyntaxToken closeCurly) {
    return new ComputedVariableTreeImpl(openCurly, expression, closeCurly);
  }

  public VariableIdentifierTree variableIdentifier(InternalSyntaxToken variableIdentifier) {
    return new VariableIdentifierTreeImpl(new IdentifierTreeImpl(variableIdentifier));
  }

  public CompoundVariableTree compoundVariable(InternalSyntaxToken openDollarCurly, ExpressionTree expression, InternalSyntaxToken closeDollarCurly) {
    return new CompoundVariableTreeImpl(openDollarCurly, expression, closeDollarCurly);
  }

  public ArrayAccessTree dimensionalOffset(InternalSyntaxToken openCurly, Optional<ExpressionTree> expression, InternalSyntaxToken closeCurly) {
    if (expression.isPresent()) {
      return new ArrayAccessTreeImpl(openCurly, expression.get(), closeCurly);
    }
    return new ArrayAccessTreeImpl(openCurly, closeCurly);
  }

  public ExpressionTree variableWithoutObjects(Optional<List<InternalSyntaxToken>> dollars, VariableTree compoundVariable, Optional<List<ArrayAccessTree>> offsets) {
    ExpressionTree result = compoundVariable;
    for (ExpressionTree partialArrayAccess : optionalList(offsets)) {
      result = ((ArrayAccessTreeImpl) partialArrayAccess).complete(result);
    }

    if (dollars.isPresent()) {
      result = new VariableVariableTreeImpl(dollars.get(), result);
    }

    return result;
  }

  public ArrayAccessTree alternativeDimensionalOffset(InternalSyntaxToken openBrace, Optional<ExpressionTree> offset, InternalSyntaxToken closeBrace) {
    if (offset.isPresent()) {
      return new ArrayAccessTreeImpl(openBrace, offset.get(), closeBrace);
    }
    return new ArrayAccessTreeImpl(openBrace, closeBrace);
  }

  public IdentifierTree newStaticIdentifier(InternalSyntaxToken staticToken) {
    return new IdentifierTreeImpl(staticToken);
  }

  public ReferenceVariableTree referenceVariable(InternalSyntaxToken ampersand, ExpressionTree variable) {
    return new ReferenceVariableTreeImpl(ampersand, variable);
  }

  public SpreadArgumentTree spreadArgument(InternalSyntaxToken ellipsis, ExpressionTree expression) {
    return new SpreadArgumentTreeImpl(ellipsis, expression);
  }

  public FunctionCallTree functionCallParameterList(InternalSyntaxToken openParenthesis, Optional<Tuple<ExpressionTree, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>>>> arguments, InternalSyntaxToken closeParenthesis) {
    List<ExpressionTree> expressions = Lists.newArrayList();
    List<InternalSyntaxToken> commas = Lists.newArrayList();

    if (arguments.isPresent()) {
      // First element
      expressions.add(arguments.get().first());

      // Rest of elements
      if (arguments.get().second().isPresent()) {
        for (Tuple<InternalSyntaxToken, ExpressionTree> argumentRest : arguments.get().second().get()) {
          commas.add(argumentRest.first());
          expressions.add(argumentRest.second());
        }
      }
    }
    return new FunctionCallTreeImpl(openParenthesis, new SeparatedList(expressions, commas), closeParenthesis);
  }

  public MemberAccessTree classMemberAccess(InternalSyntaxToken token, Tree member) {
    return new MemberAccessTreeImpl(Kind.CLASS_MEMBER_ACCESS, token, member);
  }

  public ExpressionTree objectDimensionalList(ExpressionTree variableName, Optional<List<ArrayAccessTree>> dimensionalOffsets) {
    ExpressionTree result = variableName;

    for(ArrayAccessTree arrayAccess : optionalList(dimensionalOffsets)) {
       result = ((ArrayAccessTreeImpl) arrayAccess).complete(result);
    }

    return result;
  }

  public IdentifierTree variableName(InternalSyntaxToken token) {
    return new IdentifierTreeImpl(token);
  }

  public MemberAccessTree objectMemberAccess(InternalSyntaxToken accessToken, ExpressionTree member) {
    return new MemberAccessTreeImpl(Kind.OBJECT_MEMBER_ACCESS, accessToken, member);
  }

  public ExpressionTree memberExpression(ExpressionTree object, Optional<List<ExpressionTree>> memberAccesses) {
    ExpressionTree result = object;

    for (ExpressionTree memberAccess : optionalList(memberAccesses)) {
      if (memberAccess.is(Kind.OBJECT_MEMBER_ACCESS, Kind.CLASS_MEMBER_ACCESS)) {
        result = ((MemberAccessTreeImpl) memberAccess).complete(result);

      } else if (memberAccess.is(Kind.ARRAY_ACCESS)) {
        result = ((ArrayAccessTreeImpl) memberAccess).complete(result);

      } else if (memberAccess.is(Kind.FUNCTION_CALL)) {
        result = ((FunctionCallTreeImpl) memberAccess).complete(result);
      }
    }

    return result;
  }

  public VariableTree lexicalVariable(Optional<InternalSyntaxToken> ampersandToken, VariableIdentifierTree variableIdentifier) {
    return ampersandToken.isPresent()
      ? new ReferenceVariableTreeImpl(ampersandToken.get(), variableIdentifier)
      : variableIdentifier;
  }

  public LexicalVariablesTree lexicalVariables(InternalSyntaxToken useToken, InternalSyntaxToken openParenthesis, VariableTree variable, Optional<List<Tuple<InternalSyntaxToken, VariableTree>>> variableRest, InternalSyntaxToken closeParenthesis) {
    List<VariableTree> variables = Lists.newArrayList();
    List<InternalSyntaxToken> commas = Lists.newArrayList();

      // First element
      variables.add(variable);

      // Rest of elements
      if (variableRest.isPresent()) {
        for (Tuple<InternalSyntaxToken, VariableTree> argumentRest : variableRest.get()) {
          commas.add(argumentRest.first());
          variables.add(argumentRest.second());
        }
      }

    return new LexicalVariablesTreeImpl(useToken, openParenthesis, new SeparatedList(variables, commas), closeParenthesis);
  }

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
  
  public <T, U> Tuple<T, U> newTuple90(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple91(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple92(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple93(T first, U second) {
    return newTuple(first, second);
  }

  public <T, U> Tuple<T, U> newTuple94(T first, U second) {
    return newTuple(first, second);
  }

}
