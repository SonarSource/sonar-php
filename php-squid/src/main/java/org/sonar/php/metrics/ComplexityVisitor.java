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
package org.sonar.php.metrics;

import com.sonar.sslr.api.AstNode;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonar.php.api.PHPMetric;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.sslr.parser.LexerlessGrammar;

public class ComplexityVisitor extends SquidAstVisitor<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(
      // Entry points
      PHPGrammar.FUNCTION_DECLARATION,
      PHPGrammar.METHOD_DECLARATION,
      PHPGrammar.FUNCTION_EXPRESSION,

      // Branching nodes
      PHPGrammar.IF_STATEMENT,
      PHPGrammar.ALTERNATIVE_IF_STATEMENT,
      PHPGrammar.FOR_STATEMENT,
      PHPGrammar.FOREACH_STATEMENT,
      PHPGrammar.WHILE_STATEMENT,
      PHPGrammar.DO_WHILE_STATEMENT,
      PHPGrammar.CASE_CLAUSE,
      PHPGrammar.CATCH_STATEMENT,
      PHPGrammar.RETURN_STATEMENT,
      PHPGrammar.THROW_STATEMENT,
      PHPGrammar.GOTO_STATEMENT,

      // Expressions
      PHPPunctuator.QUERY,
      PHPGrammar.LOGICAL_AND_OPERATOR,
      PHPGrammar.LOGICAL_OR_OPERATOR);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.RETURN_STATEMENT) && isLastReturn(astNode)) {
      return;
    }

    getContext().peekSourceCode().add(PHPMetric.COMPLEXITY, 1);
  }

  private static boolean isLastReturn(AstNode returnNode) {
    AstNode nextNode = returnNode.getNextAstNode();
    return nextNode.is(PHPPunctuator.RCURLYBRACE) && isClosingFunction(nextNode);
  }

  private static boolean isClosingFunction(AstNode rCurlyBrace) {
    return rCurlyBrace.getParent().is(PHPGrammar.BLOCK)
      && rCurlyBrace.getParent().getParent().is(PHPGrammar.FUNCTION_DECLARATION, PHPGrammar.METHOD_BODY);
  }


}
