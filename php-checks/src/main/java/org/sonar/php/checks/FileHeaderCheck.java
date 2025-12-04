/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.Iterator;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = FileHeaderCheck.KEY)
public class FileHeaderCheck extends PHPVisitorCheck {

  public static final String KEY = "S1451";
  private static final String MESSAGE = "Add or update the header of this file.";

  private static final String DEFAULT_HEADER_FORMAT = "";
  private static final Pattern PHP_OPEN_TAG = Pattern.compile(LexicalConstant.PHP_OPENING_TAG);

  @RuleProperty(
    key = "headerFormat",
    defaultValue = DEFAULT_HEADER_FORMAT,
    type = "TEXT")
  public String headerFormat = DEFAULT_HEADER_FORMAT;

  private String[] expectedLines;

  @Override
  public void init() {
    expectedLines = headerFormat.split("(?:\r)?\n|\r");
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    Iterator<String> it = CheckUtils.lines(context().getPhpFile()).iterator();

    if (it.hasNext() && !matches(expectedLines, it)) {
      context().newFileIssue(this, MESSAGE);
    }
  }

  private static boolean matches(String[] expectedLines, Iterator<String> lines) {
    String line = lines.next();

    if (PHP_OPEN_TAG.matcher(line).matches()) {
      if (lines.hasNext()) {
        line = lines.next();
      } else {
        // actually empty file, no issue is raised
        return true;
      }
    }

    for (int i = 0; i < expectedLines.length; i++) {
      if (i > 0) {
        if (lines.hasNext()) {
          line = lines.next();
        } else {
          return false;
        }
      }
      String expectedLine = expectedLines[i];
      if (!line.equals(expectedLine)) {
        return false;
      }
    }

    return true;
  }

}
