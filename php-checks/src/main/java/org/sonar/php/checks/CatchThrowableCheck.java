/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S5708")
public class CatchThrowableCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Change this type to be a class deriving from \"Throwable\".";
  private static final String SECONDARY_MESSAGE = "Class definition.";
  private static final QualifiedName THROWABLE_FQN = QualifiedName.qualifiedName("Throwable");

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    tree.exceptionTypes().stream()
      .filter(type -> !Symbols.getClass(type).is(ClassSymbol.Kind.INTERFACE))
      .filter(type -> Symbols.getClass(type).isSubTypeOf(THROWABLE_FQN).isFalse())
      .forEach(this::addIssue);
    super.visitCatchBlock(tree);
  }

  private void addIssue(NamespaceNameTree tree) {
    context().newIssue(this, tree, MESSAGE)
      .secondary(Symbols.getClass(tree).location(), SECONDARY_MESSAGE);
  }
}
