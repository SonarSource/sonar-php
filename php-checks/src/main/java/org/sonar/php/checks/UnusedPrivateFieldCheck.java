/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.tree.TreeUtils;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1068")
public class UnusedPrivateFieldCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Remove this unused \"%s\" private field.";

  private static final Set<String> constantUsedBeforeInit = new HashSet<>();

  @Override
  public void visitMemberAccess(MemberAccessTree tree) {
    if (tree.is(Tree.Kind.CLASS_MEMBER_ACCESS) && tree.member().is(Tree.Kind.NAME_IDENTIFIER) && isSelfConstantAccess(tree.object())) {
      constantUsedBeforeInit.add(((NameIdentifierTree) tree.member()).text());
    }

    super.visitMemberAccess(tree);
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);

    for (Symbol fieldSymbol : getFieldSymbolsForCurrentClass(tree)) {
      if (fieldSymbol.hasModifier("private") && fieldSymbol.usages().isEmpty() && !constantUsedBeforeInit.contains(fieldSymbol.name())) {
        context().newIssue(this, fieldSymbol.declaration(), String.format(MESSAGE, fieldSymbol.name()));
      }
    }

    constantUsedBeforeInit.clear();
  }

  private List<Symbol> getFieldSymbolsForCurrentClass(ClassDeclarationTree tree) {
    List<Tree.Kind> classDeclarationKind = Collections.singletonList(Tree.Kind.CLASS_DECLARATION);

    return context().symbolTable().getSymbols(Kind.FIELD).stream()
      .filter(f -> TreeUtils.findAncestorWithKind(f.declaration(), Collections.singletonList(Tree.Kind.ANONYMOUS_CLASS)) == null)
      .filter(f -> TreeUtils.findAncestorWithKind(f.declaration(), classDeclarationKind) == tree)
      .toList();
  }

  private static boolean isSelfConstantAccess(ExpressionTree tree) {
    if (tree.is(Tree.Kind.NAMESPACE_NAME)) {
      String className = ((NamespaceNameTree) tree).fullName();
      if (TreeUtils.findAncestorWithKind(tree, Collections.singleton(Tree.Kind.ANONYMOUS_CLASS)) == null && className.equals("self")) {
        return true;
      }

      ClassDeclarationTree classDeclaration = (ClassDeclarationTree) TreeUtils.findAncestorWithKind(tree, Collections.singleton(Tree.Kind.CLASS_DECLARATION));
      return classDeclaration != null && classDeclaration.name().text().equals(className);
    }

    return false;
  }

}
