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
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.squid.checks.SquidCheck;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPGrammar;

@Rule(
  key = "S126",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class ElseIfWithoutElseCheck extends SquidCheck<Grammar> {

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.IF_STATEMENT,
      PHPGrammar.ALTERNATIVE_IF_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (hasElseif(astNode) && !hasElse(astNode)) {
      AstNode lastElseif = getLastElseif(astNode);

      getContext().createLineViolation(this, "Add the missing \"else\" clause.", lastElseif);
    }
  }

  private AstNode getLastElseif(AstNode ifStmt) {
    return ifStmt.getFirstChild(PHPGrammar.ELSEIF_LIST, PHPGrammar.ALTERNATIVE_ELSEIF_LIST).getLastChild();
  }

  private boolean hasElseif(AstNode ifStmt) {
    return ifStmt.hasDirectChildren(PHPGrammar.ELSEIF_LIST, PHPGrammar.ALTERNATIVE_ELSEIF_LIST);
  }

  private boolean hasElse(AstNode ifStmt) {
    return ifStmt.hasDirectChildren(PHPGrammar.ELSE_CLAUSE, PHPGrammar.ALTERNATIVE_ELSE_CLAUSE);
  }
}
