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

import java.util.Collections;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = ConstructorDeclarationCheck.KEY)
public class ConstructorDeclarationCheck extends PHPSubscriptionCheck {
  public static final String KEY = "S1603";

  private static final String MESSAGE_OLD_STYLE_PRESENT = "Replace this function name \"%s\" with \"__construct\".";
  private static final String MESSAGE_BOTH_STYLE_PRESENT = "Replace this function name \"%s\", since a \"__construct\" method has already been defined in this class.";

  @Override
  public List<Kind> nodesToVisit() {
    return Collections.singletonList(Kind.CLASS_DECLARATION);
  }

  @Override
  public void visitNode(Tree tree) {
    ClassDeclarationTree classDec = (ClassDeclarationTree) tree;

    MethodDeclarationTree oldStyleConstructor = null;
    MethodDeclarationTree newStyleConstructor = null;

    boolean namespaceContext = isClassInNamespaceContext(classDec);

    for (ClassMemberTree member : classDec.members()) {
      if (member.is(Kind.METHOD_DECLARATION)) {
        MethodDeclarationTree method = (MethodDeclarationTree) member;
        String methodName = method.name().text();

        if (classDec.name().text().equalsIgnoreCase(methodName) && !namespaceContext) {
          oldStyleConstructor = method;

        } else if (ClassTree.PHP5_CONSTRUCTOR_NAME.equalsIgnoreCase(methodName)) {
          newStyleConstructor = method;
        }
      }
    }

    if (oldStyleConstructor != null) {
      String message = String.format(
        newStyleConstructor != null ? MESSAGE_BOTH_STYLE_PRESENT : MESSAGE_OLD_STYLE_PRESENT,
        oldStyleConstructor.name().text());

      context().newIssue(this, oldStyleConstructor.name(), message);
    }
  }

  private boolean isClassInNamespaceContext(ClassDeclarationTree classDec) {
    Symbol symbol = context().symbolTable().getSymbol(classDec.name());
    QualifiedName qualifiedName = symbol.qualifiedName();
    return qualifiedName != null && !qualifiedName.toString().equals(symbol.name());
  }
}
