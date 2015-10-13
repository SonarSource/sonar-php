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
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@Rule(
  key = LineLengthCheck.KEY,
  name = "Lines should not be too long",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("1min")
public class LineLengthCheck extends PHPVisitorCheck implements CharsetAwareVisitor {

  public static final String KEY = "S103";
  private static final String MESSAGE = "Split this %s characters long line (which is greater than %s authorized).";

  public static final int DEFAULT = 120;
  private Charset charset;

  @RuleProperty(
    key = "maximumLineLength",
    defaultValue = "" + DEFAULT)
  public int maximumLineLength = DEFAULT;

  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    List<String> lines;

    try {
      lines = Files.readLines(context().file(), charset);
    } catch (IOException e) {
      throw new IllegalStateException("Check S103: Can't read the file", e);
    }

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (line.length() > maximumLineLength) {
        String message = String.format(MESSAGE, line.length(), maximumLineLength);
        context().newIssue(KEY, message).line(i + 1);
      }
    }
  }

}
