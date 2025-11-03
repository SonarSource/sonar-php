/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
    var qualifiedName = tree.namespaceName().qualifiedName();
    var topLevelNamespace = extractTopLevelNamespace(qualifiedName);

    if ("Drupal".equalsIgnoreCase(topLevelNamespace)) {
      this.framework = SymbolTable.Framework.DRUPAL;
    } else if ("Yii".equalsIgnoreCase(topLevelNamespace)) {
      this.framework = SymbolTable.Framework.YII;
    }
  }

  public SymbolTable.Framework getFramework() {
    return framework;
  }

  private static String extractTopLevelNamespace(String qualifiedName) {
    int separatorIndex = qualifiedName.indexOf('\\');
    if (separatorIndex == -1) {
      return qualifiedName;
    }
    return qualifiedName.substring(0, separatorIndex);
  }
}
