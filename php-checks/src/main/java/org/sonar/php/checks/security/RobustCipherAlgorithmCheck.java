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
package org.sonar.php.checks.security;

import com.google.common.collect.ImmutableSet;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionArgumentCheck;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S5547")
public class RobustCipherAlgorithmCheck extends FunctionArgumentCheck {
  private static final String MESSAGE = "Use a strong cipher algorithm";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    checkArgument(tree, "mcrypt_encrypt", new ArgumentVerifier(0, "cipher", ImmutableSet.of(
      "mcrypt_des",
      "mcrypt_des_compat",
      "mcrypt_tripledes",
      "mcrypt_3des",
      "mcrypt_blowfish",
      "mcrypt_rc2",
      "mcrypt_rc4")));

    checkArgument(tree, "openssl_encrypt", new ArgumentVerifier(1, "method", ImmutableSet.of(
      "bf-ecb",
      "des-ede3",
      "des-ofb",
      "rc2-cbc",
      "rc4"
    )));

    super.visitFunctionCall(tree);
  }

  @Override
  protected void createIssue(ExpressionTree tree) {
    context().newIssue(this, tree, MESSAGE);
  }
}
