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
import org.sonar.php.checks.utils.FunctionUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S2014",
  name = "\"$this\" should not be used in a static context",
  priority = Priority.BLOCKER,
  tags = {PHPRuleTags.BUG})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.BLOCKER)
public class ThisVariableUsageInStaticContextCheck extends SquidCheck<LexerlessGrammar> {

  private boolean inStaticContext = false;

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.METHOD_DECLARATION,
      PHPGrammar.VAR_IDENTIFIER);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.METHOD_DECLARATION)) {
      inStaticContext = FunctionUtils.isStaticMethod(astNode);

    } else if (inStaticContext && isThisVariable(astNode)) {
      getContext().createLineViolation(this, "Remove this use of \"$this\".", astNode);
    }
  }

  private boolean isThisVariable(AstNode varIdentifier) {
    return "$this".equals(varIdentifier.getTokenOriginalValue());
  }

}
