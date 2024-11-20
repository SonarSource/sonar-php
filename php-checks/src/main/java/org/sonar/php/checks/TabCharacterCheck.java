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

import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = TabCharacterCheck.KEY)
public class TabCharacterCheck extends PHPVisitorCheck {

  public static final String KEY = "S105";
  private static final String MESSAGE = "Replace all tab characters in this file by sequences of white-spaces.";

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    Stream<String> lines = CheckUtils.lines(context().getPhpFile());

    if (lines.anyMatch(line -> line.contains("\t"))) {
      context().newFileIssue(this, MESSAGE);
    }
  }
}
