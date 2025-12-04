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
package org.sonar.php.checks.security;

import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.type.ArrayAccess;
import org.sonar.php.checks.utils.type.NewObjectCall;
import org.sonar.php.checks.utils.type.ObjectMemberFunctionCall;
import org.sonar.php.checks.utils.type.TreeValues;
import org.sonar.php.checks.utils.type.TypePredicateList;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S3011")
public class ChangingAccessibilityCheck extends PHPVisitorCheck {

  private static final String UPDATE_MESSAGE = "Make sure that this accessibility update is safe here.";
  private static final String BYPASS_MESSAGE = "Make sure that this accessibility bypass is safe here.";

  private static final NewObjectCall REFLECTION_CLASS = new NewObjectCall("ReflectionClass");
  private static final NewObjectCall REFLECTION_OBJECT = new NewObjectCall("ReflectionObject");
  private static final NewObjectCall REFLECTION_METHOD = new NewObjectCall("ReflectionMethod");
  private static final NewObjectCall REFLECTION_PROPERTY = new NewObjectCall("ReflectionProperty");

  private static final ObjectMemberFunctionCall REFLECTION_CLASS_GET_PROPERTY = new ObjectMemberFunctionCall("getProperty", REFLECTION_CLASS, REFLECTION_OBJECT);
  private static final ObjectMemberFunctionCall REFLECTION_CLASS_GET_PROPERTIES = new ObjectMemberFunctionCall("getProperties", REFLECTION_CLASS, REFLECTION_OBJECT);
  private static final ObjectMemberFunctionCall REFLECTION_CLASS_GET_METHODS = new ObjectMemberFunctionCall("getMethods", REFLECTION_CLASS, REFLECTION_OBJECT);
  private static final ObjectMemberFunctionCall REFLECTION_CLASS_GET_METHOD = new ObjectMemberFunctionCall("getMethod", REFLECTION_CLASS, REFLECTION_OBJECT);
  private static final ObjectMemberFunctionCall REFLECTION_CLASS_GET_CONSTRUCTOR = new ObjectMemberFunctionCall("getConstructor", REFLECTION_CLASS, REFLECTION_OBJECT);

  private static final Predicate<TreeValues> BYPASS_PREDICATES = new TypePredicateList(
    new ObjectMemberFunctionCall("getClosure",
      new ArrayAccess(REFLECTION_CLASS_GET_METHODS),
      REFLECTION_CLASS_GET_CONSTRUCTOR,
      REFLECTION_CLASS_GET_METHOD,
      REFLECTION_METHOD),

    new ObjectMemberFunctionCall("getConstant", REFLECTION_CLASS, REFLECTION_OBJECT),

    new ObjectMemberFunctionCall("getConstants", REFLECTION_CLASS, REFLECTION_OBJECT),

    new ObjectMemberFunctionCall("getReflectionConstant", REFLECTION_CLASS, REFLECTION_OBJECT),

    new ObjectMemberFunctionCall("getReflectionConstants", REFLECTION_CLASS, REFLECTION_OBJECT),

    new ObjectMemberFunctionCall("getStaticProperties", REFLECTION_CLASS, REFLECTION_OBJECT),

    new ObjectMemberFunctionCall("getValue",
      new ArrayAccess(REFLECTION_CLASS_GET_PROPERTIES),
      REFLECTION_CLASS_GET_PROPERTY,
      REFLECTION_PROPERTY),

    new ObjectMemberFunctionCall("newInstanceWithoutConstructor", REFLECTION_CLASS, REFLECTION_OBJECT),

    new ObjectMemberFunctionCall("setValue",
      new ArrayAccess(REFLECTION_CLASS_GET_PROPERTIES),
      REFLECTION_CLASS_GET_PROPERTY,
      REFLECTION_PROPERTY));

  private static final Predicate<TreeValues> UPDATE_PREDICATES = new ObjectMemberFunctionCall("setAccessible",
    new ArrayAccess(REFLECTION_CLASS_GET_METHODS, REFLECTION_CLASS_GET_PROPERTIES),
    REFLECTION_CLASS_GET_CONSTRUCTOR,
    REFLECTION_CLASS_GET_METHOD,
    REFLECTION_CLASS_GET_PROPERTY,
    REFLECTION_METHOD,
    REFLECTION_PROPERTY);

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    TreeValues possibleValues = TreeValues.of(tree, context().symbolTable());
    if (BYPASS_PREDICATES.test(possibleValues)) {
      context().newIssue(this, tree, BYPASS_MESSAGE);
    } else if (UPDATE_PREDICATES.test(possibleValues)) {
      context().newIssue(this, tree, UPDATE_MESSAGE);
    }
    super.visitFunctionCall(tree);
  }

}
