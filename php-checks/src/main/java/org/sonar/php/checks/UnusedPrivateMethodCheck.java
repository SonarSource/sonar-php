/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1144")
public class UnusedPrivateMethodCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Remove this unused private \"%s\" method.";
  private static final Pattern USES_PHPDOC_PATTERN = Pattern.compile("@uses\\s+(\\w+::)?(\\w+)");

  private final List<String> stringLiterals = new ArrayList<>();
  private final List<String> dynamicUsedMethods = new ArrayList<>();

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    stringLiterals.clear();
    super.visitClassDeclaration(tree);

    if (tree.is(Tree.Kind.CLASS_DECLARATION, Tree.Kind.ENUM_DECLARATION)) {
      checkClass(tree);
    }
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    ((PHPTree) tree).getFirstToken().trivias().stream()
      .flatMap(trivia -> USES_PHPDOC_PATTERN.matcher(trivia.text()).results())
      .map(result -> result.group(2))
      .map(methodName -> methodName.toLowerCase(Locale.ROOT))
      .forEach(dynamicUsedMethods::add);

    super.visitMethodDeclaration(tree);
  }

  private void checkClass(ClassTree tree) {
    Scope classScope = context().symbolTable().getScopeFor(tree);
    for (Symbol methodSymbol : classScope.getSymbols(Kind.FUNCTION)) {

      // For enums private and protected are equivalent as inheritance is not allowed.
      boolean ruleConditions = (methodSymbol.hasModifier("private") || isProtectedEnumMethod(tree, methodSymbol)) &&
        methodSymbol.usages().isEmpty();

      if (ruleConditions
        && !dynamicUsedMethods.contains(methodSymbol.name())
        && !isConstructor(methodSymbol.declaration(), tree)
        && !isMagicMethod(methodSymbol.name())
        && !isUsedInStringLiteral(methodSymbol)) {
        context().newIssue(this, methodSymbol.declaration(), String.format(MESSAGE, methodSymbol.name()));
      }
    }
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    stringLiterals.clear();
    super.visitAnonymousClass(tree);

    checkClass(tree);
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      stringLiterals.add(CheckUtils.trimQuotes(tree).toLowerCase(Locale.ROOT));
    }
  }

  private static boolean isProtectedEnumMethod(ClassTree tree, Symbol methodSymbol) {
    return tree.is(Tree.Kind.ENUM_DECLARATION) && methodSymbol.hasModifier("protected");
  }

  private boolean isUsedInStringLiteral(Symbol methodSymbol) {
    for (String stringLiteral : stringLiterals) {
      if (stringLiteral.contains(methodSymbol.name())) {
        return true;
      }
    }
    return false;
  }

  private static boolean isConstructor(IdentifierTree methodName, ClassTree classDec) {
    MethodDeclarationTree constructor = classDec.fetchConstructor();
    return  constructor != null && constructor.name().equals(methodName);
  }

  private static boolean isMagicMethod(String methodName) {
    return methodName.startsWith("__");
  }
}
