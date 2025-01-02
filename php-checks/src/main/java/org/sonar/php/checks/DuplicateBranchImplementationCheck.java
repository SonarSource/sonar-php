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
package org.sonar.php.checks;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.AbstractDuplicateBranchImplementationCheck;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.StatementTree;

@Rule(key = DuplicateBranchImplementationCheck.KEY)
public class DuplicateBranchImplementationCheck extends AbstractDuplicateBranchImplementationCheck {

  public static final String KEY = "S1871";
  private static final String MESSAGE = "This %s's code block is the same as the block for the %s on line %s.";

  @Override
  protected void reportAllDuplicateBranches(SyntaxToken keyword) {
    // is handled by S3923 (AllBranchesIdenticalCheck)
  }

  @Override
  protected void reportTwoDuplicateBranches(String branchType, List<StatementTree> originalBranch, List<StatementTree> duplicateBranch) {
    String message = String.format(MESSAGE, branchType, branchType, ((PHPTree) originalBranch.get(0)).getLine());
    context().newIssue(this, duplicateBranch.get(0), getLast(duplicateBranch), message)
      .secondary(originalBranch.get(0), getLast(originalBranch), "Original");
  }

  private static StatementTree getLast(List<StatementTree> statements) {
    return statements.get(statements.size() - 1);
  }
}
