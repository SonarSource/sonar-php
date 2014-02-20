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
import org.sonar.php.api.PHPPunctuator;

@Rule(
  key = "S1109",
  priority = Priority.MINOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
public class RightCurlyBraceStartsLineCheck extends SquidCheck<Grammar> {

  @Override
  public void init() {
    subscribeTo(PHPPunctuator.RCURLYBRACE);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!isOnSameLineThanLeftCurlyBrace(astNode) && !isFirstOnLine(astNode)) {
      getContext().createLineViolation(this, "Move this closing curly brace to the next line.", astNode);
    }
  }

  private static boolean isFirstOnLine(AstNode rcurly) {
    return rcurly.getPreviousAstNode().getLastToken().getLine() != rcurly.getTokenLine();
  }

  private static boolean isOnSameLineThanLeftCurlyBrace(AstNode rcurly) {
    return rcurly.getParent().getFirstChild(PHPPunctuator.LCURLYBRACE).getTokenLine() == rcurly.getTokenLine();
  }

}
