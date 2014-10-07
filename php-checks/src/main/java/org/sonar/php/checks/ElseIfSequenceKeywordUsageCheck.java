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
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S1793",
  priority = Priority.MINOR)
public class ElseIfSequenceKeywordUsageCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.ELSE_CLAUSE);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (isElseIf(astNode)) {
      getContext().createLineViolation(this, "Replace this \"else if\" keyword sequence by \"elseif\" keyword.", astNode);
    }
  }

  private static boolean isElseIf(AstNode elseClause) {
    return elseClause.getFirstChild(PHPGrammar.STATEMENT).getFirstChild().is(PHPGrammar.IF_STATEMENT);
  }
}
