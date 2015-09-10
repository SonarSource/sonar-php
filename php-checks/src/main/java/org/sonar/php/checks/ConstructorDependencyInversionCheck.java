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
  key = "S2830",
  name = "Class constructors should not create other objects",
  priority = Priority.MAJOR,
  tags = {"design"})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("10min")
public class ConstructorDependencyInversionCheck extends SquidCheck<LexerlessGrammar> {

  public static final String MESSAGE = "Remove this creation of object in constructor. Use dependency injection instead.";
  private boolean inConstructor = false;
  private boolean inThrow = false;

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.METHOD_DECLARATION,
      PHPGrammar.THROW_STATEMENT,
      PHPGrammar.NEW_EXPR);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    inConstructor = false;
    inThrow = false;
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.METHOD_DECLARATION) && isConstructor(astNode)) {
      inConstructor = true;

    } else if (astNode.is(PHPGrammar.THROW_STATEMENT)) {
      inThrow = true;

    } else if (inConstructor && !inThrow) {
      getContext().createLineViolation(this, MESSAGE, astNode);

    }
    // else do nothing
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.METHOD_DECLARATION) && isConstructor(astNode)) {
      inConstructor = false;

    } else if (astNode.is(PHPGrammar.THROW_STATEMENT)) {
      inThrow = false;
    }

  }

  private static boolean isConstructor(AstNode astNode) {
    return "__construct".equals(getMethodName(astNode));
  }

  private static String getMethodName(AstNode method) {
    return method.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue();
  }

}
