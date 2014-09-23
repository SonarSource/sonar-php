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
import com.sonar.sslr.api.GenericTokenType;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S1603",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class ConstructorDeclarationCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(PHPGrammar.CLASS_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode constructor = getDeprecatedConstructor(astNode);

    if (constructor != null) {
      getContext().createLineViolation(this, "Replace this function name \"{0}\" with \"__construct\".", constructor, getMethodName(constructor));
    }
  }

  private static AstNode getDeprecatedConstructor(AstNode astNode) {
    String className = astNode.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue();

    for (AstNode classStmt : astNode.getChildren(PHPGrammar.CLASS_STATEMENT)) {
      AstNode stmt = classStmt.getFirstChild();

      if (stmt.is(PHPGrammar.METHOD_DECLARATION) && className.equals(getMethodName(stmt))) {
        return stmt;
      }
    }
    return null;
  }

  private static String getMethodName(AstNode method) {
    return method.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue();
  }
}
