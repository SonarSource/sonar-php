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
package org.sonar.plugins.php.api.visitors;

import com.google.common.annotations.Beta;
import org.sonar.php.tree.impl.expression.IdentifierTreeImpl;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
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
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;

@Beta
public interface TreeVisitor {

  /**
   * [ START ] Declaration
   */

  void visitNamespaceName(NamespaceNameTree tree);

  /**
   * [ END ] Declaration
   */


  /**
   * [ START ] Statement
   */

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

  /**
   * [ END ] Statement
   */

  void visitVariableIdentifier(VariableIdentifierTree tree);

  void visitIdentifier(IdentifierTreeImpl identifierTree);
}

