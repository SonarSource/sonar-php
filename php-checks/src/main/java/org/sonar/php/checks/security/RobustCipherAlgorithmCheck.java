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

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionArgumentCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S5547")
public class RobustCipherAlgorithmCheck extends FunctionArgumentCheck {
  private static final String MESSAGE = "Use a strong cipher algorithm";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    checkArgument(tree, "mcrypt_encrypt", new ArgumentVerifier(0, "cipher", SetUtils.immutableSetOf(
      "mcrypt_des",
      "mcrypt_des_compat",
      "mcrypt_tripledes",
      "mcrypt_3des",
      "mcrypt_blowfish",
      "mcrypt_rc2",
      "mcrypt_rc4")));

    checkArgument(tree, "openssl_encrypt", new ArgumentVerifier(1, "method", SetUtils.immutableSetOf(
      "bf-cbc",
      "bf-cfb",
      "bf-ecb",
      "bf-ofb",
      "des-cbc",
      "des-cfb",
      "des-cfb1",
      "des-cfb8",
      "des-ecb",
      "des-ede",
      "des-ede3",
      "des-ede3-cbc",
      "des-ede3-cfb",
      "des-ede3-cfb1",
      "des-ede3-cfb8",
      "des-ede3-ofb",
      "des-ede-cbc",
      "des-ede-cfb",
      "des-ede-ofb",
      "des-ofb",
      "desx-cbc",
      "rc2-40-cbc",
      "rc2-64-cbc",
      "rc2-cbc",
      "rc2-cfb",
      "rc2-ecb",
      "rc2-ofb",
      "rc4",
      "rc4-40",
      "rc4-hmac-md5"
      )));

    super.visitFunctionCall(tree);
  }

  @Override
  protected void createIssue(ExpressionTree tree) {
    context().newIssue(this, tree, MESSAGE);
  }
}
