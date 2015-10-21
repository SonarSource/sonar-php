/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import com.google.common.io.Files;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.CharsetAwareVisitor;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@Rule(
  key = FileHeaderCheck.KEY,
  name = "Copyright and license headers should be defined",
  priority = Priority.BLOCKER)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.SECURITY_FEATURES)
@SqaleConstantRemediation("5min")
public class FileHeaderCheck extends PHPVisitorCheck implements CharsetAwareVisitor {

  public static final String KEY = "S1451";
  private static final String MESSAGE = "Add or update the header of this file.";

  private static final String DEFAULT_HEADER_FORMAT = "";
  private static final Pattern PHP_OPEN_TAG = Pattern.compile(LexicalConstant.PHP_OPENING_TAG);

  @RuleProperty(
    key = "headerFormat",
    defaultValue = DEFAULT_HEADER_FORMAT,
    type = "TEXT")
  public String headerFormat = DEFAULT_HEADER_FORMAT;

  private Charset charset;
  private String[] expectedLines;

  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void init() {
    expectedLines = headerFormat.split("(?:\r)?\n|\r");
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    List<String> lines;
    try {
      lines = Files.readLines(context().file(), charset);
    } catch (IOException e) {
      throw new IllegalStateException("Check S1451: Can't read the file", e);
    }

    if (!lines.isEmpty() && !matches(expectedLines, lines)) {
      context().newIssue(KEY, MESSAGE);
    }
  }

  private static boolean matches(String[] expectedLines, List<String> lines) {
    boolean result;

    if (PHP_OPEN_TAG.matcher(lines.get(0)).matches()) {
      lines.remove(0);
    }

    if (expectedLines.length <= lines.size()) {
      result = true;

      Iterator<String> it = lines.iterator();
      for (String expectedLine : expectedLines) {
        String line = it.next();
        if (!line.equals(expectedLine)) {
          result = false;
          break;
        }
      }
    } else {
      result = false;
    }

    return result;
  }

}
