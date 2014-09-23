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
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S1066",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class CollapsibleIfStatementCheck extends SquidCheck<LexerlessGrammar> {

  private static final GrammarRuleKey[] IF_STATEMENTS = {PHPGrammar.IF_STATEMENT, PHPGrammar.ALTERNATIVE_IF_STATEMENT};

  @Override
  public void init() {
    subscribeTo(IF_STATEMENTS);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!hasElseOrElseifClause(astNode)) {
      AstNode singleChild = getSingleStatementChild(astNode);

      if (singleChild != null && isIfStatementWithoutElse(singleChild)) {
        getContext().createLineViolation(this, "Merge this if statement with the enclosing one.", singleChild);
      }
    }
  }

  private static boolean isIfStatementWithoutElse(AstNode ifChild) {
    return ifChild.is(PHPGrammar.STATEMENT) && ifChild.getFirstChild().is(IF_STATEMENTS) && !hasElseOrElseifClause(ifChild.getFirstChild());
  }

  private static AstNode getSingleStatementChild(AstNode ifStatement) {
    AstNode ifInnerStmtList = null;

    if (ifStatement.is(PHPGrammar.ALTERNATIVE_IF_STATEMENT)) {
      ifInnerStmtList = ifStatement.getFirstChild(PHPGrammar.INNER_STATEMENT_LIST);
    } else {
      AstNode ifChild = ifStatement.getFirstChild(PHPGrammar.STATEMENT).getFirstChild();

      if (ifChild.is(PHPGrammar.BLOCK)) {
        ifInnerStmtList = ifChild.getFirstChild(PHPGrammar.INNER_STATEMENT_LIST);

      } else if (ifChild.is(IF_STATEMENTS)) {
        return ifStatement.getFirstChild(PHPGrammar.STATEMENT);
      }
    }
    return ifInnerStmtList != null && ifInnerStmtList.getNumberOfChildren() == 1 ? ifInnerStmtList.getFirstChild() : null;
  }

  private static boolean hasElseOrElseifClause(AstNode ifStatement) {
    return ifStatement.hasDirectChildren(
      PHPGrammar.ELSE_CLAUSE,
      PHPGrammar.ALTERNATIVE_ELSE_CLAUSE,
      PHPGrammar.ELSEIF_LIST,
      PHPGrammar.ALTERNATIVE_ELSEIF_LIST);
  }


}
