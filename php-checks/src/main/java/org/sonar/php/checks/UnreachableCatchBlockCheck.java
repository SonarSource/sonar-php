/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1045")
public class UnreachableCatchBlockCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Catch this exception only once; it is already handled by a previous catch clause.";
  private static final String PARENT_MESSAGE = "A parent exception class is caught here.";
  private static final String SAME_MESSAGE = "The same exception class is caught here.";

  private static final BinaryOperator<NamespaceNameTree> DUPLICATE_RESOLUTION = (a, b) -> a;

  @Override
  public void visitTryStatement(TryStatementTree tree) {
    Map<ClassSymbol, NamespaceNameTree> previouslyCaught = new HashMap<>();

    for (CatchBlockTree catchBlock : tree.catchBlocks()) {

      Map<ClassSymbol, NamespaceNameTree> caughtInThisCatch = catchBlock.exceptionTypes().stream().collect(Collectors.toMap(Symbols::getClass, e -> e, DUPLICATE_RESOLUTION));

      Map<ClassSymbol, Optional<ClassSymbol>> caughtSuperClasses = caughtInThisCatch.keySet().stream()
        .collect(Collectors.toMap(
          e -> e,
          e -> previouslyCaught.keySet().stream()
            .filter(s -> e.isSubTypeOf(s.qualifiedName()).isTrue())
            .findFirst()));

      if (caughtSuperClasses.values().stream().allMatch(Optional::isPresent)) {
        caughtInThisCatch.forEach((symbol, name) -> {
          ClassSymbol superClass = caughtSuperClasses.get(symbol).get();
          NamespaceNameTree redundantCatch = previouslyCaught.get(superClass);
          String secondaryMessage = redundantCatch.qualifiedName().equalsIgnoreCase(symbol.qualifiedName().simpleName()) ? SAME_MESSAGE : PARENT_MESSAGE;
          context().newIssue(this, name, MESSAGE)
            .secondary(previouslyCaught.get(superClass), secondaryMessage);
        });
      }

      previouslyCaught.putAll(caughtInThisCatch);
    }
    super.visitTryStatement(tree);
  }
}
