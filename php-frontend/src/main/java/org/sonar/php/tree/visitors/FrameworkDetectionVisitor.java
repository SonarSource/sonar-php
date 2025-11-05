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

import java.util.Locale;
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
    if (qualifiedName.startsWith("Drupal")) {
      this.framework = SymbolTable.Framework.DRUPAL;
    } else if (isWordPressNamespace(qualifiedName)) {
      this.framework = SymbolTable.Framework.WORDPRESS;
    } else if (qualifiedName.startsWith("Yii")) {
      this.framework = SymbolTable.Framework.YII;
    }
  }

  public SymbolTable.Framework getFramework() {
    return framework;
  }

  private static boolean isWordPressNamespace(String qualifiedName) {
    var normalized = qualifiedName.toUpperCase(Locale.ROOT);
    return normalized.startsWith("WP_")
      || normalized.startsWith("WP\\")
      || normalized.startsWith("WORDPRESS_")
      || normalized.startsWith("WORDPRESS\\");
  }
}
