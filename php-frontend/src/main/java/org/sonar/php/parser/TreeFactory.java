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
package org.sonar.php.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.typed.Optional;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.tree.impl.CompilationUnitTreeImpl;
import org.sonar.php.tree.impl.ScriptTreeImpl;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.tree.impl.declaration.BuiltInTypeTreeImpl;
import org.sonar.php.tree.impl.declaration.ClassDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.ClassPropertyDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.ConstantDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.FunctionDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.MethodDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.NamespaceNameTreeImpl;
import org.sonar.php.tree.impl.declaration.ParameterListTreeImpl;
import org.sonar.php.tree.impl.declaration.ParameterTreeImpl;
import org.sonar.php.tree.impl.declaration.ReturnTypeClauseTreeImpl;
import org.sonar.php.tree.impl.declaration.TraitAliasTreeImpl;
import org.sonar.php.tree.impl.declaration.TraitMethodReferenceTreeImpl;
import org.sonar.php.tree.impl.declaration.TraitPrecedenceTreeImpl;
import org.sonar.php.tree.impl.declaration.TypeTreeImpl;
import org.sonar.php.tree.impl.declaration.UseClauseTreeImpl;
import org.sonar.php.tree.impl.declaration.UseTraitDeclarationTreeImpl;
import org.sonar.php.tree.impl.expression.AnonymousClassTreeImpl;
import org.sonar.php.tree.impl.expression.ArrayAccessTreeImpl;
import org.sonar.php.tree.impl.expression.ArrayAssignmentPatternElementTreeImpl;
import org.sonar.php.tree.impl.expression.ArrayAssignmentPatternElements;
import org.sonar.php.tree.impl.expression.ArrayAssignmentPatternTreeImpl;
import org.sonar.php.tree.impl.expression.ArrayInitializerBracketTreeImpl;
import org.sonar.php.tree.impl.expression.ArrayInitializerFunctionTreeImpl;
import org.sonar.php.tree.impl.expression.ArrayPairTreeImpl;
import org.sonar.php.tree.impl.expression.AssignmentByReferenceTreeImpl;
import org.sonar.php.tree.impl.expression.AssignmentExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.BinaryExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.CastExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.CompoundVariableTreeImpl;
import org.sonar.php.tree.impl.expression.ComputedVariableTreeImpl;
import org.sonar.php.tree.impl.expression.ConditionalExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.ExpandableStringCharactersTreeImpl;
import org.sonar.php.tree.impl.expression.ExpandableStringLiteralTreeImpl;
import org.sonar.php.tree.impl.expression.FunctionCallTreeImpl;
import org.sonar.php.tree.impl.expression.FunctionExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.HeredocStringLiteralTreeImpl;
import org.sonar.php.tree.impl.expression.LexicalVariablesTreeImpl;
import org.sonar.php.tree.impl.expression.ListExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.LiteralTreeImpl;
import org.sonar.php.tree.impl.expression.MemberAccessTreeImpl;
import org.sonar.php.tree.impl.expression.NameIdentifierTreeImpl;
import org.sonar.php.tree.impl.expression.NewExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.ParenthesizedExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.PostfixExpressionTreeImpl;
import org.sonar.php.tree.impl.expression.PrefixExpressionTreeImpl;
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
import org.sonar.php.tree.impl.statement.ExpressionListStatementTreeImpl;
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
import org.sonar.php.tree.impl.statement.StaticStatementTreeImpl;
import org.sonar.php.tree.impl.statement.SwitchStatementTreeImpl;
import org.sonar.php.tree.impl.statement.ThrowStatementTreeImpl;
import org.sonar.php.tree.impl.statement.TryStatementTreeImpl;
import org.sonar.php.tree.impl.statement.UnsetVariableStatementTreeImpl;
import org.sonar.php.tree.impl.statement.UseStatementTreeImpl;
import org.sonar.php.tree.impl.statement.VariableDeclarationTreeImpl;
import org.sonar.php.tree.impl.statement.WhileStatementTreeImpl;
import org.sonar.php.tree.impl.statement.YieldStatementTreeImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.BuiltInTypeTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ConstantDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.declaration.TypeNameTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.CompoundVariableTree;
import org.sonar.plugins.php.api.tree.expression.ComputedVariableTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.HeredocStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.expression.ListExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
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
import org.sonar.plugins.php.api.tree.statement.ExpressionListStatementTree;
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
import org.sonar.plugins.php.api.tree.statement.StaticStatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.TraitAdaptationStatementTree;
import org.sonar.plugins.php.api.tree.statement.TraitAliasTree;
import org.sonar.plugins.php.api.tree.statement.TraitMethodReferenceTree;
import org.sonar.plugins.php.api.tree.statement.TraitPrecedenceTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.tree.statement.UnsetVariableStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.YieldStatementTree;


public class TreeFactory {

  private static final Map<String, Kind> BINARY_EXPRESSION_KINDS_BY_OPERATOR = ImmutableMap.<String, Kind>builder()
    .put(PHPPunctuator.DOT.getValue(), Kind.CONCATENATION)
    .put(PHPPunctuator.STAR_STAR.getValue(), Kind.POWER)
    .put(PHPPunctuator.STAR.getValue(), Kind.MULTIPLY)
    .put(PHPPunctuator.DIV.getValue(), Kind.DIVIDE)
    .put(PHPPunctuator.MOD.getValue(), Kind.REMAINDER)
    .put(PHPPunctuator.PLUS.getValue(), Kind.PLUS)
    .put(PHPPunctuator.MINUS.getValue(), Kind.MINUS)
    .put(PHPPunctuator.SL.getValue(), Kind.LEFT_SHIFT)
    .put(PHPPunctuator.SR.getValue(), Kind.RIGHT_SHIFT)
    .put(PHPPunctuator.LT.getValue(), Kind.LESS_THAN)
    .put(PHPPunctuator.GT.getValue(), Kind.GREATER_THAN)
    .put(PHPPunctuator.LE.getValue(), Kind.LESS_THAN_OR_EQUAL_TO)
    .put(PHPPunctuator.GE.getValue(), Kind.GREATER_THAN_OR_EQUAL_TO)
    .put(PHPPunctuator.EQUAL.getValue(), Kind.EQUAL_TO)
    .put(PHPPunctuator.EQUAL2.getValue(), Kind.STRICT_EQUAL_TO)
    .put(PHPPunctuator.NOTEQUAL.getValue(), Kind.NOT_EQUAL_TO)
    .put(PHPPunctuator.NOTEQUAL2.getValue(), Kind.STRICT_NOT_EQUAL_TO)
    .put(PHPPunctuator.NOTEQUALBIS.getValue(), Kind.ALTERNATIVE_NOT_EQUAL_TO)
    .put(PHPPunctuator.SPACESHIP.getValue(), Kind.COMPARISON)
    .put(PHPPunctuator.AMPERSAND.getValue(), Kind.BITWISE_AND)
    .put(PHPPunctuator.XOR.getValue(), Kind.BITWISE_XOR)
    .put(PHPPunctuator.OR.getValue(), Kind.BITWISE_OR)
    .put(PHPPunctuator.ANDAND.getValue(), Kind.CONDITIONAL_AND)
    .put(PHPPunctuator.OROR.getValue(), Kind.CONDITIONAL_OR)
    .put(PHPKeyword.AND.getValue(), Kind.ALTERNATIVE_CONDITIONAL_AND)
    .put(PHPKeyword.XOR.getValue(), Kind.ALTERNATIVE_CONDITIONAL_XOR)
    .put(PHPKeyword.OR.getValue(), Kind.ALTERNATIVE_CONDITIONAL_OR)
    .put(PHPPunctuator.NULL_COALESCE.getValue(), Kind.NULL_COALESCING_EXPRESSION)
    .build();

  private static final Map<String, Kind> UNARY_EXPRESSION_KINDS_BY_OPERATOR = ImmutableMap.<String, Kind>builder()
    .put(PHPPunctuator.INC.getValue(), Kind.PREFIX_INCREMENT)
    .put(PHPPunctuator.DEC.getValue(), Kind.PREFIX_DECREMENT)
    .put(PHPPunctuator.PLUS.getValue(), Kind.UNARY_PLUS)
    .put(PHPPunctuator.MINUS.getValue(), Kind.UNARY_MINUS)
    .put(PHPPunctuator.TILDA.getValue(), Kind.BITWISE_COMPLEMENT)
    .put(PHPPunctuator.BANG.getValue(), Kind.LOGICAL_COMPLEMENT)
    .put(PHPPunctuator.AT.getValue(), Kind.ERROR_CONTROL)
    .build();

  private static <T extends Tree> List<T> optionalList(Optional<List<T>> list) {
    if (list.isPresent()) {
      return list.get();
    } else {
      return Collections.emptyList();
    }
  }

  private static <T extends Tree> SeparatedListImpl<T> optionalSeparatedList(Optional<SeparatedListImpl<T>> list) {
    if (list.isPresent()) {
      return list.get();
    } else {
      return new SeparatedListImpl<>(new LinkedList<T>(), new LinkedList<SyntaxToken>());
    }
  }

  private static <T extends Tree> SeparatedListImpl<T> separatedList(T firstElement, Optional<List<Tuple<InternalSyntaxToken, T>>> tuples) {
    return separatedList(firstElement, tuples, null);
  }

  private static <T extends Tree> SeparatedListImpl<T> separatedList(
    T firstElement,
    Optional<List<Tuple<InternalSyntaxToken, T>>> tuples,
    @Nullable InternalSyntaxToken trailingSeparator
  ) {
    ImmutableList.Builder<T> elements = ImmutableList.builder();
    ImmutableList.Builder<SyntaxToken> separators = ImmutableList.builder();

    elements.add(firstElement);
    if (tuples.isPresent()) {
      for (Tuple<InternalSyntaxToken, T> tuple : tuples.get()) {
        separators.add(tuple.first());
        elements.add(tuple.second());
      }
    }

    if (trailingSeparator != null) {
      separators.add(trailingSeparator);
    }

    return new SeparatedListImpl<>(elements.build(), separators.build());
  }


  public ScriptTree script(InternalSyntaxToken fileOpeningTagToken, Optional<List<StatementTree>> statements) {
    return new ScriptTreeImpl(fileOpeningTagToken, optionalList(statements));
  }

  public ScriptTree script(InternalSyntaxToken anythingButOpeningTagToken) {
    return new ScriptTreeImpl(anythingButOpeningTagToken, ImmutableList.of());
  }

  public CompilationUnitTree compilationUnit(Optional<ScriptTree> script, Optional<InternalSyntaxToken> spacing, InternalSyntaxToken eofToken) {
    return new CompilationUnitTreeImpl(script.orNull(), eofToken);
  }

  /**
   * [ START ] Declarations
   */

  public VariableDeclarationTree variableDeclaration(InternalSyntaxToken identifierToken, Optional<Tuple<InternalSyntaxToken, ExpressionTree>> optionalEqual) {
    VariableIdentifierTreeImpl variableIdentifier = new VariableIdentifierTreeImpl(identifierToken);
    if (optionalEqual.isPresent()) {
      return new VariableDeclarationTreeImpl(variableIdentifier, optionalEqual.get().first(), optionalEqual.get().second());
    } else {
      return new VariableDeclarationTreeImpl(variableIdentifier, null, null);
    }
  }

  public VariableDeclarationTree staticVar(InternalSyntaxToken identifierToken, Optional<Tuple<InternalSyntaxToken, ExpressionTree>> optionalEqual) {
    return variableDeclaration(identifierToken, optionalEqual);
  }

  public VariableDeclarationTree memberConstDeclaration(InternalSyntaxToken identifierToken, Optional<Tuple<InternalSyntaxToken, ExpressionTree>> optionalEqual) {
    NameIdentifierTree identifier = new NameIdentifierTreeImpl(identifierToken);
    if (optionalEqual.isPresent()) {
      return new VariableDeclarationTreeImpl(identifier, optionalEqual.get().first(), optionalEqual.get().second());
    } else {
      return new VariableDeclarationTreeImpl(identifier, null, null);
    }
  }

  public VariableDeclarationTree constDeclaration(InternalSyntaxToken identifierToken, InternalSyntaxToken equToken, ExpressionTree expression) {
    return new VariableDeclarationTreeImpl(new NameIdentifierTreeImpl(identifierToken), equToken, expression);
  }

  public UseClauseTree useClause(NamespaceNameTree namespaceName, Optional<Tuple<InternalSyntaxToken, InternalSyntaxToken>> alias) {
    return groupUseClause(Optional.<InternalSyntaxToken>absent(), namespaceName, alias);
  }

  public UseClauseTree groupUseClause(
    Optional<InternalSyntaxToken> useTypeToken,
    NamespaceNameTree namespaceName,
    Optional<Tuple<InternalSyntaxToken, InternalSyntaxToken>> alias
  ) {
    if (alias.isPresent()) {
      NameIdentifierTreeImpl aliasName = new NameIdentifierTreeImpl(alias.get().second());
      return new UseClauseTreeImpl(useTypeToken.orNull(), namespaceName, alias.get().first(), aliasName);
    }
    return new UseClauseTreeImpl(useTypeToken.orNull(), namespaceName);
  }

  public ClassPropertyDeclarationTree classConstantDeclaration(
    Optional<SyntaxToken> visibility,
    InternalSyntaxToken constToken,
    VariableDeclarationTree firstDeclaration,
    Optional<List<Tuple<InternalSyntaxToken, VariableDeclarationTree>>> additionalDeclarations,
    InternalSyntaxToken eosToken
  ) {
    return ClassPropertyDeclarationTreeImpl.constant(visibility.orNull(), constToken, separatedList(firstDeclaration, additionalDeclarations), eosToken);
  }

  public ConstantDeclarationTree constantDeclaration(
    InternalSyntaxToken constToken,
    VariableDeclarationTree firstDeclaration,
    Optional<List<Tuple<InternalSyntaxToken, VariableDeclarationTree>>> additionalDeclarations,
    InternalSyntaxToken eosToken
  ) {
    return new ConstantDeclarationTreeImpl(constToken, separatedList(firstDeclaration, additionalDeclarations), eosToken);
  }

  public ClassPropertyDeclarationTree classVariableDeclaration(
    List<SyntaxToken> modifierTokens,
    VariableDeclarationTree firstVariable,
    Optional<List<Tuple<InternalSyntaxToken, VariableDeclarationTree>>> additionalVariables,
    InternalSyntaxToken eosToken
  ) {
    return ClassPropertyDeclarationTreeImpl.variable(modifierTokens, separatedList(firstVariable, additionalVariables), eosToken);
  }

  public MethodDeclarationTree methodDeclaration(
    Optional<List<SyntaxToken>> modifiers,
    InternalSyntaxToken functionToken,
    Optional<InternalSyntaxToken> referenceToken,
    NameIdentifierTree name,
    ParameterListTree parameters,
    Optional<ReturnTypeClauseTree> returnTypeClause,
    Tree body
  ) {
    return new MethodDeclarationTreeImpl(optionalList(modifiers), functionToken, referenceToken.orNull(), name, parameters, returnTypeClause.orNull(), body);
  }

  public FunctionDeclarationTree functionDeclaration(
    InternalSyntaxToken functionToken,
    Optional<InternalSyntaxToken> referenceToken,
    NameIdentifierTree name,
    ParameterListTree parameters,
    Optional<ReturnTypeClauseTree> returnTypeClauseTree,
    BlockTree body
  ) {
    return new FunctionDeclarationTreeImpl(functionToken, referenceToken.orNull(), name, parameters, returnTypeClauseTree.orNull(), body);
  }

  public ParameterListTree parameterList(
    InternalSyntaxToken leftParenthesis,
    Optional<Tuple<ParameterTree, Optional<List<Tuple<InternalSyntaxToken, ParameterTree>>>>> parameters,
    InternalSyntaxToken rightParenthesis
  ) {
    SeparatedListImpl<ParameterTree> separatedList = SeparatedListImpl.empty();
    if (parameters.isPresent()) {
      separatedList = separatedList(parameters.get().first(), parameters.get().second());
    }
    return new ParameterListTreeImpl(leftParenthesis, separatedList, rightParenthesis);
  }

  public ParameterTree parameter(
    Optional<TypeTree> type,
    Optional<InternalSyntaxToken> ampersand,
    Optional<InternalSyntaxToken> ellipsis,
    InternalSyntaxToken identifier,
    Optional<Tuple<InternalSyntaxToken, ExpressionTree>> eqAndInitValue
  ) {
    InternalSyntaxToken eqToken = null;
    ExpressionTree initValue = null;
    if (eqAndInitValue.isPresent()) {
      eqToken = eqAndInitValue.get().first();
      initValue = eqAndInitValue.get().second();
    }
    VariableIdentifierTree varIdentifier = new VariableIdentifierTreeImpl(identifier);
    return new ParameterTreeImpl(type.orNull(), ampersand.orNull(), ellipsis.orNull(), varIdentifier, eqToken, initValue);
  }

  public SeparatedListImpl<NamespaceNameTree> interfaceList(NamespaceNameTree first, Optional<List<Tuple<InternalSyntaxToken, NamespaceNameTree>>> others) {
    return separatedList(first, others);
  }

  public UseTraitDeclarationTree useTraitDeclaration(InternalSyntaxToken useToken, SeparatedListImpl<NamespaceNameTree> traits, InternalSyntaxToken eosToken) {
    return new UseTraitDeclarationTreeImpl(useToken, traits, eosToken);
  }

  public UseTraitDeclarationTree useTraitDeclaration(
    InternalSyntaxToken useToken,
    SeparatedListImpl<NamespaceNameTree> traits,
    InternalSyntaxToken openCurlyBrace,
    Optional<List<TraitAdaptationStatementTree>> adaptations,
    InternalSyntaxToken closeCurlyBrace
  ) {
    return new UseTraitDeclarationTreeImpl(useToken, traits, openCurlyBrace, optionalList(adaptations), closeCurlyBrace);
  }

  public TraitPrecedenceTree traitPrecedence(
    TraitMethodReferenceTree methodReference,
    InternalSyntaxToken insteadOfToken,
    SeparatedListImpl<NamespaceNameTree> traits,
    InternalSyntaxToken eosToken
  ) {
    return new TraitPrecedenceTreeImpl(methodReference, insteadOfToken, traits, eosToken);
  }

  public TraitAliasTree traitAlias(
    TraitMethodReferenceTree methodReference,
    InternalSyntaxToken asToken,
    Optional<SyntaxToken> modifier,
    NameIdentifierTree alias,
    InternalSyntaxToken eos
  ) {
    return new TraitAliasTreeImpl(methodReference, asToken, modifier.orNull(), alias, eos);
  }

  public TraitAliasTree traitAlias(
    TraitMethodReferenceTree methodReference,
    InternalSyntaxToken asToken,
    SyntaxToken modifier,
    InternalSyntaxToken eos
  ) {
    return new TraitAliasTreeImpl(methodReference, asToken, modifier, null, eos);
  }

  public TraitMethodReferenceTree traitMethodReference(InternalSyntaxToken identifier) {
    return new TraitMethodReferenceTreeImpl(new NameIdentifierTreeImpl(identifier));
  }

  public TraitMethodReferenceTree traitMethodReference(NamespaceNameTree trait, InternalSyntaxToken doubleColonToken, InternalSyntaxToken identifier) {
    return new TraitMethodReferenceTreeImpl(trait, doubleColonToken, new NameIdentifierTreeImpl(identifier));
  }

  public ClassDeclarationTree interfaceDeclaration(
    InternalSyntaxToken interfaceToken, NameIdentifierTree name,
    Optional<Tuple<InternalSyntaxToken, SeparatedListImpl<NamespaceNameTree>>> extendsClause,
    InternalSyntaxToken openCurlyBraceToken, Optional<List<ClassMemberTree>> members, InternalSyntaxToken closeCurlyBraceToken
  ) {
    InternalSyntaxToken extendsToken = null;
    SeparatedListImpl<NamespaceNameTree> interfaceList = SeparatedListImpl.empty();
    if (extendsClause.isPresent()) {
      extendsToken = extendsClause.get().first();
      interfaceList = extendsClause.get().second();
    }
    return ClassDeclarationTreeImpl.createInterface(
      interfaceToken,
      name,
      extendsToken,
      interfaceList,
      openCurlyBraceToken,
      optionalList(members),
      closeCurlyBraceToken
    );
  }

  public ClassDeclarationTree traitDeclaration(
    InternalSyntaxToken traitToken, NameIdentifierTree name,
    InternalSyntaxToken openCurlyBraceToken, Optional<List<ClassMemberTree>> members, InternalSyntaxToken closeCurlyBraceToken
  ) {
    return ClassDeclarationTreeImpl.createTrait(
      traitToken,
      name,
      openCurlyBraceToken,
      optionalList(members),
      closeCurlyBraceToken
    );
  }

  public ClassDeclarationTree classDeclaration(
    Optional<InternalSyntaxToken> modifier, InternalSyntaxToken classToken, NameIdentifierTree name,
    Optional<Tuple<InternalSyntaxToken, NamespaceNameTree>> extendsClause,
    Optional<Tuple<InternalSyntaxToken, SeparatedListImpl<NamespaceNameTree>>> implementsClause,
    InternalSyntaxToken openCurlyBrace, Optional<List<ClassMemberTree>> members, InternalSyntaxToken closeCurlyBrace
  ) {
    return ClassDeclarationTreeImpl.createClass(
      modifier.orNull(), classToken, name,
      extendsToken(extendsClause), superClass(extendsClause),
      implementsToken(implementsClause), superInterfaces(implementsClause),
      openCurlyBrace, optionalList(members), closeCurlyBrace
    );
  }

  /**
   * [ END ] Declarations
   */


  /**
   * [ START ] Statement
   */

  public GlobalStatementTree globalStatement(
    InternalSyntaxToken globalToken, VariableTree variable,
    Optional<List<Tuple<InternalSyntaxToken, VariableTree>>> variableRest, InternalSyntaxToken eosToken
  ) {
    return new GlobalStatementTreeImpl(
      globalToken,
      separatedList(variable, variableRest),
      eosToken
    );
  }

  public VariableTree globalVar(Optional<List<InternalSyntaxToken>> dollars, VariableTree variableTree) {
    if (dollars.isPresent()) {
      return new VariableVariableTreeImpl(dollars.get(), variableTree);
    }
    return variableTree;
  }

  public UseStatementTree useStatement(
    InternalSyntaxToken useToken,
    Optional<InternalSyntaxToken> useTypeToken,
    UseClauseTree firstDeclaration,
    Optional<List<Tuple<InternalSyntaxToken, UseClauseTree>>> additionalDeclarations,
    InternalSyntaxToken eosToken
  ) {
    SeparatedListImpl<UseClauseTree> declarations = separatedList(firstDeclaration, additionalDeclarations);
    return UseStatementTreeImpl.createUseStatement(useToken, useTypeToken.orNull(), declarations, eosToken);
  }

  public UseStatementTree groupUseStatement(
    InternalSyntaxToken useToken,
    Optional<InternalSyntaxToken> useTypeToken,
    NamespaceNameTree prefix,
    InternalSyntaxToken nsSeparator,
    InternalSyntaxToken lCurlyBrace,
    UseClauseTree firstDeclaration,
    Optional<List<Tuple<InternalSyntaxToken, UseClauseTree>>> additionalDeclarations,
    Optional<InternalSyntaxToken> trailingComma,
    InternalSyntaxToken rCurlyBrace,
    InternalSyntaxToken eosToken
  ) {
    SeparatedListImpl<UseClauseTree> declarations = separatedList(firstDeclaration, additionalDeclarations, trailingComma.orNull());
    return UseStatementTreeImpl.createGroupUseStatement(
      useToken,
      useTypeToken.orNull(),
      prefix,
      nsSeparator,
      lCurlyBrace,
      declarations,
      rCurlyBrace,
      eosToken);
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
    return new GotoStatementTreeImpl(gotoToken, new NameIdentifierTreeImpl(identifier), eos);
  }

  public ExpressionStatementTree expressionStatement(ExpressionTree expression, InternalSyntaxToken eos) {
    return new ExpressionStatementTreeImpl(expression, eos);
  }

  public ExpressionListStatementTree expressionListStatement(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> expressions, InternalSyntaxToken eos) {
    ImmutableList.Builder<ExpressionTree> elements = ImmutableList.builder();
    ImmutableList.Builder<SyntaxToken> separators = ImmutableList.builder();

    elements.add(exp1);

    if (expressions.isPresent()) {
      expressions.get().forEach(tuple -> {
        separators.add(tuple.first);
        elements.add(tuple.second);
      });
    }

    return new ExpressionListStatementTreeImpl(new SeparatedListImpl<>(elements.build(), separators.build()), eos);
  }

  public LabelTree label(InternalSyntaxToken identifier, InternalSyntaxToken colon) {
    return new LabelTreeImpl(new NameIdentifierTreeImpl(identifier), colon);
  }


  public TryStatementTree tryStatement(
    InternalSyntaxToken tryToken, BlockTree blockTree,
    Optional<List<CatchBlockTree>> catchBlocks,
    Optional<Tuple<InternalSyntaxToken, BlockTree>> finallyBlock
  ) {
    if (finallyBlock.isPresent()) {
      return new TryStatementTreeImpl(
        tryToken,
        blockTree,
        optionalList(catchBlocks),
        finallyBlock.get().first(),
        finallyBlock.get().second()
      );
    } else {
      return new TryStatementTreeImpl(
        tryToken,
        blockTree,
        optionalList(catchBlocks)
      );
    }
  }

  public TypeTree type(Optional<InternalSyntaxToken> questionMarkToken, TypeNameTree typeName) {
    return new TypeTreeImpl(questionMarkToken.orNull(), typeName);
  }

  public NamespaceNameTree namespaceName(List<Tuple<InternalSyntaxToken, InternalSyntaxToken>> tuples) {
    NameIdentifierTree lastPartIfOneTuple = new NameIdentifierTreeImpl(tuples.get(0).second());
    return namespaceName(lastPartIfOneTuple, tuples.get(0).first(), tuples.subList(1, tuples.size()));
  }

  public NamespaceNameTree namespaceName(InternalSyntaxToken token, Optional<List<Tuple<InternalSyntaxToken, InternalSyntaxToken>>> listOptional) {
    NameIdentifierTree lastPartIfNoTuples = new NameIdentifierTreeImpl(token);
    return namespaceName(lastPartIfNoTuples, null, listOptional.or(Collections.<Tuple<InternalSyntaxToken,InternalSyntaxToken>>emptyList()));
  }

  private static NamespaceNameTree namespaceName(
    NameIdentifierTree lastPartIfNoTuples,
    @Nullable InternalSyntaxToken absoluteSeparator,
    List<Tuple<InternalSyntaxToken, InternalSyntaxToken>> separatorIdentifierTuples
  ) {

    if (separatorIdentifierTuples.isEmpty()) {
      return new NamespaceNameTreeImpl(absoluteSeparator, SeparatedListImpl.<NameIdentifierTree>empty(), lastPartIfNoTuples);

    } else {
      ImmutableList.Builder<NameIdentifierTree> elements = ImmutableList.builder();
      ImmutableList.Builder<SyntaxToken> separators = ImmutableList.builder();

      elements.add(lastPartIfNoTuples);

      int lastIndex = separatorIdentifierTuples.size() - 1;
      Tuple<InternalSyntaxToken, InternalSyntaxToken> lastTuple = separatorIdentifierTuples.get(lastIndex);

      for (int i = 0; i < lastIndex; i++) {
        elements.add(new NameIdentifierTreeImpl(separatorIdentifierTuples.get(i).second()));
        separators.add(separatorIdentifierTuples.get(i).first());
      }

      separators.add(lastTuple.first());

      return new NamespaceNameTreeImpl(
        absoluteSeparator,
        new SeparatedListImpl<>(elements.build(), separators.build()),
        new NameIdentifierTreeImpl(lastTuple.second));
    }
  }

  public CatchBlockTree catchBlock(
    InternalSyntaxToken catchToken, InternalSyntaxToken lParenthesis,
    NamespaceNameTree exceptionType, Optional<List<Tuple<InternalSyntaxToken, NamespaceNameTree>>> additionalTypes,
    InternalSyntaxToken variable, InternalSyntaxToken rParenthsis, BlockTree block
  ) {
    SeparatedListImpl<NamespaceNameTree> exceptionTypes = separatedList(exceptionType, additionalTypes);
    return new CatchBlockTreeImpl(
      catchToken,
      lParenthesis,
      exceptionTypes,
      new VariableIdentifierTreeImpl(variable),
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
  private static ExpressionTree getForEachKey(Optional<Tuple<ExpressionTree, InternalSyntaxToken>> optionalKey) {
    if (optionalKey.isPresent()) {
      return optionalKey.get().first();
    } else {
      return null;
    }
  }

  @Nullable
  private static InternalSyntaxToken getForEachArrow(Optional<Tuple<ExpressionTree, InternalSyntaxToken>> optionalKey) {
    if (optionalKey.isPresent()) {
      return optionalKey.get().second();
    } else {
      return null;
    }
  }

  public ForStatementHeader forStatementHeader(
    InternalSyntaxToken forToken, InternalSyntaxToken lParenthesis,
    Optional<SeparatedListImpl<ExpressionTree>> init, InternalSyntaxToken semicolon1,
    Optional<SeparatedListImpl<ExpressionTree>> condition, InternalSyntaxToken semicolon2,
    Optional<SeparatedListImpl<ExpressionTree>> update, InternalSyntaxToken rParenthesis
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

  public SeparatedListImpl<ExpressionTree> forExpr(ExpressionTree expression, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> listOptional) {
    return separatedList(expression, listOptional);
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

  public DoWhileStatementTree doWhileStatement(
    InternalSyntaxToken doToken, StatementTree statement,
    InternalSyntaxToken whileToken, ParenthesisedExpressionTree condition,
    InternalSyntaxToken eosToken
  ) {
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
    Optional<List<StatementTree>> statements, InternalSyntaxToken endwhileToken, InternalSyntaxToken eosToken
  ) {
    return new WhileStatementTreeImpl(
      whileToken,
      condition,
      colonToken,
      optionalList(statements),
      endwhileToken,
      eosToken
    );
  }

  public SwitchStatementTree switchStatement(
    InternalSyntaxToken switchToken, ParenthesisedExpressionTree expression, InternalSyntaxToken openCurlyBraceToken,
    Optional<InternalSyntaxToken> semicolonToken,
    Optional<List<SwitchCaseClauseTree>> switchCaseClauses,
    InternalSyntaxToken closeCurlyBraceToken
  ) {
    return new SwitchStatementTreeImpl(
      switchToken,
      expression,
      openCurlyBraceToken,
      semicolonToken.orNull(),
      optionalList(switchCaseClauses),
      closeCurlyBraceToken
    );
  }

  public SwitchStatementTree alternativeSwitchStatement(
    InternalSyntaxToken switchToken, ParenthesisedExpressionTree expression, InternalSyntaxToken colonToken,
    Optional<InternalSyntaxToken> semicolonToken,
    Optional<List<SwitchCaseClauseTree>> switchCaseClauses,
    InternalSyntaxToken endswitchToken, InternalSyntaxToken eosToken
  ) {
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
    return new UnsetVariableStatementTreeImpl(
      unsetToken,
      openParenthesisToken,
      separatedList(expression, list),
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
    return new DeclareStatementHead(
      declareToken,
      openParenthesisToken,
      separatedList(firstDirective, optionalDirectives),
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

  public StaticStatementTree staticStatement(
    InternalSyntaxToken staticToken, VariableDeclarationTree variable,
    Optional<List<Tuple<InternalSyntaxToken, VariableDeclarationTree>>> listOptional,
    InternalSyntaxToken eosToken
  ) {
    return new StaticStatementTreeImpl(staticToken, separatedList(variable, listOptional), eosToken);
  }

  public ExpressionStatementTree haltCompilerStatement(
    InternalSyntaxToken haltCompilerToken,
    InternalSyntaxToken openParenthesisToken,
    InternalSyntaxToken closeParenthesisToken,
    InternalSyntaxToken eosToken
  ) {
    return new ExpressionStatementTreeImpl(
      new FunctionCallTreeImpl(
        new NamespaceNameTreeImpl(null, SeparatedListImpl.<NameIdentifierTree>empty(), new NameIdentifierTreeImpl(haltCompilerToken)),
        openParenthesisToken,
        SeparatedListImpl.<ExpressionTree>empty(),
        closeParenthesisToken),
      eosToken);
  }

  public ExpressionStatementTree echoStatement(
    InternalSyntaxToken echoToken,
    SeparatedListImpl<ExpressionTree> list,
    InternalSyntaxToken eosToken
  ) {
    return new ExpressionStatementTreeImpl(
      new FunctionCallTreeImpl(
        new NamespaceNameTreeImpl(null, SeparatedListImpl.<NameIdentifierTree>empty(), new NameIdentifierTreeImpl(echoToken)),
        list),
      eosToken);
  }

  /**
   * [ END ] Statement
   */

  /**
   * [ START ] Expression
   */

  public ExpressionTree castExpression(InternalSyntaxToken leftParenthesis, InternalSyntaxToken type, InternalSyntaxToken rightParenthesis, ExpressionTree expression) {
    return new CastExpressionTreeImpl(leftParenthesis, type, rightParenthesis, expression);
  }

  public ExpressionTree prefixExpr(Optional<List<InternalSyntaxToken>> operators, ExpressionTree expression) {
    ExpressionTree result = expression;
    if (operators.isPresent()) {
      for (InternalSyntaxToken operator : Lists.reverse(operators.get())) {
        Kind kind = UNARY_EXPRESSION_KINDS_BY_OPERATOR.get(operator.text());
        if (kind == null) {
          throw new IllegalArgumentException("Mapping not found for unary operator " + operator.text());
        }
        result = new PrefixExpressionTreeImpl(kind, operator, result);
      }
    }
    return result;
  }

  public ExpressionTree powerExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return rightAssociativeBinaryExpression(exp1, operatorsAndOperands);
  }

  public ExpressionTree nullCoalescingExpr(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    return rightAssociativeBinaryExpression(exp1, operatorsAndOperands);
  }

  private static ExpressionTree rightAssociativeBinaryExpression(ExpressionTree leftOperand, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    if (!operatorsAndOperands.isPresent()) {
      return leftOperand;
    }
    Tuple<InternalSyntaxToken, ExpressionTree> firstOperatorAndRightOperand = operatorsAndOperands.get().get(0);
    InternalSyntaxToken operator = firstOperatorAndRightOperand.first();
    ExpressionTree rightOperand = firstOperatorAndRightOperand.second();
    int size = operatorsAndOperands.get().size();
    if (size > 1) {
      List<Tuple<InternalSyntaxToken, ExpressionTree>> followingOperatorsAndOperands = operatorsAndOperands.get().subList(1, size);
      rightOperand = rightAssociativeBinaryExpression(firstOperatorAndRightOperand.second(), Optional.of(followingOperatorsAndOperands));
    }
    return new BinaryExpressionTreeImpl(binaryKind(operator), leftOperand, operator, rightOperand);
  }

  public ExpressionTree binaryExpression(ExpressionTree exp1, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> operatorsAndOperands) {
    if (!operatorsAndOperands.isPresent()) {
      return exp1;
    }

    ExpressionTree result = exp1;
    for (Tuple<InternalSyntaxToken, ExpressionTree> t : operatorsAndOperands.get()) {
      result = new BinaryExpressionTreeImpl(binaryKind(t.first()), result, t.first(), t.second());
    }
    return result;
  }

  private static Kind binaryKind(InternalSyntaxToken token) {
    Kind kind = BINARY_EXPRESSION_KINDS_BY_OPERATOR.get(token.text().toLowerCase(Locale.ENGLISH));
    if (kind == null) {
      throw new IllegalArgumentException("Mapping not found for binary operator " + token.text());
    }
    return kind;
  }

  public LiteralTree numericLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.NUMERIC_LITERAL, token);
  }

  public LiteralTree regularStringLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.REGULAR_STRING_LITERAL, token);
  }

  public ExpressionTree stringLiteral(ExpressionTree literal, Optional<ArrayAccessTree> arrayAccess) {
    if (arrayAccess.isPresent()) {
      return ((ArrayAccessTreeImpl) arrayAccess.get()).complete(literal);
    }
    return literal;
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

  public LiteralTree nowdocLiteral(InternalSyntaxToken token) {
    return new LiteralTreeImpl(Tree.Kind.NOWDOC_LITERAL, token);
  }

  public ExpandableStringCharactersTree expandableStringCharacters(InternalSyntaxToken token) {
    return new ExpandableStringCharactersTreeImpl(Kind.EXPANDABLE_STRING_CHARACTERS, token);
  }

  public ExpandableStringCharactersTree heredocStringCharacters(InternalSyntaxToken token) {
    return new ExpandableStringCharactersTreeImpl(Kind.HEREDOC_STRING_CHARACTERS, token);
  }

  public NameIdentifierTree identifier(InternalSyntaxToken token) {
    return new NameIdentifierTreeImpl(token);
  }

  public NameIdentifierTree identifierOrKeyword(InternalSyntaxToken token) {
    return identifier(token);
  }

  public ArrayAccessTree expandableArrayAccess(InternalSyntaxToken openBracket, ExpressionTree offset, InternalSyntaxToken closeBracket) {
    return new ArrayAccessTreeImpl(openBracket, offset, closeBracket);
  }

  public MemberAccessTree expandableObjectMemberAccess(InternalSyntaxToken arrow, NameIdentifierTree property) {
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
    return new NameIdentifierTreeImpl(token);
  }

  public ExpressionTree encapsulatedSemiComplexVariable(InternalSyntaxToken openDollarCurly, ExpressionTree expressionTree, InternalSyntaxToken closeCurly) {
    return new CompoundVariableTreeImpl(openDollarCurly, expressionTree, closeCurly);
  }

  public VariableIdentifierTree encapsulatedVariableIdentifier(InternalSyntaxToken spaces, InternalSyntaxToken variableIdentifier) {
    return new VariableIdentifierTreeImpl(variableIdentifier);
  }

  public ExpressionTree encapsulatedComplexVariable(InternalSyntaxToken openCurly, Tree lookahead, ExpressionTree expression, InternalSyntaxToken closeCurly) {
    return new ComputedVariableTreeImpl(openCurly, expression, closeCurly);
  }

  public ExpandableStringLiteralTree expandableStringLiteral(
    Tree spacing, InternalSyntaxToken openDoubleQuote,
    List<ExpressionTree> expressions,
    InternalSyntaxToken closeDoubleQuote
  ) {
    return new ExpandableStringLiteralTreeImpl(openDoubleQuote, expressions, closeDoubleQuote);
  }

  public YieldExpressionTree yieldExpression(InternalSyntaxToken yieldToken, Optional<ExpressionTree> expr) {
    return new YieldExpressionTreeImpl(yieldToken, expr.orNull());
  }

  public YieldExpressionTree yieldFromExpression(InternalSyntaxToken yieldToken, InternalSyntaxToken fromToken, ExpressionTree expr) {
    return new YieldExpressionTreeImpl(yieldToken, fromToken, expr);
  }

  public YieldExpressionTree yieldExpressionWithKey(InternalSyntaxToken yieldToken, ExpressionTree expr1, InternalSyntaxToken arrow, ExpressionTree expr2) {
    return new YieldExpressionTreeImpl(yieldToken, expr1, arrow, expr2);
  }

  public ParenthesisedExpressionTree parenthesizedExpression(InternalSyntaxToken openParenthesis, ExpressionTree expression, InternalSyntaxToken closeParenthesis) {
    return new ParenthesizedExpressionTreeImpl(openParenthesis, expression, closeParenthesis);
  }

  public ListExpressionTree listExpression(
    InternalSyntaxToken listToken, InternalSyntaxToken openParenthesis,
    Optional<ArrayAssignmentPatternElementTree> firstElement,
    Optional<List<Tuple<InternalSyntaxToken, Optional<ArrayAssignmentPatternElementTree>>>> rest,
    InternalSyntaxToken closeParenthesis
  ) {
    return new ListExpressionTreeImpl(
      listToken,
      openParenthesis,
      arrayAssignmentPatternElements(firstElement, rest),
      closeParenthesis);
  }

  public AssignmentExpressionTree listExpressionAssignment(ExpressionTree listExpression, InternalSyntaxToken equalToken, ExpressionTree expression) {
    return new AssignmentExpressionTreeImpl(Kind.ASSIGNMENT, listExpression, equalToken, expression);
  }

  public AssignmentExpressionTree arrayDestructuringAssignment(ExpressionTree arrayAssignmentPattern, InternalSyntaxToken equalToken, ExpressionTree expression) {
    return new AssignmentExpressionTreeImpl(Kind.ASSIGNMENT, arrayAssignmentPattern, equalToken, expression);
  }

  public ComputedVariableTree computedVariableName(InternalSyntaxToken openCurly, ExpressionTree expression, InternalSyntaxToken closeCurly) {
    return new ComputedVariableTreeImpl(openCurly, expression, closeCurly);
  }

  public VariableIdentifierTree variableIdentifier(InternalSyntaxToken variableIdentifier) {
    return new VariableIdentifierTreeImpl(variableIdentifier);
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

  public NameIdentifierTree newStaticIdentifier(InternalSyntaxToken staticToken) {
    return new NameIdentifierTreeImpl(staticToken);
  }

  public ReferenceVariableTree referenceVariable(InternalSyntaxToken ampersand, ExpressionTree variable) {
    return new ReferenceVariableTreeImpl(ampersand, variable);
  }

  public SpreadArgumentTree spreadArgument(InternalSyntaxToken ellipsis, ExpressionTree expression) {
    return new SpreadArgumentTreeImpl(ellipsis, expression);
  }

  public FunctionCallTree functionCallParameterList(
    InternalSyntaxToken openParenthesis,
    SeparatedListImpl<ExpressionTree> arguments,
    InternalSyntaxToken closeParenthesis
  ) {
    return new FunctionCallTreeImpl(openParenthesis, arguments, closeParenthesis);
  }

  public MemberAccessTree classMemberAccess(InternalSyntaxToken token, Tree member) {
    return new MemberAccessTreeImpl(Kind.CLASS_MEMBER_ACCESS, token, member);
  }

  public ExpressionTree objectDimensionalList(ExpressionTree variableName, Optional<List<ArrayAccessTree>> dimensionalOffsets) {
    ExpressionTree result = variableName;

    for (ArrayAccessTree arrayAccess : optionalList(dimensionalOffsets)) {
      result = ((ArrayAccessTreeImpl) arrayAccess).complete(result);
    }

    return result;
  }

  public NameIdentifierTree variableName(InternalSyntaxToken token) {
    return new NameIdentifierTreeImpl(token);
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

  public LexicalVariablesTree lexicalVariables(
    InternalSyntaxToken useToken, InternalSyntaxToken openParenthesis,
    VariableTree variable, Optional<List<Tuple<InternalSyntaxToken, VariableTree>>> variableRest,
    InternalSyntaxToken closeParenthesis
  ) {
    return new LexicalVariablesTreeImpl(useToken, openParenthesis, separatedList(variable, variableRest), closeParenthesis);
  }

  public FunctionCallTree internalFunction(
    InternalSyntaxToken issetToken, InternalSyntaxToken openParenthesis,
    ExpressionTree expression, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>> expressionRest,
    InternalSyntaxToken closeParenthesis
  ) {
    return internalFunction(
      issetToken,
      openParenthesis,
      separatedList(expression, expressionRest),
      closeParenthesis);
  }

  public FunctionCallTree internalFunction(
    InternalSyntaxToken functionNameToken, InternalSyntaxToken openParenthesis,
    ExpressionTree expression,
    InternalSyntaxToken closeParenthesis
  ) {
    return internalFunction(
      functionNameToken,
      openParenthesis,
      new SeparatedListImpl(ImmutableList.of(expression), ImmutableList.<InternalSyntaxToken>of()),
      closeParenthesis);
  }

  public FunctionCallTree internalFunction(InternalSyntaxToken includeOnceToken, ExpressionTree expression) {
    return internalFunction(
      includeOnceToken,
      null,
      new SeparatedListImpl(ImmutableList.of(expression), ImmutableList.<InternalSyntaxToken>of()),
      null);
  }

  public FunctionCallTree internalFunction(
    InternalSyntaxToken callee,
    @Nullable InternalSyntaxToken openParenthesis,
    SeparatedListImpl<ExpressionTree> arguments,
    @Nullable InternalSyntaxToken closeParenthesis
  ) {
    return new FunctionCallTreeImpl(
      new NamespaceNameTreeImpl(null, SeparatedListImpl.<NameIdentifierTree>empty(), new NameIdentifierTreeImpl(callee)),
      openParenthesis,
      arguments,
      closeParenthesis);
  }

  public ArrayPairTree arrayPair1(ExpressionTree expression, Optional<Tuple<InternalSyntaxToken, ExpressionTree>> pairExpression) {
    if (pairExpression.isPresent()) {
      return new ArrayPairTreeImpl(expression, pairExpression.get().first(), pairExpression.get().second());
    }
    return new ArrayPairTreeImpl(expression);
  }

  public ArrayPairTree arrayPair2(ReferenceVariableTree referenceVariableTree) {
    return new ArrayPairTreeImpl(referenceVariableTree);
  }

  public SeparatedListImpl<ArrayPairTree> arrayInitializerList(
    ArrayPairTree firstElement,
    Optional<List<Tuple<InternalSyntaxToken, ArrayPairTree>>> restElements,
    Optional<InternalSyntaxToken> trailingComma
  ) {
    return separatedList(firstElement, restElements, trailingComma.orNull());
  }

  public ArrayInitializerTree newArrayInitFunction(
    InternalSyntaxToken arrayToken, InternalSyntaxToken openParenthesis,
    Optional<SeparatedListImpl<ArrayPairTree>> elements,
    InternalSyntaxToken closeParenthesis
  ) {
    return new ArrayInitializerFunctionTreeImpl(
      arrayToken,
      openParenthesis,
      elements.isPresent() ? elements.get() : new SeparatedListImpl<>(ImmutableList.<ArrayPairTree>of(), ImmutableList.<SyntaxToken>of()),
      closeParenthesis);
  }

  public ArrayInitializerTree newArrayInitBracket(InternalSyntaxToken openBracket, Optional<SeparatedListImpl<ArrayPairTree>> elements, InternalSyntaxToken closeBracket) {
    return new ArrayInitializerBracketTreeImpl(
      openBracket,
      elements.isPresent() ? elements.get() : new SeparatedListImpl<>(ImmutableList.<ArrayPairTree>of(), ImmutableList.<SyntaxToken>of()),
      closeBracket);
  }

  public FunctionExpressionTree functionExpression(
    Optional<InternalSyntaxToken> staticToken,
    InternalSyntaxToken functionToken,
    Optional<InternalSyntaxToken> ampersandToken,
    ParameterListTree parameters,
    Optional<LexicalVariablesTree> lexicalVariables,
    Optional<ReturnTypeClauseTree> returnTypeClause,
    BlockTree block
  ) {

    return new FunctionExpressionTreeImpl(
      staticToken.orNull(),
      functionToken,
      ampersandToken.orNull(),
      parameters,
      lexicalVariables.orNull(),
      returnTypeClause.orNull(),
      block);
  }

  public NewExpressionTree newExpression(InternalSyntaxToken newToken, ExpressionTree expression) {
    return new NewExpressionTreeImpl(newToken, expression);
  }

  public FunctionCallTreeImpl newExitExpression(InternalSyntaxToken openParenthesis, Optional<ExpressionTree> expressionTreeOptional, InternalSyntaxToken closeParenthesis) {
    SeparatedListImpl<ExpressionTree> arguments;
    if (expressionTreeOptional.isPresent()) {
      arguments = new SeparatedListImpl<>(ImmutableList.of(expressionTreeOptional.get()), Collections.<SyntaxToken>emptyList());
    } else {
      arguments = SeparatedListImpl.empty();
    }
    return new FunctionCallTreeImpl(openParenthesis, arguments, closeParenthesis);
  }

  public FunctionCallTree completeExitExpression(InternalSyntaxToken exitOrDie, Optional<FunctionCallTreeImpl> partial) {
    NameIdentifierTreeImpl callee = new NameIdentifierTreeImpl(exitOrDie);
    return partial.isPresent() ? partial.get().complete(callee) : new FunctionCallTreeImpl(callee, SeparatedListImpl.<ExpressionTree>empty());
  }

  public ExpressionTree combinedScalarOffset(ArrayInitializerTree arrayInitialiser, Optional<List<ArrayAccessTree>> offsets) {
    ExpressionTree result = arrayInitialiser;
    for (ArrayAccessTree offset : optionalList(offsets)) {
      result = ((ArrayAccessTreeImpl) offset).complete(result);
    }

    return result;
  }

  public ExpressionTree postfixExpression(ExpressionTree expression, Optional<Object> optional) {
    if (optional.isPresent()) {

      if (optional.get() instanceof SyntaxToken) {
        SyntaxToken operator = (SyntaxToken) optional.get();

        return new PostfixExpressionTreeImpl(
          operator.text().equals(PHPPunctuator.INC.getValue()) ? Kind.POSTFIX_INCREMENT : Kind.POSTFIX_DECREMENT,
          expression,
          operator);

      } else {
        Tuple<InternalSyntaxToken, ExpressionTree> tuple = (Tuple) optional.get();
        return new BinaryExpressionTreeImpl(Kind.INSTANCE_OF, expression, tuple.first(), tuple.second);
      }
    }

    return expression;
  }

  public AssignmentExpressionTree assignmentExpression(ExpressionTree lhs, InternalSyntaxToken operatorToken, ExpressionTree rhs) {
    String operator = operatorToken.text();
    Kind kind = Kind.ASSIGNMENT;

    if ("**=".equals(operator)) {
      kind = Kind.POWER_ASSIGNMENT;
    } else if ("*=".equals(operator)) {
      kind = Kind.MULTIPLY_ASSIGNMENT;
    } else if ("/=".equals(operator)) {
      kind = Kind.DIVIDE_ASSIGNMENT;
    } else if ("%=".equals(operator)) {
      kind = Kind.REMAINDER_ASSIGNMENT;
    } else if ("+=".equals(operator)) {
      kind = Kind.PLUS_ASSIGNMENT;
    } else if ("-=".equals(operator)) {
      kind = Kind.MINUS_ASSIGNMENT;
    } else if ("<<=".equals(operator)) {
      kind = Kind.LEFT_SHIFT_ASSIGNMENT;
    } else if (">>=".equals(operator)) {
      kind = Kind.RIGHT_SHIFT_ASSIGNMENT;
    } else if ("&=".equals(operator)) {
      kind = Kind.AND_ASSIGNMENT;
    } else if ("^=".equals(operator)) {
      kind = Kind.XOR_ASSIGNMENT;
    } else if ("|=".equals(operator)) {
      kind = Kind.OR_ASSIGNMENT;
    } else if (".=".equals(operator)) {
      kind = Kind.CONCATENATION_ASSIGNMENT;
    }

    return new AssignmentExpressionTreeImpl(kind, lhs, operatorToken, rhs);
  }

  public AssignmentExpressionTree assignmentByReference(ExpressionTree lhs, InternalSyntaxToken equToken, InternalSyntaxToken ampersandToken, ExpressionTree rhs) {
    return new AssignmentByReferenceTreeImpl(lhs, equToken, ampersandToken, rhs);
  }

  public ConditionalExpressionTreeImpl newConditionalExpr(
    InternalSyntaxToken queryToken, Optional<ExpressionTree> trueExpression,
    InternalSyntaxToken colonToken, ExpressionTree falseExpression
  ) {
    return new ConditionalExpressionTreeImpl(queryToken, trueExpression.orNull(), colonToken, falseExpression);
  }

  public ExpressionTree completeConditionalExpr(ExpressionTree expression, Optional<List<ConditionalExpressionTreeImpl>> partial) {
    ExpressionTree result = expression;
    if (partial.isPresent()) {
      for (ConditionalExpressionTreeImpl conditionalExpressionTree : partial.get()) {
        result = conditionalExpressionTree.complete(result);
      }
    }
    return result;
  }

  public BuiltInTypeTree builtInType(InternalSyntaxToken token) {
    return new BuiltInTypeTreeImpl(token);
  }

  public ReturnTypeClauseTree returnTypeClause(InternalSyntaxToken colonToken, TypeTree typeTree) {
    return new ReturnTypeClauseTreeImpl(colonToken, typeTree);
  }

  public SeparatedListImpl<ExpressionTree> arguments(Optional<Tuple<ExpressionTree, Optional<List<Tuple<InternalSyntaxToken, ExpressionTree>>>>> arguments) {
    SeparatedListImpl<ExpressionTree> list;
    if (arguments.isPresent()) {
      list = separatedList(arguments.get().first(), arguments.get().second());
    } else {
      list = SeparatedListImpl.empty();
    }

    return list;
  }

  public AnonymousClassTree anonymousClass(
    InternalSyntaxToken classToken,
    Optional<InternalSyntaxToken> lParenthesis, SeparatedListImpl<ExpressionTree> arguments, Optional<InternalSyntaxToken> rParenthesis,
    Optional<Tuple<InternalSyntaxToken, NamespaceNameTree>> extendsClause,
    Optional<Tuple<InternalSyntaxToken, SeparatedListImpl<NamespaceNameTree>>> implementsClause,
    InternalSyntaxToken lCurlyBrace, Optional<List<ClassMemberTree>> members, InternalSyntaxToken rCurlyBrace
  ) {
    return new AnonymousClassTreeImpl(
      classToken,
      lParenthesis.orNull(),
      arguments,
      rParenthesis.orNull(),
      extendsToken(extendsClause), superClass(extendsClause),
      implementsToken(implementsClause), superInterfaces(implementsClause),
      lCurlyBrace,
      optionalList(members),
      rCurlyBrace
    );
  }

  private static InternalSyntaxToken extendsToken(Optional<Tuple<InternalSyntaxToken, NamespaceNameTree>> extendsClause) {
    return extendsClause.isPresent() ? extendsClause.get().first() : null;
  }

  private static NamespaceNameTree superClass(Optional<Tuple<InternalSyntaxToken, NamespaceNameTree>> extendsClause) {
    return extendsClause.isPresent() ? extendsClause.get().second() : null;
  }

  private static InternalSyntaxToken implementsToken(Optional<Tuple<InternalSyntaxToken, SeparatedListImpl<NamespaceNameTree>>> implementsClause) {
    return implementsClause.isPresent() ? implementsClause.get().first() : null;
  }

  private static SeparatedListImpl<NamespaceNameTree> superInterfaces(Optional<Tuple<InternalSyntaxToken, SeparatedListImpl<NamespaceNameTree>>> implementsClause) {
    return implementsClause.isPresent() ? implementsClause.get().second() : SeparatedListImpl.<NamespaceNameTree>empty();
  }

  public HeredocStringLiteralTree heredocStringLiteral(InternalSyntaxToken token) {
    return new HeredocStringLiteralTreeImpl(token);
  }

  public HeredocStringLiteralTreeImpl.HeredocBody heredocBodyTree(List<ExpressionTree> expressions) {
    return new HeredocStringLiteralTreeImpl.HeredocBody(expressions);
  }

  public ArrayAssignmentPatternTree arrayAssignmentPattern(
    InternalSyntaxToken lBracket,
    Optional<ArrayAssignmentPatternElementTree> firstElement,
    Optional<List<Tuple<InternalSyntaxToken, Optional<ArrayAssignmentPatternElementTree>>>> rest,
    InternalSyntaxToken rBracket
  ) {

    return new ArrayAssignmentPatternTreeImpl(lBracket, arrayAssignmentPatternElements(firstElement, rest), rBracket);
  }

  private ArrayAssignmentPatternElements arrayAssignmentPatternElements(
    Optional<ArrayAssignmentPatternElementTree> firstElement,
    Optional<List<Tuple<InternalSyntaxToken, Optional<ArrayAssignmentPatternElementTree>>>> rest
  ) {
    List<Tuple<SyntaxToken, java.util.Optional<ArrayAssignmentPatternElementTree>>> otherElements = Collections.emptyList();
    if (rest.isPresent()) {
      otherElements = rest.get().stream()
        .map(t -> newTuple((SyntaxToken) t.first(), optional(t.second())))
        .collect(Collectors.toList());
    }
    return new ArrayAssignmentPatternElements(firstElement.orNull(), otherElements);
  }

  private static <T> java.util.Optional<T> optional(Optional<T> sslrOptional) {
    return java.util.Optional.ofNullable(sslrOptional.orNull());
  }

  public ArrayAssignmentPatternTree arrayAssignmentPattern(
    InternalSyntaxToken lBracket,
    ArrayAssignmentPatternElementTree firstElement,
    Optional<List<Tuple<InternalSyntaxToken, Optional<ArrayAssignmentPatternElementTree>>>> rest,
    InternalSyntaxToken rBracket
  ) {
    return arrayAssignmentPattern(lBracket, Optional.of(firstElement), rest, rBracket);
  }

  public ArrayAssignmentPatternTree arrayAssignmentPattern(
    InternalSyntaxToken lBracket, List<Tuple<InternalSyntaxToken, Optional<ArrayAssignmentPatternElementTree>>> rest, InternalSyntaxToken rBracket
  ) {
    return arrayAssignmentPattern(lBracket, Optional.absent(), Optional.of(rest), rBracket);
  }

  public ArrayAssignmentPatternElementTree arrayAssignmentPatternElement(Optional<Tuple<ExpressionTree, InternalSyntaxToken>> key, Tree variable) {
    if (key.isPresent()) {
      return new ArrayAssignmentPatternElementTreeImpl(key.get().first(), key.get().second(), variable);
    }
    return new ArrayAssignmentPatternElementTreeImpl(variable);
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

  public <T, U> Tuple<T, U> newTuple(T first, U second) {
    return new Tuple<>(first, second);
  }

  public List<SyntaxToken> singleToken(SyntaxToken token) {
    return ImmutableList.of(token);
  }

}
