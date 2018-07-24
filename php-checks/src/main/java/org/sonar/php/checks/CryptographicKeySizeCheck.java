/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

@Rule(key = CryptographicKeySizeCheck.KEY)
public class CryptographicKeySizeCheck extends PHPVisitorCheck {

  public static final String KEY = "S4426";
  private static final int MIN_KEY_LENGTH = 2048;
  private static final String MESSAGE = "Use a key length of at least " + MIN_KEY_LENGTH + " bits";

  private AssignmentExpressionVisitor assignmentExpressionVisitor;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    this.assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree functionCall) {
    if ("openssl_pkey_new".equals(CheckUtils.getFunctionName(functionCall))) {
      SeparatedList<ExpressionTree> arguments = functionCall.arguments();
      if (arguments.size() == 1) {
        ExpressionTree configArgument = arguments.get(0);
        getKeySize(configArgument)
          .filter(this::lessThanMinKeyLength)
          .ifPresent(keySize -> context().newIssue(this, keySize, MESSAGE));
      }
    }
    super.visitFunctionCall(functionCall);
  }

  private boolean lessThanMinKeyLength(ExpressionTree keySize) {
    if (keySize.is(Kind.NUMERIC_LITERAL)) {
      LiteralTree literal = (LiteralTree) keySize;
      int size = Integer.parseInt(literal.value());
      return size < MIN_KEY_LENGTH;
    } else if (keySize.is(Kind.VARIABLE_IDENTIFIER)) {
      Symbol keySizeSymbol = context().symbolTable().getSymbol(keySize);
      return assignmentExpressionVisitor.getUniqueAssignedValue(keySizeSymbol)
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
    }
    Symbol configSymbol = context().symbolTable().getSymbol(config);
    return assignmentExpressionVisitor
      .getUniqueAssignedValue(configSymbol)
      .flatMap(this::getKeySize);
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
