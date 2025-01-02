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
package org.sonar.plugins.php.api.visitors;

import java.util.List;
import org.sonar.api.Beta;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;

/**
 * Marker interface for all PHP checks.
 *
 * To implement a check you should extend {@link PHPVisitorCheck} or {@link PHPSubscriptionCheck}.
 */
public interface PHPCheck {

  CheckContext context();

  /**
   * Initialize the check, this method is called once.
   */
  void init();

  /**
   * Terminates the check, doing cleanup and postprocessing after the analysis of all project files if necessary.
   * This method is called once.
   */
  default void terminate() {
  }

  List<PhpIssue> analyze(PhpFile file, CompilationUnitTree tree);

  List<PhpIssue> analyze(PhpFile file, CompilationUnitTree tree, SymbolTable symbolTable);

  List<PhpIssue> analyze(CheckContext context);

  PreciseIssue newIssue(Tree tree, String message);

  @Beta
  default boolean scanWithoutParsing(PhpInputFileContext phpInputFileContext) {
    return true;
  }
}
