/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

@Rule(key = "S4830")
public class SSLCertificatesVerificationDisabledCheck extends FunctionArgumentCheck {

  private static final String MESSAGE = "Enable server certificate validation on this SSL/TLS connection.";

  private static final String CURL_SETOPT = "curl_setopt";
  private static final String CURLOPT_SSL_VERIFYPEER = "CURLOPT_SSL_VERIFYPEER";
  private static final Set<String> VERIFY_PEER_COMPLIANT_VALUES = Set.of("false", "0");

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ArgumentMatcherValueContainment curlMatcher = ArgumentMatcherValueContainment.builder()
      .position(1)
      .name("option")
      .values(CURLOPT_SSL_VERIFYPEER)
      .build();

    ArgumentVerifierValueContainment verifyPeerMatcher = ArgumentVerifierValueContainment.builder()
      .position(2)
      .name("value")
      .values(VERIFY_PEER_COMPLIANT_VALUES)
      .build();

    checkArgument(tree, CURL_SETOPT, curlMatcher, verifyPeerMatcher);

    super.visitFunctionCall(tree);
  }

  protected void createIssue(ExpressionTree argument) {
    context().newIssue(this, argument, MESSAGE);
  }
}
