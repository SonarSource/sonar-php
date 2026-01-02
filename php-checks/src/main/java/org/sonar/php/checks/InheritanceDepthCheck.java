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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = "S110")
public class InheritanceDepthCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "This class has %d parents which is greater than %d authorized.";
  private static final String SECONDARY_MESSAGE = "Parent class.";

  public static final int DEFAULT_MAX = 5;

  @RuleProperty(
    key = "max",
    description = "Maximum depth of the inheritance tree. (Number)",
    defaultValue = "" + DEFAULT_MAX)
  public int max = DEFAULT_MAX;

  @RuleProperty(
    key = "filteredClasses",
    description = "Classes to be filtered out of the count of inheritance. Ex : RuntimeException, Exception")
  public String filteredClasses = "";

  private Set<QualifiedName> filteredOutClassNames;
  private int inheritanceCounter = 0;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    ClassSymbol symbol = Symbols.get(tree);
    checkClassInheritance(symbol, tree.name());

    super.visitClassDeclaration(tree);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    NamespaceNameTree superClass = tree.superClass();
    if (superClass != null) {
      ClassSymbol symbol = Symbols.getClass(superClass);
      if (!symbol.isUnknownSymbol()) {
        inheritanceCounter = 1;
        checkClassInheritance(symbol, tree.classToken());
      }
    }

    super.visitAnonymousClass(tree);
  }

  private void checkClassInheritance(ClassSymbol symbol, Tree tree) {
    Optional<ClassSymbol> superClass = symbol.superClass();
    Set<ClassSymbol> superClasses = new HashSet<>();
    while (superClass.filter(classSymbol -> !classSymbol.isUnknownSymbol()).isPresent()) {
      ClassSymbol superClassSymbol = superClass.get();
      QualifiedName qualifiedName = superClassSymbol.qualifiedName();

      if (getFilteredOutClasses().contains(qualifiedName) || !superClasses.add(superClassSymbol)) {
        break;
      }

      inheritanceCounter++;
      superClass = superClassSymbol.superClass();
    }

    if (inheritanceCounter > max) {
      PreciseIssue issue = newIssue(tree, String.format(MESSAGE, inheritanceCounter, max))
        .cost(inheritanceCounter - max);
      superClasses.forEach(e -> issue.secondary(e.location(), SECONDARY_MESSAGE));
    }
    inheritanceCounter = 0;
  }

  private Set<QualifiedName> getFilteredOutClasses() {
    if (filteredOutClassNames == null) {
      filteredOutClassNames = Arrays.stream(filteredClasses.split(","))
        .map(String::trim)
        .map(QualifiedName::qualifiedName)
        .collect(Collectors.toSet());
    }
    return filteredOutClassNames;
  }
}
