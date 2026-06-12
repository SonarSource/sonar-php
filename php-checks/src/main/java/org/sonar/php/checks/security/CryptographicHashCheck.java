/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.argumentmatching.ArgumentVerifierValueContainment;
import org.sonar.php.checks.utils.argumentmatching.FunctionArgumentCheck;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S4790")
public class CryptographicHashCheck extends FunctionArgumentCheck {

  private static final String MESSAGE = "Make sure this weak hash algorithm is not used in a sensitive context here.";

  private static final Set<String> WEAK_HASH_FUNCTIONS = Set.of("md5", "sha1");
  private static final Set<String> SUBSTR_FUNCTIONS = Set.of("substr", "mb_substr");
  // Maps each WordPress cache/transient function to its key parameter name, used to resolve
  // the key argument correctly even when PHP 8 named arguments reorder the call.
  private static final Map<String, String> WORDPRESS_CACHE_KEY_PARAMS = Map.of(
    "wp_cache_get", "key",
    "wp_cache_set", "key",
    "wp_cache_add", "key",
    "wp_cache_delete", "key",
    "wp_cache_replace", "key",
    "wp_cache_incr", "key",
    "wp_cache_decr", "key",
    "get_transient", "transient",
    "set_transient", "transient",
    "delete_transient", "transient");
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
      if (!isWrappedInSubstr(tree) && !isUsedAsWordPressCacheKey(tree)) {
        createIssue(tree);
      }
      return;
    }

    checkArgument(tree, "hash_init", hashArgumentVerifier);
    if (!isWrappedInSubstr(tree) && !isUsedAsWordPressCacheKey(tree)) {
      checkArgument(tree, "hash", hashArgumentVerifier);
    }
    checkArgument(tree, "hash_pbkdf2", hashArgumentVerifier);
    checkArgument(tree, "mhash", mHashArgumentVerifier);
  }

  // SONARPHP-1840: Suppress when the hash output is immediately truncated by substr/mb_substr,
  // which signals a non-cryptographic use (cache key, ETag, short identifier, etc.)
  private static boolean isWrappedInSubstr(FunctionCallTree hashCall) {
    Tree parent = hashCall.getParent();
    if (parent == null || !parent.is(Tree.Kind.CALL_ARGUMENT)) {
      return false;
    }
    Tree grandParent = parent.getParent();
    if (grandParent == null || !grandParent.is(Tree.Kind.FUNCTION_CALL)) {
      return false;
    }
    FunctionCallTree outerCall = (FunctionCallTree) grandParent;
    String outerName = CheckUtils.getLowerCaseFunctionName(outerCall);
    if (outerName == null || !SUBSTR_FUNCTIONS.contains(outerName)) {
      return false;
    }
    return !outerCall.callArguments().isEmpty() && outerCall.callArguments().get(0) == parent;
  }

  // SONARPHP-1839: Suppress when the hash output is used as a WordPress cache/transient key,
  // either directly or as part of a concatenated key string (e.g. 'prefix_' . sha1($q)).
  // Cache keys are not a security primitive — a collision yields a stale read, not a security incident.
  private static boolean isUsedAsWordPressCacheKey(FunctionCallTree hashCall) {
    Tree node = hashCall.getParent();
    while (node != null && node.is(Tree.Kind.CONCATENATION)) {
      node = node.getParent();
    }
    Tree callArg = node;
    if (callArg == null || !callArg.is(Tree.Kind.CALL_ARGUMENT)) {
      return false;
    }
    Tree grandParent = callArg.getParent();
    if (grandParent == null || !grandParent.is(Tree.Kind.FUNCTION_CALL)) {
      return false;
    }
    FunctionCallTree outerCall = (FunctionCallTree) grandParent;
    String outerName = CheckUtils.getLowerCaseFunctionName(outerCall);
    String keyParamName = outerName != null ? WORDPRESS_CACHE_KEY_PARAMS.get(outerName) : null;
    if (keyParamName == null) {
      return false;
    }
    return CheckUtils.argument(outerCall, keyParamName, 0)
      .filter(keyArg -> keyArg == callArg)
      .isPresent();
  }

  protected void createIssue(FunctionCallTree tree) {
    context().newIssue(this, tree.callee(), MESSAGE);
  }

  @Override
  protected void createIssue(ExpressionTree argument) {
    context().newIssue(this, argument, MESSAGE);
  }
}
