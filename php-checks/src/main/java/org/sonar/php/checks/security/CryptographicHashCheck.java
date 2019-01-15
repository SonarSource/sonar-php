/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S4790")
public class CryptographicHashCheck extends FunctionUsageCheck {

  private static final String MESSAGE = "Make sure that hashing data is safe here.";

  private static final ImmutableSet<String> FUNCTION_NAMES = ImmutableSet.of(
    "hash",
    "hash_init",
    "crypt",
    "password_hash",
    "hash_pbkdf2",
    "openssl_pbkdf2",
    "md5",
    "sha1");

  @Override
  protected ImmutableSet<String> functionNames() {
    return FUNCTION_NAMES;
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    if (!isHashInitHMAC(tree)) {
      context().newIssue(this, tree, MESSAGE);
    }
  }

  private static boolean isHashInitHMAC(FunctionCallTree tree) {
    String qualifiedName = ((NamespaceNameTree) tree.callee()).qualifiedName();
    return qualifiedName.equalsIgnoreCase("hash_init") &&
      tree.arguments().size() >= 2 &&
      isHMAC(tree.arguments().get(1));
  }

  private static boolean isHMAC(ExpressionTree option) {
    return option.getKind() == Tree.Kind.NAMESPACE_NAME &&
      ((NamespaceNameTree)option).qualifiedName().equals("HASH_HMAC");
  }

}
