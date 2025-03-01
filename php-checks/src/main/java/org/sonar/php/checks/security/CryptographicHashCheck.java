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
package org.sonar.php.checks.security;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.argumentmatching.ArgumentVerifierValueContainment;
import org.sonar.php.checks.utils.argumentmatching.FunctionArgumentCheck;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S4790")
public class CryptographicHashCheck extends FunctionArgumentCheck {

  private static final String MESSAGE = "Make sure this weak hash algorithm is not used in a sensitive context here.";

  private static final Set<String> WEAK_HASH_FUNCTIONS = Set.of("md5", "sha1");
  private static final Set<String> WEAK_HASH_ARGUMENTS = Set.of(
    "md2",
    "md4",
    "md5",
    "sha1",
    "sha224",
    "ripemd128",
    "ripemd160",
    "haval160,3",
    "haval192,3",
    "haval224,3");
  private static final Set<String> WEAK_MHASH_ARGUMENTS = Set.of(
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
    "MHASH_HAVAL224");

  private static final ArgumentVerifierValueContainment hashArgumentVerifier = ArgumentVerifierValueContainment.builder()
    .position(0)
    .name("algo")
    .values(WEAK_HASH_ARGUMENTS)
    .build();
  private static final ArgumentVerifierValueContainment mHashArgumentVerifier = ArgumentVerifierValueContainment.builder()
    .position(0)
    .name("hash")
    .values(WEAK_MHASH_ARGUMENTS)
    .build();

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    super.visitFunctionCall(tree);

    String functionName = CheckUtils.getLowerCaseFunctionName(tree);
    if (functionName != null && WEAK_HASH_FUNCTIONS.contains(functionName)) {
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
