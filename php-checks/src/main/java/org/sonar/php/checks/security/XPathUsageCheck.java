/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.php.checks.security;

import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.type.FunctionCall;
import org.sonar.php.checks.utils.type.NewObjectCall;
import org.sonar.php.checks.utils.type.ObjectMemberFunctionCall;
import org.sonar.php.checks.utils.type.TreeValues;
import org.sonar.php.checks.utils.type.TypePredicateList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S4817")
public class XPathUsageCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that executing this XPATH expression is safe.";

  private static final Predicate<TreeValues> XPATH_PREDICATES = new TypePredicateList(
    new ObjectMemberFunctionCall("query", new NewObjectCall("DOMXpath")),
    new ObjectMemberFunctionCall("evaluate", new NewObjectCall("DOMXpath")),
    new ObjectMemberFunctionCall("xpath",
      new NewObjectCall("SimpleXMLElement"),
      new FunctionCall("simplexml_load_file"),
      new FunctionCall("simplexml_load_string"),
      new FunctionCall("simplexml_import_dom"))
    );

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    TreeValues possibleValues = TreeValues.of(tree, context().symbolTable());
    if (XPATH_PREDICATES.test(possibleValues) && firstArgIsNotHardcoded(tree)) {
      context().newIssue(this, tree, MESSAGE);
    }
    super.visitFunctionCall(tree);
  }

  private static boolean firstArgIsNotHardcoded(FunctionCallTree tree) {
    return !tree.arguments().isEmpty() && !tree.arguments().get(0).is(Tree.Kind.REGULAR_STRING_LITERAL);
  }

}
