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
package org.sonar.php.checks.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2077")
public class QueryUsageCheck extends PHPVisitorCheck {

  private static final Set<String> SQL_QUERY_METHODS = new HashSet<>(Arrays.asList(
    "mysql_query",
    "mysql_db_query",
    "mysql_unbuffered_query",
    "pg_update",
    "pg_query",
    "pg_send_query",
    "mssql_query",
    "mysqli_query", "mysqli::query",
    "mysqli_real_query", "mysqli::real_query",
    "mysqli_multi_query", "mysqli::multi_query",
    "mysqli_send_query", "mysqli::send_query",
    "PDO::query",
    "PDO::exec"));

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    super.visitFunctionCall(tree);
    String functionName = CheckUtils.getFunctionName(tree).toLowerCase();
    if (SQL_QUERY_METHODS.contains(functionName)) {
      context().newIssue(this, tree, "Make sure that executing SQL queries is safe here.");
    }
  }
}
