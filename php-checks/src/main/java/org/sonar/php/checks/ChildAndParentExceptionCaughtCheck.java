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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.check.Rule;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = "S5713")
public class ChildAndParentExceptionCaughtCheck extends PHPVisitorCheck {

  private static final String MESSAGE_DERIVATIVE = "Remove this useless Exception class; it derives from class %s which is already caught.";
  private static final String MESSAGE_DUPLICATE = "Remove this duplicate Exception class.";
  private static final String SECONDARY_MESSAGE_DERIVATIVE = "Parent class.";
  private static final String SECONDARY_MESSAGE_DUPLICATE = "Duplicate.";

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    if (tree.exceptionTypes().size() > 1) {
      Map<ClassSymbol, List<NamespaceNameTree>> caughtExceptionsBySymbol = new HashMap<>();
      tree.exceptionTypes().forEach(e -> addException(e, caughtExceptionsBySymbol));
      checkCaughtExceptions(caughtExceptionsBySymbol);
    }

    super.visitCatchBlock(tree);
  }

  private void checkCaughtExceptions(Map<ClassSymbol, List<NamespaceNameTree>> caughtExceptionsBySymbol) {
    caughtExceptionsBySymbol.forEach((currentSymbol, caughtExceptionsWithSameSymbol) -> {
      NamespaceNameTree currentException = caughtExceptionsWithSameSymbol.get(0);

      if (caughtExceptionsWithSameSymbol.size() > 1) {
        PreciseIssue issue = context().newIssue(this, currentException, MESSAGE_DUPLICATE);
        caughtExceptionsWithSameSymbol.stream().skip(1).forEach(e -> issue.secondary(e, SECONDARY_MESSAGE_DUPLICATE));
      }

      PreciseIssue issue = null;
      for (Map.Entry<ClassSymbol, List<NamespaceNameTree>> otherException : caughtExceptionsBySymbol.entrySet()) {
        ClassSymbol comparedSymbol = otherException.getKey();
        if (currentSymbol != comparedSymbol && currentSymbol.isSubTypeOf(comparedSymbol.qualifiedName()).isTrue()) {
          if (issue == null) {
            issue = context().newIssue(this, currentException, String.format(MESSAGE_DERIVATIVE, comparedSymbol.qualifiedName().toString()));
          }
          addSecondaryLocations(issue, otherException.getValue());
        }
      }
    });
  }

  private static void addException(NamespaceNameTree exception, Map<ClassSymbol, List<NamespaceNameTree>> caughtExceptionsByFQN) {
    ClassSymbol classSymbol = Symbols.getClass(exception);
    caughtExceptionsByFQN.computeIfAbsent(classSymbol, k -> new ArrayList<>()).add(exception);
  }

  private static void addSecondaryLocations(PreciseIssue issue, List<NamespaceNameTree> others) {
    for (NamespaceNameTree other : others) {
      issue.secondary(other, SECONDARY_MESSAGE_DERIVATIVE);
    }
  }
}
