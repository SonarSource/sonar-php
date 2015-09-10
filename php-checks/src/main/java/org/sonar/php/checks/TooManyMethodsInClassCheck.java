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
import com.sonar.sslr.api.TokenType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S1448",
  name = "Classes should not have too many methods",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("1h")
public class TooManyMethodsInClassCheck extends SquidCheck<LexerlessGrammar> {

  private static final int DEFAULT_THRESHOLD = 20;
  private static final boolean DEFAULT_NON_PUBLIC = true;

  @RuleProperty(
    key = "maximumMethodThreshold",
    defaultValue = "" + DEFAULT_THRESHOLD)
  public int maximumMethodThreshold = DEFAULT_THRESHOLD;

  @RuleProperty(
    key = "countNonpublicMethods",
    type = "BOOLEAN",
    defaultValue = "" + DEFAULT_NON_PUBLIC)
  public boolean countNonpublicMethods = DEFAULT_NON_PUBLIC;

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.CLASS_DECLARATION,
      PHPGrammar.INTERFACE_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    int nbMethod = getNumberOfMethods(astNode);

    if (nbMethod > maximumMethodThreshold) {
      getContext().createLineViolation(this, "Class \"{0}\" has {1} methods, which is greater than {2} authorized. Split it into smaller classes.",
        astNode, astNode.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue(), nbMethod, maximumMethodThreshold);
    }
  }

  public int getNumberOfMethods(AstNode classNode) {
    int nbMethod = 0;

    for (AstNode classStmt : classNode.getChildren(PHPGrammar.CLASS_STATEMENT)) {
      AstNode classMember = classStmt.getFirstChild();
      if (classMember.is(PHPGrammar.METHOD_DECLARATION) && !isExcluded(classMember)) {
        nbMethod++;

      }
    }
    return nbMethod;
  }

  /**
   * Return true if method is private or protected.
   */
  private boolean isExcluded(AstNode methodDec) {
    if (!countNonpublicMethods) {

      for (AstNode modifier : methodDec.getChildren(PHPGrammar.MEMBER_MODIFIER)) {
        TokenType modifierType = modifier.getFirstChild().getToken().getType();
        if (PHPKeyword.PROTECTED.equals(modifierType) || PHPKeyword.PRIVATE.equals(modifierType)) {
          return true;
        }
      }
    }
    return false;
  }

}
