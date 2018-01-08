/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.SyntacticEquivalence;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = DuplicatedArgumentCheck.KEY)
public class DuplicatedArgumentCheck extends PHPVisitorCheck {

  public static final String KEY = "S4142";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    SeparatedList<ExpressionTree> arguments = tree.arguments();
    int arity = arguments.size();
    if (arity <= 1) {
      return;
    }
    Set<ExpressionTree> reported = new HashSet<>();
    for (int i = 0; i < arity; i++) {
      ExpressionTree arg = CheckUtils.skipParenthesis(arguments.get(i));
      if (shouldBeSkipped(arg)) {
        continue;
      }
      for (int j = i + 1; j < arity; j++) {
        ExpressionTree otherArg = CheckUtils.skipParenthesis(arguments.get(j));
        if (!reported.contains(otherArg) && SyntacticEquivalence.areSyntacticallyEquivalent(arg, otherArg)) {
          context()
            .newIssue(this, otherArg, String.format("Verify that this is the intended value; it is the same as the %s argument.", argumentNumber(i + 1)))
            .secondary(arg, null);
          reported.add(otherArg);
        }
      }
    }
    super.visitFunctionCall(tree);
  }

  private static boolean shouldBeSkipped(ExpressionTree arg) {
    return isLiteral(arg)
      || isVariable(arg)
      || isAccessedVariable(arg)
      || isEmptyArrayConstructor(arg)
      || isNewObject(arg);
  }

  private static boolean isNewObject(ExpressionTree arg) {
    return arg.is(Tree.Kind.NEW_EXPRESSION);
  }

  private static boolean isEmptyArrayConstructor(ExpressionTree arg) {
    return (arg.is(Tree.Kind.ARRAY_INITIALIZER_FUNCTION, Tree.Kind.ARRAY_INITIALIZER_BRACKET) &&
      ((ArrayInitializerTree) arg).arrayPairs().isEmpty());
  }

  private static boolean isAccessedVariable(ExpressionTree arg) {
    return arg.is(Tree.Kind.CLASS_MEMBER_ACCESS, Tree.Kind.OBJECT_MEMBER_ACCESS) && isVariable(((MemberAccessTree) arg).member());
  }

  private static boolean isVariable(Tree arg) {
    return arg.is(Tree.Kind.VARIABLE_IDENTIFIER, Tree.Kind.VARIABLE_VARIABLE, Tree.Kind.NAME_IDENTIFIER);
  }

  private static boolean isLiteral(Tree arg) {
    return arg.is(Tree.Kind.NUMERIC_LITERAL, Tree.Kind.BOOLEAN_LITERAL, Tree.Kind.NULL_LITERAL, Tree.Kind.REGULAR_STRING_LITERAL);
  }

  private static String argumentNumber(int index) {
    switch (index) {
      case 1:
        return "1st";
      case 2:
        return "2nd";
      case 3:
        return "3rd";
      default:
        return index + "th";
    }
  }
}
