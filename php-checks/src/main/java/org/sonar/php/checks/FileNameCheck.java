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

import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;

import java.util.regex.Pattern;

@Rule(
  key = "S1578",
  name = "File names should comply with a naming convention",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("10min")
public class FileNameCheck extends SquidCheck<LexerlessGrammar> {

  public static final String DEFAULT = "[a-z][A-Za-z0-9]+.php";
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    defaultValue = DEFAULT)
  String format = DEFAULT;


  @Override
  public void init() {
    pattern = Pattern.compile(format);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    if (!pattern.matcher(getContext().getFile().getName()).matches()) {
      getContext().createFileViolation(this, "Rename this file to match this regular expression: \"{0}\"", format);
    }
  }
}
