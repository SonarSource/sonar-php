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
package org.sonar.php.symbols;

import java.util.Optional;
import org.sonar.php.tree.impl.declaration.ClassDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.MethodDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.NamespaceNameTreeImpl;
import org.sonar.php.tree.impl.expression.AnonymousClassTreeImpl;
import org.sonar.php.tree.impl.expression.NameIdentifierTreeImpl;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import static org.sonar.php.utils.SymbolUtils.isFunctionCall;
import static org.sonar.php.utils.SymbolUtils.isMethodCall;
import static org.sonar.php.utils.SymbolUtils.isResolvable;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NAMESPACE_NAME;

/**
 * Utility class to retrieve symbols from the AST.
 * We can drop this class as soon as we expose an equivalent API directly on the AST interfaces.
 */
public class Symbols {

  private Symbols() {
  }

  public static ClassSymbol get(ClassDeclarationTree classDeclarationTree) {
    return ((ClassDeclarationTreeImpl) classDeclarationTree).symbol();
  }

  public static Optional<FunctionSymbol> get(FunctionCallTree functionCallTree) {
    ExpressionTree callee = functionCallTree.callee();
    if (isFunctionCall(functionCallTree) && callee.is(NAMESPACE_NAME)) {
      return Optional.of(getFunction((NamespaceNameTree) functionCallTree.callee()));
    } else if (isMethodCall(functionCallTree) && isResolvable((MemberAccessTree) callee)) {
      return Optional.of(getMethod((NameIdentifierTree) ((MemberAccessTree) callee).member()));
    }
    return Optional.empty();
  }

  public static ClassSymbol getClass(NamespaceNameTree namespaceNameTree) {
    Symbol symbol = ((NamespaceNameTreeImpl) namespaceNameTree).symbol();
    if (symbol instanceof ClassSymbol) {
      return (ClassSymbol) symbol;
    }
    throw new IllegalStateException("No class symbol available on " + namespaceNameTree);
  }

  public static FunctionSymbol getFunction(NamespaceNameTree namespaceNameTree) {
    Symbol symbol = ((NamespaceNameTreeImpl) namespaceNameTree).symbol();
    if (symbol instanceof FunctionSymbol) {
      return (FunctionSymbol) symbol;
    }
    throw new IllegalStateException("No function symbol available on " + namespaceNameTree);
  }

  public static ClassSymbol get(AnonymousClassTree anonymousClassTree) {
    return ((AnonymousClassTreeImpl) anonymousClassTree).symbol();
  }

  public static MethodSymbol get(MethodDeclarationTree methodDeclarationTree) {
    return ((MethodDeclarationTreeImpl) methodDeclarationTree).symbol();
  }

  public static MethodSymbol getMethod(NameIdentifierTree nameIdentifierTree) {
    Symbol symbol = ((NameIdentifierTreeImpl) nameIdentifierTree).symbol();
    if (symbol instanceof MethodSymbol) {
      return (MethodSymbol) symbol;
    }
    throw new IllegalStateException("No method symbol available on " + nameIdentifierTree);
  }
}
