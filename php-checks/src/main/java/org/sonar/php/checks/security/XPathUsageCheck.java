/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks.security;

import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S4817")
public class XPathUsageCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that executing this XPATH expression is safe.";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();
    if (callee.is(Tree.Kind.OBJECT_MEMBER_ACCESS) && firstArgIsNotHardcoded(tree)) {
      MemberAccessTree memberAccess = (MemberAccessTree) callee;
      if (matchesFunctionCall(memberAccess, "DOMXpath", "query") ||
        matchesFunctionCall(memberAccess, "DOMXpath", "evaluate") ||
        matchesFunctionCall(memberAccess, "SimpleXMLElement", "xpath")) {
        context().newIssue(this, tree, MESSAGE);
      }
    }
    super.visitFunctionCall(tree);
  }

  private static boolean firstArgIsNotHardcoded(FunctionCallTree tree) {
    return !tree.arguments().isEmpty() && !tree.arguments().get(0).is(Tree.Kind.REGULAR_STRING_LITERAL);
  }

  private boolean matchesFunctionCall(MemberAccessTree memberAccess, String type, String name) {
    return isMemberNameMatches(memberAccess, name) && isObjectTypeMatches(memberAccess, type);
  }

  private static boolean isMemberNameMatches(MemberAccessTree memberAccess, String name) {
    Tree member = memberAccess.member();
    return member.is(Tree.Kind.NAME_IDENTIFIER) && ((NameIdentifierTree) member).text().equals(name);
  }

  private boolean isObjectTypeMatches(MemberAccessTree memberAccess, String type) {
    Symbol symbol = context().symbolTable().getSymbol(memberAccess.object());
    if (symbol == null) {
      return false;
    }
    return Stream.concat(Stream.of(symbol.declaration()), symbol.usages().stream().map(SyntaxToken::getParent))
      .map(XPathUsageCheck::parentAssignment)
      .filter(Objects::nonNull)
      .map(AssignmentExpressionTree::value)
      .map(XPathUsageCheck::extractNewType)
      .anyMatch(type::equals);
  }

  @Nullable
  private static AssignmentExpressionTree parentAssignment(Tree tree) {
    if (tree.getParent().is(Tree.Kind.ASSIGNMENT)) {
      return ((AssignmentExpressionTree) tree.getParent());
    }
    return null;
  }

  @Nullable
  private static String extractNewType(Tree tree) {
    if (tree.is(Tree.Kind.NEW_EXPRESSION)) {
      ExpressionTree expression = ((NewExpressionTree) tree).expression();
      if (expression.is(Tree.Kind.FUNCTION_CALL)) {
        ExpressionTree callee = ((FunctionCallTree) expression).callee();
        if (callee.is(Tree.Kind.NAMESPACE_NAME)) {
          return ((NamespaceNameTree) callee).qualifiedName();
        }
      }
    }
    return null;
  }

}
