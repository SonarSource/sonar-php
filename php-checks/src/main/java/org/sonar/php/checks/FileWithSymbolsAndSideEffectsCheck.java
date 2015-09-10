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
import com.sonar.sslr.api.AstNodeType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;

@Rule(
  key = "S2036",
  name = "Files that define symbols should not cause side-effects",
  priority = Priority.CRITICAL,
  tags = {Tags.PSR1, Tags.USER_EXPERIENCE})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.SOFTWARE_RELATED_PORTABILITY)
@SqaleConstantRemediation("5min")
public class FileWithSymbolsAndSideEffectsCheck extends SquidCheck<LexerlessGrammar> {

  private static class File {
    boolean hasSymbol = false;
    boolean hasSideEffects = false;
    boolean inDeclaration = false;
    boolean hasIssue = false;

    public void reset() {
      hasSymbol = false;
      hasSideEffects = false;
      hasIssue = false;
      inDeclaration = false;
    }
  }

  private static AstNodeType[] DECLARATIONS = {
    PHPGrammar.CLASS_DECLARATION,
    PHPGrammar.FUNCTION_DECLARATION,
    PHPGrammar.INTERFACE_DECLARATION
  };

  private static AstNodeType[] SIDE_EFFECTS_STATEMENT = {
    PHPGrammar.YIELD_STATEMENT,
    PHPGrammar.ECHO_STATEMENT,
    PHPGrammar.INLINE_HTML,
    PHPGrammar.UNSET_VARIABLE_STATEMENT,
    PHPGrammar.EXPRESSION_STATEMENT
  };

  @Override
  public void init() {
    subscribeTo(DECLARATIONS);
    subscribeTo(SIDE_EFFECTS_STATEMENT);
  }

  private File currentFile = new File();

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    currentFile.reset();
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(DECLARATIONS)) {
      currentFile.inDeclaration = true;

      if (!currentFile.hasSymbol) {
        currentFile.hasSymbol = true;
      }

      // Do not report "side-effects" statement within a declaration
    } else if (!currentFile.inDeclaration && !currentFile.hasSideEffects && astNode.is(SIDE_EFFECTS_STATEMENT) && !isExcluded(astNode)) {
      currentFile.hasSideEffects = true;
    }

    if (!currentFile.hasIssue && currentFile.hasSymbol && currentFile.hasSideEffects) {
      getContext().createFileViolation(this, "Refactor this file to either declare symbols or cause side effects, but not both.");
      currentFile.hasIssue = true;
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(DECLARATIONS)) {
      currentFile.inDeclaration = false;
    }
  }

  /**
   * Return true if expression is "define" function call, use to declare constant.
   */
  private boolean isExcluded(AstNode astNode) {
    if (astNode.is(PHPGrammar.EXPRESSION_STATEMENT)) {
      AstNode expression = astNode.getFirstChild(PHPGrammar.EXPRESSION).getFirstChild();

      if (expression.is(PHPGrammar.POSTFIX_EXPR)) {
        AstNode child = expression.getFirstChild();

        if (child.is(PHPGrammar.MEMBER_EXPRESSION) && isDefineMethodCall(child)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isDefineMethodCall(AstNode memberExpr) {
    return "define".equals(memberExpr.getFirstChild().getTokenOriginalValue())
      && memberExpr.getLastChild().is(PHPGrammar.FUNCTION_CALL_PARAMETER_LIST);
  }

}
