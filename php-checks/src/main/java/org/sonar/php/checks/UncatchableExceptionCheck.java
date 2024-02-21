/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = UncatchableExceptionCheck.KEY)
public class UncatchableExceptionCheck extends PHPVisitorCheck {

  public static final String KEY = "S5911";
  private static final String MESSAGE = "Create class \"%s\" in namespace or check correct import of class";
  private static final String GLOBAL_NAMESPACE = "";

  private String currentNamespace = GLOBAL_NAMESPACE;

  @Override
  public void visitNamespaceStatement(NamespaceStatementTree tree) {
    currentNamespace = tree.namespaceName() != null ? tree.namespaceName().qualifiedName().toLowerCase() : GLOBAL_NAMESPACE;
    super.visitNamespaceStatement(tree);

    if (isBracketedNamespace(tree)) {
      currentNamespace = "";
    }
  }

  private boolean isBracketedNamespace(NamespaceStatementTree tree) {
    return tree.openCurlyBrace() != null;
  }

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    if (!currentNamespace.isEmpty()) {
      tree.exceptionTypes().stream()
        .filter(this::isMissingException)
        .forEach(this::raiseIssue);
    }

    super.visitCatchBlock(tree);
  }

  private void raiseIssue(NamespaceNameTree tree) {
    context().newIssue(this, tree, String.format(MESSAGE, tree.fullyQualifiedName()));
  }

  private boolean isMissingException(NamespaceNameTree tree) {
    ClassSymbol symbol = Symbols.getClass(tree);
    return symbol.isUnknownSymbol() && symbol.qualifiedName().toString().startsWith(currentNamespace);
  }
}
