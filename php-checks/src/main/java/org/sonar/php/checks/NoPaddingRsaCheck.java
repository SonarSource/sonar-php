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
package org.sonar.php.checks;

import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = NoPaddingRsaCheck.KEY)
public class NoPaddingRsaCheck extends FunctionUsageCheck {

  public static final String KEY = "S2277";
  private static final String MESSAGE = "Use an RSA algorithm with a OAEP padding: OPENSSL_PKCS1_OAEP_PADDING.";

  private static final int PADDING_ARGUMENT_INDEX = 3;
  private static final String SECURE_PADDING = "OPENSSL_PKCS1_OAEP_PADDING";

  @Override
  protected Set<String> lookedUpFunctionNames() {
    return Set.of("openssl_public_encrypt");
  }

  @Override
  protected void checkFunctionCall(FunctionCallTree tree) {
    Optional<CallArgumentTree> paddingArgument = CheckUtils.argument(tree, "padding", PADDING_ARGUMENT_INDEX);
    if (paddingArgument.isPresent()) {
      ExpressionTree padding = paddingArgument.get().value();
      if (padding.is(Kind.NAMESPACE_NAME) && !((NamespaceNameTree) padding).unqualifiedName().equals(SECURE_PADDING)) {
        context().newIssue(this, padding, MESSAGE);
      }
    } else {
      context().newIssue(this, tree.callee(), MESSAGE);
    }
  }

}
