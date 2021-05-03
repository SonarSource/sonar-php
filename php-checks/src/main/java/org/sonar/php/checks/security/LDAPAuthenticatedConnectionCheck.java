/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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

import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S4433")
public class LDAPAuthenticatedConnectionCheck extends FunctionUsageCheck {

  private static final String MESSAGE = "Provide username and password to authenticate the connection.";

  @Override
  protected Set<String> functionNames() {
    return SetUtils.immutableSetOf("ldap_bind");
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    if (argumentIsNullOrEmptyString(tree, "bind_rdn", 1) || argumentIsNullOrEmptyString(tree, "bind_password", 2)) {
      context().newIssue(this, tree, MESSAGE);
    }
  }

  private boolean argumentIsNullOrEmptyString(FunctionCallTree tree, String argumentName, int argumentIndex) {
    Optional<CallArgumentTree> argument = CheckUtils.argument(tree, argumentName, argumentIndex);
    if (argument.isPresent()) {
      ExpressionTree argumentValue = CheckUtils.assignedValue(argument.get().value());
      return CheckUtils.isNullOrEmptyString(argumentValue);
    }
    return true;
  }

}
