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
import org.sonar.check.RuleProperty;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

@Rule(
  key = "S1142",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class TooManyReturnCheck extends SquidCheck<LexerlessGrammar> {

  private static final int DEFAULT = 3;
  private final Deque<Integer> returnStatementCounter = new ArrayDeque<Integer>();

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  int max = DEFAULT;

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.BLOCK,
      PHPGrammar.RETURN_STATEMENT);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    returnStatementCounter.clear();
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.RETURN_STATEMENT) && isInFunctionBody()) {
      setReturnStatementCounter(getReturnStatementCounter() + 1);
    } else if (isFunctionBlock(astNode)) {
      returnStatementCounter.push(0);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.BLOCK) && isFunctionBlock(astNode)) {
      if (getReturnStatementCounter() > max) {
        getContext().createLineViolation(this, "Reduce the number of returns of this function {0}, down to the maximum allowed {1}.",
          astNode.getParent(), getReturnStatementCounter(), max);
      }
      returnStatementCounter.pop();
    }
  }

  private boolean isInFunctionBody() {
    return !returnStatementCounter.isEmpty();
  }

  private int getReturnStatementCounter() {
    return returnStatementCounter.peek();
  }

  private void setReturnStatementCounter(int value) {
    returnStatementCounter.pop();
    returnStatementCounter.push(value);
  }

  private static boolean isFunctionBlock(AstNode block) {
    return block.getParent().is(
      PHPGrammar.METHOD_BODY,
      PHPGrammar.FUNCTION_DECLARATION,
      PHPGrammar.FUNCTION_EXPRESSION);
  }
}
