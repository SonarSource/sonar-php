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

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S5042")
public class ZipEntryCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that expanding this archive file is safe here.";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = CheckUtils.functionName(tree);
    if (tree.callee().is(Tree.Kind.OBJECT_MEMBER_ACCESS) && "extractTo".equalsIgnoreCase(functionName)) {
      checkZipArchive(tree);
    } else if ("zip_entry_read".equalsIgnoreCase(functionName)) {
      checkZipEntryRead(tree);
    }

    super.visitFunctionCall(tree);
  }

  private void checkZipEntryRead(FunctionCallTree tree) {
    CheckUtils.argument(tree, "length", 1)
      .map(CallArgumentTree::value)
      .filter(c -> c.is(Tree.Kind.FUNCTION_CALL))
      .map(FunctionCallTree.class::cast)
      .map(CheckUtils::functionName)
      .filter(c -> c.equalsIgnoreCase("zip_entry_filesize"))
      .ifPresent(c -> context().newIssue(this, tree, MESSAGE));
  }

  private void checkZipArchive(FunctionCallTree tree) {
    CheckUtils.resolveReceiver((MemberAccessTree) tree.callee())
      .filter("ZipArchive"::equalsIgnoreCase)
      .ifPresent(c -> context().newIssue(this, tree, MESSAGE));
  }
}
