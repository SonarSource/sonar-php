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
import org.sonar.php.api.CharsetAwareVisitor;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

@Rule(
  key = NonLFCharAsEOLCheck.KEY,
  name = "Only LF character (Unix-like) should be used to end lines",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION, Tags.PSR2})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class NonLFCharAsEOLCheck extends PHPVisitorCheck implements CharsetAwareVisitor {

  public static final String KEY = "S1779";
  private static final String MESSAGE = "Replace all non line feed end of line characters in this file \"%s\" by LF.";

  private Charset charset;

  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }


  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    File file = context().file();
    try {
      BufferedReader reader = Files.newReader(file, charset);

      int c;
      while ((c = reader.read()) != -1) {

        if (c == '\r' || c == '\u2028' || c == '\u2029') {
          String message = String.format(MESSAGE, file.getName());
          context().newIssue(KEY, message);
          break;
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Check S1779: Can't read the file", e);
    }
  }

}
