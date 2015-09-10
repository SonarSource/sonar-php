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
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.regex.Pattern;

@Rule(
  key = "S2068",
  name = "Credentials should not be hard-coded",
  tags = {Tags.CWE, Tags.OWASP_A2, Tags.SANS_TOP25_POROUS, Tags.SECURITY},
  priority = Priority.CRITICAL)
@ActivatedByDefault
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.CRITICAL)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.SECURITY_FEATURES)
@SqaleConstantRemediation("30min")
public class HardCodedCredentialsCheck extends SquidCheck<LexerlessGrammar> {

  private static final Pattern PASSWORD_LITERAL_PATTERN = Pattern.compile("password=..", Pattern.CASE_INSENSITIVE);
  private static final Pattern PASSWORD_VARIABLE_PATTERN = Pattern.compile("password", Pattern.CASE_INSENSITIVE);

  @Override
  public void init() {
    subscribeTo(PHPGrammar.STRING_LITERAL, PHPGrammar.VARIABLE_DECLARATION, PHPGrammar.ASSIGNMENT_EXPR);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.STRING_LITERAL)) {
      if (PASSWORD_LITERAL_PATTERN.matcher(astNode.getTokenOriginalValue()).find()) {
        addIssue(astNode);
      }
    } else if (astNode.is(PHPGrammar.VARIABLE_DECLARATION)) {
      String identifier = astNode.getFirstChild().getTokenOriginalValue();
      AstNode value = astNode.getLastChild();
      checkVariable(astNode, identifier, value);
    } else {
      String identifier = astNode.getFirstChild().getLastToken().getOriginalValue();
      AstNode value = astNode.getLastChild();
      checkVariable(astNode, identifier, value);
    }
  }

  private void checkVariable(AstNode astNode, String identifier, AstNode value) {
    if (value.hasDescendant(PHPGrammar.STRING_LITERAL) && value.getTokens().size() == 1 && PASSWORD_VARIABLE_PATTERN.matcher(identifier).find()) {
      addIssue(astNode);
    }
  }

  private void addIssue(AstNode astNode) {
    getContext().createLineViolation(this, "Remove this hard-coded password.", astNode);
  }

}
