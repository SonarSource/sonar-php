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
package org.sonar.php.tree.visitors;

import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

/**
 * Visitor that detects the framework used in the analyzed file.
 */
public class FrameworkDetectionVisitor extends PHPVisitorCheck {
  private SymbolTable.Framework framework = SymbolTable.Framework.EMPTY;

  @Override
  public void visitUseClause(UseClauseTree tree) {
    if (tree.namespaceName().qualifiedName().startsWith("Drupal")) {
      this.framework = SymbolTable.Framework.DRUPAL;
    }
  }

  public SymbolTable.Framework getFramework() {
    return framework;
  }
}