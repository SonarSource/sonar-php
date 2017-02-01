/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.compat.CompatibleInputFile;
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
    CompatibleInputFile file = context().file();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.inputStream(), file.charset()))) {
      Iterator<String> it = reader.lines().iterator();
      if (it.hasNext() && !matches(expectedLines, it)) {
        context().newFileIssue(this, MESSAGE);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Check S1451: Can't read the file", e);
    }
  }

  private static boolean matches(String[] expectedLines, Iterator<String> lines) {
    String line = lines.next();

    if (PHP_OPEN_TAG.matcher(line).matches()) {
      line = lines.next();
    }

    for (String expectedLine : expectedLines) {
      if (!line.equals(expectedLine)) {
        return false;
      }
      line = lines.next();
    }

    return true;
  }

}
