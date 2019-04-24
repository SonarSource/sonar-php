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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.type.NewObjectCall;
import org.sonar.php.checks.utils.type.ObjectMemberFunctionCall;
import org.sonar.php.checks.utils.type.TreeValues;
import org.sonar.php.checks.utils.type.TypePredicateList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2077")
public class QueryUsageCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that formatting this SQL query is safe here.";
  private static final NewObjectCall IS_PDO_OBJECT = new NewObjectCall("PDO");
  private static final NewObjectCall IS_MYSQLI_OBJECT = new NewObjectCall("mysqli");

  private static final Map<String, Integer> SUSPICIOUS_GLOBAL_FUNCTIONS = buildSuspiciousGlobalFunctions();

  private static Map<String, Integer> buildSuspiciousGlobalFunctions() {
    Map<String, Integer> map = new HashMap<>();
    map.put("mssql_query", 0);
    map.put("mysql_query", 0);
    map.put("mysql_db_query", 1);
    map.put("mysql_unbuffered_query", 0);
    map.put("pg_send_query", 1);
    map.put("mysqli_query", 1);
    map.put("mysqli_real_query", 1);
    map.put("mysqli_multi_query", 1);
    map.put("mysqli_send_query", 1);
    return map;
  }

  private static final Predicate<TreeValues> SUSPICIOUS_QUERY_PREDICATES = new TypePredicateList(
    new ObjectMemberFunctionCall("exec", IS_PDO_OBJECT),
    new ObjectMemberFunctionCall("query", IS_PDO_OBJECT, IS_MYSQLI_OBJECT),
    new ObjectMemberFunctionCall("real_query", IS_MYSQLI_OBJECT),
    new ObjectMemberFunctionCall("multi_query", IS_MYSQLI_OBJECT),
    new ObjectMemberFunctionCall("send_query", IS_MYSQLI_OBJECT));

  private static final Predicate<TreeValues> PDO_PREPARE_PREDICATE = new ObjectMemberFunctionCall("prepare", new NewObjectCall("PDO"));

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    TreeValues possibleValues = TreeValues.of(tree, context().symbolTable());
    if (isSuspiciousGlobalFunction(tree) || isSuspiciousMemberFunction(tree, possibleValues) || isSuspiciousPrepareStatement(tree, possibleValues)) {
      context().newIssue(this, tree.callee(), MESSAGE);
    }

    super.visitFunctionCall(tree);
  }

  private static boolean isSuspiciousGlobalFunction(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();
    if (callee.is(Tree.Kind.NAMESPACE_NAME)) {
      String qualifiedNameLowerCase = ((NamespaceNameTree) callee).qualifiedName().toLowerCase(Locale.ENGLISH);
      if (SUSPICIOUS_GLOBAL_FUNCTIONS.containsKey(qualifiedNameLowerCase)) {
        Integer index = SUSPICIOUS_GLOBAL_FUNCTIONS.get(qualifiedNameLowerCase);
        return tree.arguments().size() > index && isSuspiciousArgument(tree.arguments().get(index));
      } else if ("pg_query".equals(qualifiedNameLowerCase)) {
        // First argument of function 'pg_query' is optional
        return !tree.arguments().isEmpty() && isSuspiciousArgument(tree.arguments().get(tree.arguments().size() - 1));
      }
    }
    return false;
  }

  private static boolean isSuspiciousMemberFunction(FunctionCallTree tree, TreeValues possibleValues) {
    return SUSPICIOUS_QUERY_PREDICATES.test(possibleValues) && !tree.arguments().isEmpty() && isSuspiciousArgument(tree.arguments().get(0));
  }

  private static boolean isSuspiciousPrepareStatement(FunctionCallTree tree, TreeValues possibleValues) {
    return PDO_PREPARE_PREDICATE.test(possibleValues) && !tree.arguments().isEmpty() && isSuspiciousArgument(tree.arguments().get(0));
  }

  private static boolean isSuspiciousArgument(ExpressionTree expression) {
    return expression.is(Tree.Kind.EXPANDABLE_STRING_LITERAL) || expression.is(Kind.CONCATENATION);
  }

}
