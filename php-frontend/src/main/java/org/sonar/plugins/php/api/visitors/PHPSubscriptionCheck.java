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
package org.sonar.plugins.php.api.visitors;

import java.util.List;
import org.sonar.php.tree.visitors.PHPCheckContext;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;

public abstract class PHPSubscriptionCheck extends PHPTreeSubscriber implements PHPCheck {

  private CheckContext context;

  @Override
  public abstract List<Kind> nodesToVisit();

  @Override
  public CheckContext context() {
    return context;
  }

  @Override
  public void init() {
    // Default behavior : do nothing.
  }

  @Override
  public final List<PhpIssue> analyze(PhpFile file, CompilationUnitTree tree) {
    return analyze(new PHPCheckContext(file, tree, null));
  }

  @Override
  public List<PhpIssue> analyze(PhpFile file, CompilationUnitTree tree, SymbolTable symbolTable) {
    return analyze(new PHPCheckContext(file, tree, null, symbolTable));
  }

  @Override
  public final List<PhpIssue> analyze(CheckContext context) {
    this.context = context;
    scanTree(context.tree());
    return context().getIssues();
  }

  @Override
  public PreciseIssue newIssue(Tree tree, String message) {
    return context().newIssue(this, tree, message);
  }
}
