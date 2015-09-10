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
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.regex.Pattern;

@Rule(
  key = "S114",
  name = "Interface names should comply with a naming convention",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("10min")
public class InterfaceNameCheck extends SquidCheck<LexerlessGrammar> {

  public static final String DEFAULT_FORMAT = "^[A-Z][a-zA-Z0-9]*$";

  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    description = "Regular expression used to check the interface names against.",
    defaultValue = DEFAULT_FORMAT)
  public String format = DEFAULT_FORMAT;

  @Override
  public void init() {
    pattern = Pattern.compile(format);
    subscribeTo(PHPGrammar.INTERFACE_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    String name = astNode.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue();
    if (!pattern.matcher(name).matches()) {
      getContext().createLineViolation(this, "Rename this interface name to match the regular expression {0}.", astNode, format);
    }
  }

}
