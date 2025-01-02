/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.argumentmatching.ArgumentMatcherValueContainment;
import org.sonar.php.checks.utils.argumentmatching.ArgumentVerifierValueContainment;
import org.sonar.php.checks.utils.argumentmatching.FunctionArgumentCheck;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S5527")
public class SSLHostVerificationDisabledCheck extends FunctionArgumentCheck {

  private static final String MESSAGE = "Enable server hostname verification on this SSL/TLS connection.";

  private static final String CURL_SETOPT = "curl_setopt";
  private static final String CURLOPT_SSL_VERIFYHOST = "CURLOPT_SSL_VERIFYHOST";
  private static final Set<String> VERIFY_HOST_COMPLIANT_VALUES = Set.of("1", "2", "TRUE");

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ArgumentMatcherValueContainment curlMatcher = ArgumentMatcherValueContainment.builder()
      .position(1)
      .name("option")
      .values(CURLOPT_SSL_VERIFYHOST)
      .build();

    ArgumentVerifierValueContainment verifyHostMatcher = ArgumentVerifierValueContainment.builder()
      .position(2)
      .name("value")
      .values(VERIFY_HOST_COMPLIANT_VALUES)
      .raiseIssueOnMatch(false)
      .build();

    checkArgument(tree, CURL_SETOPT, curlMatcher, verifyHostMatcher);

    super.visitFunctionCall(tree);
  }

  protected void createIssue(ExpressionTree expressionTree) {
    context().newIssue(this, expressionTree, MESSAGE);
  }
}
