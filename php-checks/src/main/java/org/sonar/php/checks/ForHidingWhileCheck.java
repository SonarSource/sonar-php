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
  key = "S1264",
  priority = Priority.MINOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
public class ForHidingWhileCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(PHPGrammar.FOR_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!hasInitialisation(astNode) && !hasIncrement(astNode)) {
      getContext().createLineViolation(this, "Replace this \"for\" loop with a \"while\" loop.", astNode);
    }
  }

  public static boolean hasInitialisation(AstNode forStmt) {
    return forStmt.getFirstChild(PHPPunctuator.LPARENTHESIS).getNextSibling().is(PHPGrammar.FOR_EXRR);
  }

  public static boolean hasIncrement(AstNode forStmt) {
    return forStmt.getFirstChild(PHPPunctuator.RPARENTHESIS).getPreviousSibling().is(PHPGrammar.FOR_EXRR);
  }
}
