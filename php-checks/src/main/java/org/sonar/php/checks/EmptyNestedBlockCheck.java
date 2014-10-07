/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S108",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class EmptyNestedBlockCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.BLOCK,
      PHPGrammar.SWITCH_CASE_LIST,
      PHPGrammar.TRAIT_ADAPTATIONS);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (isNested(astNode) && isEmptyBlock(astNode)) {
      getContext().createLineViolation(this, "Either remove or fill this block of code.", astNode);
    }
  }

  private static boolean isNested(AstNode node) {
    return !node.getParent().is(PHPGrammar.METHOD_BODY, PHPGrammar.FUNCTION_DECLARATION);
  }

  private static boolean isEmptyBlock(AstNode node) {
    AstNode rightCurlyBrace = node.getFirstChild(PHPPunctuator.RCURLYBRACE);
    return rightCurlyBrace != null && rightCurlyBrace.getPreviousAstNode().is(PHPPunctuator.LCURLYBRACE) && !hasComment(rightCurlyBrace);
  }

  private static boolean hasComment(AstNode node) {
    return node.getToken().hasTrivia();
  }
}
