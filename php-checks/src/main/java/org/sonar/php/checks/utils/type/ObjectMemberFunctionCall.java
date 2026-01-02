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
package org.sonar.php.checks.utils.type;

import java.util.function.Predicate;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;

public class ObjectMemberFunctionCall extends TreeKindPredicate<FunctionCallTree> {

  private final String name;

  private final TypePredicateList nestedPredicates;

  public ObjectMemberFunctionCall(String name, Predicate<TreeValues>... nestedPredicates) {
    super(Tree.Kind.FUNCTION_CALL);
    this.name = name;
    this.nestedPredicates = new TypePredicateList(nestedPredicates);
  }

  @Override
  protected boolean matches(TreeValues possibleValues, FunctionCallTree functionCall) {
    ExpressionTree callee = functionCall.callee();
    if (callee.is(Tree.Kind.OBJECT_MEMBER_ACCESS)) {
      MemberAccessTree memberAccess = (MemberAccessTree) callee;
      return memberAccess.member().is(Tree.Kind.NAME_IDENTIFIER) &&
        name.equalsIgnoreCase(((NameIdentifierTree) memberAccess.member()).text()) &&
        nestedPredicates.test(possibleValues.lookupPossibleValues(memberAccess.object()));
    }
    return false;
  }

}
