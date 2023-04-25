/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
import org.sonar.php.checks.utils.argumentmatching.ArgumentVerifierValueContainment;
import org.sonar.php.checks.utils.argumentmatching.FunctionArgumentCheck;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S5542")
public class EncryptionModeAndPaddingCheck extends FunctionArgumentCheck {

  private static final String MESSAGE = "Use secure mode and padding scheme.";

  private static final String OPENSSL_PUBLIC_ENCRYPT = "openssl_public_encrypt";
  private static final String OPENSSL_PUBLIC_ENCRYPT_COMPLIANT_VALUE = "OPENSSL_PKCS1_OAEP_PADDING";

  private static final String OPENSSL_ENCRYPT = "openssl_encrypt";
  private static final Set<String> OPENSSL_ENCRYPT_NONCOMPLIANT_VALUES = opensslPublicEncryptNoncompliantValues();

  private static final String MCRYPT_ENCRYPT = "mcrypt_encrypt";
  private static final String MCRYPT_ENCRYPT_NONCOMPLIANT_VALUE = "ecb";

  private static Set<String> opensslPublicEncryptNoncompliantValues() {
    return Set.of(
      "aes-128-ecb",
      "aes-192-ecb",
      "aes-256-ecb",
      "aria-128-ecb",
      "aria-192-ecb",
      "aria-256-ecb",
      "bf-ecb",
      "camellia-128-ecb",
      "camellia-192-ecb",
      "camellia-256-ecb",
      "cast5-ecb",
      "des-ecb",
      "rc2-ecb",
      "seed-ecb",
      "sm4-ecb"
    );
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    // by default OPENSSL_PKCS1_PADDING is used as padding mode argument
    if (!checkArgumentAbsence(tree, OPENSSL_PUBLIC_ENCRYPT, 3)) {
      ArgumentVerifierValueContainment opensslPublicEncryptMatcher = ArgumentVerifierValueContainment.builder()
        .position(3)
        .name("padding")
        .values(OPENSSL_PUBLIC_ENCRYPT_COMPLIANT_VALUE)
        .raiseIssueOnMatch(false)
        .build();
      checkArgument(tree, OPENSSL_PUBLIC_ENCRYPT, opensslPublicEncryptMatcher);
    }
    ArgumentVerifierValueContainment opensslMatcher = ArgumentVerifierValueContainment.builder()
      .position(1)
      .name("method")
      .values(OPENSSL_ENCRYPT_NONCOMPLIANT_VALUES)
      .raiseIssueOnMatch(true)
      .build();
    ArgumentVerifierValueContainment mcryptMatcher = ArgumentVerifierValueContainment.builder()
      .position(3)
      .name("mode")
      .values(MCRYPT_ENCRYPT_NONCOMPLIANT_VALUE)
      .raiseIssueOnMatch(true)
      .build();

    checkArgument(tree, OPENSSL_ENCRYPT, opensslMatcher);
    checkArgument(tree, MCRYPT_ENCRYPT, mcryptMatcher);

    super.visitFunctionCall(tree);
  }

  @Override
  protected void createIssue(ExpressionTree tree) {
    context().newIssue(this, tree, MESSAGE);
  }
}
