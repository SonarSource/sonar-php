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

import java.util.Locale;
import org.sonar.check.Rule;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2166")
public class ClassNamedLikeExceptionCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Rename this class to remove \"Exception\" or correct its inheritance.";
  private static final QualifiedName EXCEPTION_FQN = QualifiedName.qualifiedName("Exception");

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    if (tree.is(Tree.Kind.CLASS_DECLARATION) && tree.name().text().toLowerCase(Locale.ENGLISH).endsWith("exception") && Symbols.get(tree).isOrSubClassOf(EXCEPTION_FQN).isFalse()) {
      context().newIssue(this, tree.name(), MESSAGE);
    }
    super.visitClassDeclaration(tree);
  }

}
