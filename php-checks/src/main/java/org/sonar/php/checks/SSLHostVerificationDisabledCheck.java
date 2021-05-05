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
package org.sonar.php.checks;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionArgumentCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S5527")
public class SSLHostVerificationDisabledCheck extends FunctionArgumentCheck {

  private static final String MESSAGE = "Enable server hostname verification on this SSL/TLS connection.";

  private static final String CURL_SETOPT = "curl_setopt";
  private static final String CURLOPT_SSL_VERIFYHOST = "CURLOPT_SSL_VERIFYHOST";
  private static final Set<String> VERIFY_HOST_COMPLIANT_VALUES = SetUtils.immutableSetOf("1", "2", "TRUE");

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    checkArgument(tree, CURL_SETOPT, new ArgumentMatcher(1, "option", CURLOPT_SSL_VERIFYHOST), new ArgumentVerifier(2, "value", VERIFY_HOST_COMPLIANT_VALUES, false));

    super.visitFunctionCall(tree);
  }

  protected void createIssue(ExpressionTree expressionTree) {
    context().newIssue(this, expressionTree, MESSAGE);
  }
}
