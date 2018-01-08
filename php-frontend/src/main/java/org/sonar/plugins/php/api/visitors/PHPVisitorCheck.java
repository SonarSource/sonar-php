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

import java.util.Iterator;
import java.util.List;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.visitors.PHPCheckContext;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
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

public abstract class PHPVisitorCheck implements VisitorCheck {

  private PHPCheckContext context;

  @Override
  public void init() {
    // Default behavior : do nothing.
  }

  @Override
  public void visitToken(SyntaxToken token) {
    scan(token);
  }

  @Override
  public void visitTrivia(SyntaxTrivia trivia) {
    // Do nothing is the default behavior (There is no children to visit)
  }

  @Override
  public void visitVariableDeclaration(VariableDeclarationTree tree) {
    scan(tree);
  }

  @Override
  public void visitNamespaceName(NamespaceNameTree tree) {
    scan(tree);
  }

  @Override
  public void visitUseClause(UseClauseTree tree) {
    scan(tree);
  }

  @Override
  public void visitClassPropertyDeclaration(ClassPropertyDeclarationTree tree) {
    scan(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    scan(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    scan(tree);
  }

  @Override
  public void visitParameterList(ParameterListTree tree) {
    scan(tree);
  }

  @Override
  public void visitParameter(ParameterTree tree) {
    scan(tree);
  }

  @Override
  public void visitUseTraitDeclaration(UseTraitDeclarationTree tree) {
    scan(tree);
  }

  @Override
  public void visitTraitPrecedence(TraitPrecedenceTree tree) {
    scan(tree);
  }

  @Override
  public void visitTraitAlias(TraitAliasTree tree) {
    scan(tree);
  }

  @Override
  public void visitTraitMethodReference(TraitMethodReferenceTree tree) {
    scan(tree);
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    scan(tree);
  }

  @Override
  public void visitConstDeclaration(ConstantDeclarationTree tree) {
    scan(tree);
  }

  @Override
  public void visitStaticStatement(StaticStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitDeclareStatement(DeclareStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitInlineHTML(InlineHTMLTree tree) {
    scan(tree);
  }

  @Override
  public void visitGlobalStatement(GlobalStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitUseStatement(UseStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitUnsetVariableStatement(UnsetVariableStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitYieldStatement(YieldStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitDefaultClause(DefaultClauseTree tree) {
    scan(tree);
  }

  @Override
  public void visitCaseClause(CaseClauseTree tree) {
    scan(tree);
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitWhileStatement(WhileStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitDoWhileStatement(DoWhileStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitElseifClause(ElseifClauseTree tree) {
    scan(tree);
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitElseClause(ElseClauseTree tree) {
    scan(tree);
  }

  @Override
  public void visitBlock(BlockTree tree) {
    scan(tree);
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitThrowStatement(ThrowStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitEmptyStatement(EmptyStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitReturnStatement(ReturnStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitContinueStatement(ContinueStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitBreakStatement(BreakStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    scan(tree);
  }

  @Override
  public void visitTryStatement(TryStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitGotoStatement(GotoStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitExpressionListStatement(ExpressionListStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitArrayAssignmentPattern(ArrayAssignmentPatternTree tree) {
    scan(tree);
  }

  @Override
  public void visitArrayAssignmentPatternElement(ArrayAssignmentPatternElementTree tree) {
    scan(tree);
  }

  @Override
  public void visitLabel(LabelTree tree) {
    scan(tree);
  }

  @Override
  public void visitNamespaceStatement(NamespaceStatementTree tree) {
    scan(tree);
  }

  @Override
  public void visitCastExpression(CastExpressionTree tree) {
    scan(tree);
  }

  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    scan(tree);
  }

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    scan(tree);
  }

  @Override
  public void visitVariableIdentifier(VariableIdentifierTree tree) {
    scan(tree);
  }

  @Override
  public void visitNameIdentifier(NameIdentifierTree tree) {
    scan(tree);
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    scan(tree);
  }

  @Override
  public void visitExpandableStringCharacters(ExpandableStringCharactersTree tree) {
    scan(tree);
  }

  @Override
  public void visitArrayAccess(ArrayAccessTree tree) {
    scan(tree);
  }

  @Override
  public void visitMemberAccess(MemberAccessTree tree) {
    scan(tree);
  }

  @Override
  public void visitCompoundVariable(CompoundVariableTree tree) {
    scan(tree);
  }

  @Override
  public void visitComputedVariable(ComputedVariableTree tree) {
    scan(tree);
  }

  @Override
  public void visitExpandableStringLiteral(ExpandableStringLiteralTree tree) {
    scan(tree);
  }

  @Override
  public void visitYieldExpression(YieldExpressionTree tree) {
    scan(tree);
  }

  @Override
  public void visitParenthesisedExpression(ParenthesisedExpressionTree tree) {
    scan(tree);
  }

  @Override
  public void visitListExpression(ListExpressionTree tree) {
    scan(tree);
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    scan(tree);
  }

  @Override
  public void visitVariableVariable(VariableVariableTree tree) {
    scan(tree);
  }

  @Override
  public void visitReferenceVariable(ReferenceVariableTree tree) {
    scan(tree);
  }

  @Override
  public void visitSpreadArgument(SpreadArgumentTree tree) {
    scan(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    scan(tree);
  }

  @Override
  public void visitLexicalVariables(LexicalVariablesTree tree) {
    scan(tree);
  }

  @Override
  public void visitArrayPair(ArrayPairTree tree) {
    scan(tree);
  }

  @Override
  public void visitArrayInitializerFunction(ArrayInitializerFunctionTree tree) {
    scan(tree);
  }

  @Override
  public void visitArrayInitializerBracket(ArrayInitializerBracketTree tree) {
    scan(tree);
  }

  @Override
  public void visitScript(ScriptTree tree) {
    scan(tree);
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    scan(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    scan(tree);
  }

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    scan(tree);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    scan(tree);
  }

  @Override
  public void visitPostfixExpression(UnaryExpressionTree tree) {
    scan(tree);
  }

  @Override
  public void visitConditionalExpression(ConditionalExpressionTree tree) {
    scan(tree);
  }

  @Override
  public void visitType(TypeTree tree) {
    scan(tree);
  }

  @Override
  public void visitBuiltInType(BuiltInTypeTree tree) {
    scan(tree);
  }

  @Override
  public void visitReturnTypeClause(ReturnTypeClauseTree tree) {
    scan(tree);
  }

  @Override
  public void visitHeredoc(HeredocStringLiteralTree tree) {
    scan(tree);
  }

  @Override
  public CheckContext context() {
    return context;
  }

  protected void scan(Tree tree) {
    Iterator<Tree> childrenIterator = ((PHPTree) tree).childrenIterator();
    Tree child;

    while (childrenIterator.hasNext()) {
      child = childrenIterator.next();
      if (child != null) {
        child.accept(this);
      }
    }
  }

  protected <T extends Tree> void scan(List<T> trees) {
    for (T tree : trees) {
      tree.accept(this);
    }
  }


  @Override
  public final List<PhpIssue> analyze(PhpFile file, CompilationUnitTree tree) {
    this.context = new PHPCheckContext(file, tree);
    visitCompilationUnit(tree);

    return context().getIssues();
  }

  @Override
  public List<PhpIssue> analyze(PhpFile file, CompilationUnitTree tree, SymbolTable symbolTable) {
    this.context = new PHPCheckContext(file, tree, symbolTable);
    visitCompilationUnit(tree);

    return context().getIssues();
  }
}
