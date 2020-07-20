/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.tree.symbols;

import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.ClassSymbolIndex;
import org.sonar.php.tree.impl.declaration.NamespaceNameTreeImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;

class SymbolUsageVisitor extends NamespaceNameResolvingVisitor {

  private final ClassSymbolIndex classSymbolIndex;

  SymbolUsageVisitor(SymbolTableImpl symbolTable, ClassSymbolIndex classSymbolIndex) {
    super(symbolTable);
    this.classSymbolIndex = classSymbolIndex;
  }

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    for (NamespaceNameTree exceptionType : tree.exceptionTypes()) {
      resolveClassSymbol(exceptionType);
    }
    super.visitCatchBlock(tree);
  }

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    ExpressionTree expression = tree.expression();
    if (expression.is(Tree.Kind.FUNCTION_CALL)) {
      expression = ((FunctionCallTree) expression).callee();
    }
    if (expression.is(Tree.Kind.NAMESPACE_NAME)) {
      resolveClassSymbol((NamespaceNameTree) expression);
    }
    super.visitNewExpression(tree);
  }

  private void resolveClassSymbol(NamespaceNameTree namespaceName) {
    QualifiedName fqn = getFullyQualifiedName(namespaceName, Symbol.Kind.CLASS);
    ClassSymbol classSymbol = classSymbolIndex.get(fqn);
    ((NamespaceNameTreeImpl) namespaceName).setSymbol(classSymbol);
  }
}
