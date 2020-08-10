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

import org.sonar.php.tree.impl.declaration.ClassDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.NamespaceNameTreeImpl;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;

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
}
