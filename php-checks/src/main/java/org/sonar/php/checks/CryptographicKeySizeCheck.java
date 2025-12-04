/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.utils.LiteralUtils;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

@Rule(key = CryptographicKeySizeCheck.KEY)
public class CryptographicKeySizeCheck extends PHPVisitorCheck {

  public static final String KEY = "S4426";
  private static final int MIN_KEY_LENGTH = 2048;
  private static final String MESSAGE = "Use a key length of at least " + MIN_KEY_LENGTH + " bits";

  @Override
  public void visitFunctionCall(FunctionCallTree functionCall) {
    if ("openssl_pkey_new".equals(CheckUtils.lowerCaseFunctionName(functionCall))) {
      CheckUtils.argumentValue(functionCall, "options", 0)
        .flatMap(this::getKeySize)
        .filter(this::lessThanMinKeyLength)
        .ifPresent(keySize -> context().newIssue(this, keySize, MESSAGE));
    }
    super.visitFunctionCall(functionCall);
  }

  private boolean lessThanMinKeyLength(ExpressionTree keySize) {
    if (keySize.is(Kind.NUMERIC_LITERAL)) {
      LiteralTree literal = (LiteralTree) keySize;
      long size = LiteralUtils.longLiteralValue(literal.value());
      return size < MIN_KEY_LENGTH;
    } else if (keySize.is(Kind.VARIABLE_IDENTIFIER)) {
      return CheckUtils.uniqueAssignedValue((VariableIdentifierTree) keySize)
        .map(this::lessThanMinKeyLength)
        .orElse(false);
    }
    return false;
  }

  private Optional<ExpressionTree> getKeySize(ExpressionTree config) {
    if (config.is(Kind.ARRAY_INITIALIZER_FUNCTION, Kind.ARRAY_INITIALIZER_BRACKET) && isRSA((ArrayInitializerTree) config)) {
      return ((ArrayInitializerTree) config).arrayPairs().stream()
        .filter(pair -> hasKey(pair, "private_key_bits"))
        .map(ArrayPairTree::value)
        .findFirst();
    } else if (config.is(Kind.VARIABLE_IDENTIFIER)) {
      return CheckUtils.uniqueAssignedValue((VariableIdentifierTree) config).flatMap(this::getKeySize);
    }
    return Optional.empty();
  }

  private static boolean hasKey(ArrayPairTree pair, String keyName) {
    return pair.key() != null && pair.key().is(Kind.REGULAR_STRING_LITERAL) && keyName.equals(trimQuotes((LiteralTree) pair.key()));
  }

  private static boolean isRSA(ArrayInitializerTree config) {
    return config.arrayPairs().stream().anyMatch(pair -> {
      if (!hasKey(pair, "private_key_type")) {
        return false;
      }
      if (pair.value().is(Kind.NAMESPACE_NAME)) {
        NamespaceNameTree value = (NamespaceNameTree) pair.value();
        return "OPENSSL_KEYTYPE_RSA".equals(value.name().text());
      }
      return false;
    });
  }

}
