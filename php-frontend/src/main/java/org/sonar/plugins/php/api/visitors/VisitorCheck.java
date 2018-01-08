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
package org.sonar.plugins.php.api.visitors;

import com.google.common.annotations.Beta;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.declaration.BuiltInTypeTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ConstantDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerBracketTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerFunctionTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.CastExpressionTree;
import org.sonar.plugins.php.api.tree.expression.CompoundVariableTree;
import org.sonar.plugins.php.api.tree.expression.ComputedVariableTree;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
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
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableVariableTree;
import org.sonar.plugins.php.api.tree.expression.YieldExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
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
import org.sonar.plugins.php.api.tree.statement.StaticStatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
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

@Beta
public interface VisitorCheck extends PHPCheck {

  void visitToken(SyntaxToken token);

  void visitTrivia(SyntaxTrivia trivia);

  /**
   * [ START ] Declaration
   */

  void visitVariableDeclaration(VariableDeclarationTree tree);

  void visitNamespaceName(NamespaceNameTree tree);

  void visitUseClause(UseClauseTree tree);

  void visitClassPropertyDeclaration(ClassPropertyDeclarationTree tree);

  void visitMethodDeclaration(MethodDeclarationTree tree);

  void visitFunctionDeclaration(FunctionDeclarationTree tree);

  void visitParameterList(ParameterListTree tree);

  void visitParameter(ParameterTree tree);

  void visitUseTraitDeclaration(UseTraitDeclarationTree tree);

  void visitTraitPrecedence(TraitPrecedenceTree tree);

  void visitTraitAlias(TraitAliasTree tree);

  void visitTraitMethodReference(TraitMethodReferenceTree tree);

  void visitClassDeclaration(ClassDeclarationTree tree);

  void visitType(TypeTree tree);

  void visitBuiltInType(BuiltInTypeTree tree);

  void visitReturnTypeClause(ReturnTypeClauseTree tree);

  /**
   * [ END ] Declaration
   */


  /**
   * [ START ] Statement
   */

  void visitConstDeclaration(ConstantDeclarationTree tree);

  void visitStaticStatement(StaticStatementTree tree);

  void visitDeclareStatement(DeclareStatementTree tree);

  void visitInlineHTML(InlineHTMLTree tree);

  void visitGlobalStatement(GlobalStatementTree tree);

  void visitUseStatement(UseStatementTree tree);

  void visitUnsetVariableStatement(UnsetVariableStatementTree tree);

  void visitYieldStatement(YieldStatementTree tree);

  void visitDefaultClause(DefaultClauseTree tree);

  void visitCaseClause(CaseClauseTree tree);

  void visitSwitchStatement(SwitchStatementTree tree);

  void visitWhileStatement(WhileStatementTree tree);

  void visitDoWhileStatement(DoWhileStatementTree tree);

  void visitElseifClause(ElseifClauseTree tree);

  void visitIfStatement(IfStatementTree tree);

  void visitElseClause(ElseClauseTree tree);

  void visitBlock(BlockTree tree);

  void visitForStatement(ForStatementTree tree);

  void visitForEachStatement(ForEachStatementTree tree);

  void visitThrowStatement(ThrowStatementTree tree);

  void visitEmptyStatement(EmptyStatementTree tree);

  void visitReturnStatement(ReturnStatementTree tree);

  void visitContinueStatement(ContinueStatementTree tree);

  void visitBreakStatement(BreakStatementTree tree);

  void visitCatchBlock(CatchBlockTree tree);

  void visitTryStatement(TryStatementTree tree);

  void visitGotoStatement(GotoStatementTree tree);

  void visitExpressionStatement(ExpressionStatementTree tree);

  void visitLabel(LabelTree tree);

  void visitNamespaceStatement(NamespaceStatementTree tree);

  /**
   * [ END ] Statement
   */

  /**
   * [ START ] Expression
   */

  void visitCastExpression(CastExpressionTree tree);

  void visitPrefixExpression(UnaryExpressionTree tree);

  void visitBinaryExpression(BinaryExpressionTree tree);

  void visitVariableIdentifier(VariableIdentifierTree tree);

  void visitNameIdentifier(NameIdentifierTree tree);

  void visitLiteral(LiteralTree tree);

  void visitExpandableStringCharacters(ExpandableStringCharactersTree tree);

  void visitArrayAccess(ArrayAccessTree tree);

  void visitMemberAccess(MemberAccessTree tree);

  void visitCompoundVariable(CompoundVariableTree tree);

  void visitComputedVariable(ComputedVariableTree tree);

  void visitExpandableStringLiteral(ExpandableStringLiteralTree tree);

  void visitYieldExpression(YieldExpressionTree tree);

  void visitParenthesisedExpression(ParenthesisedExpressionTree tree);

  void visitListExpression(ListExpressionTree tree);

  void visitArrayAssignmentPattern(ArrayAssignmentPatternTree tree);

  void visitArrayAssignmentPatternElement(ArrayAssignmentPatternElementTree tree);

  void visitAssignmentExpression(AssignmentExpressionTree tree);

  void visitVariableVariable(VariableVariableTree tree);

  void visitReferenceVariable(ReferenceVariableTree tree);

  void visitSpreadArgument(SpreadArgumentTree tree);

  void visitFunctionCall(FunctionCallTree tree);

  void visitLexicalVariables(LexicalVariablesTree tree);

  void visitArrayPair(ArrayPairTree tree);

  void visitArrayInitializerFunction(ArrayInitializerFunctionTree tree);

  void visitArrayInitializerBracket(ArrayInitializerBracketTree tree);

  void visitScript(ScriptTree tree);

  void visitCompilationUnit(CompilationUnitTree tree);

  void visitFunctionExpression(FunctionExpressionTree tree);

  void visitNewExpression(NewExpressionTree tree);

  void visitPostfixExpression(UnaryExpressionTree tree);

  void visitConditionalExpression(ConditionalExpressionTree tree);

  void visitAnonymousClass(AnonymousClassTree tree);

  void visitHeredoc(HeredocStringLiteralTree tree);

  void visitExpressionListStatement(ExpressionListStatementTree tree);
  /**
   * [ END ] Expression
   */

}

