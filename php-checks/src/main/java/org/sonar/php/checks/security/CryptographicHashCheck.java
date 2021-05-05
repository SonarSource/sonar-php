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

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionArgumentCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S4790")
public class CryptographicHashCheck extends FunctionArgumentCheck {

  private static final String MESSAGE = "Make sure this weak hash algorithm is not used in a sensitive context here.";

  private static final Set<String> WEAK_HASH_FUNCTIONS = SetUtils.immutableSetOf("md5", "sha1");
  private static final Set<String> WEAK_HASH_ARGUMENTS = SetUtils.immutableSetOf(
    "md2",
    "md4",
    "md5",
    "sha1",
    "sha224",
    "ripemd128",
    "ripemd160",
    "haval160,3",
    "haval192,3",
    "haval224,3"
  );
  private static final Set<String> WEAK_MHASH_ARGUMENTS = SetUtils.immutableSetOf(
    "MHASH_MD2",
    "MHASH_MD4",
    "MHASH_MD5",
    "MHASH_RIPEMD128",
    "MHASH_SHA1",
    "MHASH_SHA192",
    "MHASH_SHA224",
    "MHASH_HAVAL128",
    "MHASH_HAVAL160",
    "MHASH_HAVAL192",
    "MHASH_HAVAL224"
  );

  private static final ArgumentVerifier hashArgumentVerifier = new ArgumentVerifier(0, "algo", WEAK_HASH_ARGUMENTS);
  private static final ArgumentVerifier mHashArgumentVerifier = new ArgumentVerifier(0, "hash", WEAK_MHASH_ARGUMENTS);

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    super.visitFunctionCall(tree);

    String functionName = CheckUtils.getLowerCaseFunctionName(tree);
    if (WEAK_HASH_FUNCTIONS.contains(functionName)) {
      createIssue(tree);
      return;
    }

    checkArgument(tree, "hash_init", hashArgumentVerifier);
    checkArgument(tree, "hash", hashArgumentVerifier);
    checkArgument(tree, "hash_pbkdf2", hashArgumentVerifier);
    checkArgument(tree, "mhash", mHashArgumentVerifier);
  }

  protected void createIssue(FunctionCallTree tree) {
    context().newIssue(this, tree.callee(), MESSAGE);
  }

  @Override
  protected void createIssue(ExpressionTree argument) {
    context().newIssue(this, argument, MESSAGE);
  }
}
