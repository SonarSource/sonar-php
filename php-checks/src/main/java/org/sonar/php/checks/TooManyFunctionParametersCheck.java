/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.checks;

import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.symbols.Symbols;
import org.sonar.php.symbols.Trilean;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static java.util.function.Predicate.not;

@Rule(key = TooManyFunctionParametersCheck.KEY)
public class TooManyFunctionParametersCheck extends PHPVisitorCheck {

  public static final String KEY = "S107";

  private static final String MESSAGE = "This function has %s parameters, which is greater than the %s authorized.";

  public static final int DEFAULT_MAX = 7;
  public static final int DEFAULT_CONSTRUCTOR_MAX = 7;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT_MAX)
  public int max = DEFAULT_MAX;

  @RuleProperty(
    key = "constructorMax",
    defaultValue = "" + DEFAULT_CONSTRUCTOR_MAX)
  public int constructorMax = DEFAULT_CONSTRUCTOR_MAX;

  private ClassTree classTree;

  @Override
  public void visitParameterList(ParameterListTree parameterList) {
    int numberOfParameters = getNumberOfParametersExcludingPromotedProperties(parameterList);
    int maxValue = isConstructorParameterList(parameterList) ? constructorMax : max;
    if (numberOfParameters > maxValue && isOverriding(parameterList.getParent()).isFalse()) {
      context().newIssue(this, parameterList, String.format(MESSAGE, numberOfParameters, maxValue));
    }
    super.visitParameterList(parameterList);
  }

  private static int getNumberOfParametersExcludingPromotedProperties(ParameterListTree parameterList) {
    return (int) parameterList.parameters().stream()
      .filter(not(ParameterTree::isPropertyPromotion))
      .count();
  }

  private static Trilean isOverriding(@Nullable Tree tree) {
    return Optional.ofNullable(tree)
      .filter(t -> t.is(Tree.Kind.METHOD_DECLARATION))
      .map(t -> Symbols.get(((MethodDeclarationTree) t)).isOverriding())
      .orElse(Trilean.FALSE);
  }

  private boolean isConstructorParameterList(ParameterListTree parameterList) {
    if (classTree != null) {
      MethodDeclarationTree constructor = classTree.fetchConstructor();
      if (constructor != null) {
        return parameterList.equals(constructor.parameters());
      }
    }
    return false;
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    classTree = tree;
    super.visitClassDeclaration(tree);
    classTree = null;
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    classTree = tree;
    super.visitAnonymousClass(tree);
    classTree = null;
  }

}
