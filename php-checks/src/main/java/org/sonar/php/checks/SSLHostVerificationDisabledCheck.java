/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.CurlUsageCheck;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;


@Rule(key = "S5527")
public class SSLHostVerificationDisabledCheck extends CurlUsageCheck {

  private static final String CURLOPT_SSL_VERIFYHOST = "CURLOPT_SSL_VERIFYHOST";
  private static final Set<String> VERIFY_HOST_COMPLIANT_VALUES = ImmutableSet.of("2","true");

  private static final String MESSAGE = "Enable server hostname verification on this SSL/TLS connection.";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = CheckUtils.getLowerCaseFunctionName(tree);
    List<ExpressionTree> arguments = tree.arguments();

    // Detect curl_setopt function usage
    // http://php.net/manual/fr/function.curl-setopt.php
    if (CURL_SETOPT.equals(functionName) && arguments.size() > 2) {
      checkCurlSetop(arguments, CURLOPT_SSL_VERIFYHOST, VERIFY_HOST_COMPLIANT_VALUES);
    }

    // super method must be called in order to visit function call node's children
    super.visitFunctionCall(tree);
  }

  protected void addIssue(ExpressionTree expressionTree) {
    context().newIssue(this, expressionTree, MESSAGE);
  }
}
