/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.argument;
import static org.sonar.php.checks.utils.CheckUtils.isTrueValue;
import static org.sonar.php.checks.utils.CheckUtils.nameOf;
import static org.sonar.plugins.php.api.tree.Tree.Kind;

@Rule(key = "S2755")
public class XxeCheck extends PHPVisitorCheck {

  private AssignmentExpressionVisitor assignmentExpressionVisitor;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree call) {
    String functionName = CheckUtils.functionName(call);
    ExpressionTree callee = call.callee();
    if (callee.is(Kind.NAMESPACE_NAME) && "simplexml_load_string".equals(functionName)) {
      argument(call, "options", 2).ifPresent(x -> checkSimpleXmlOption(x.value()));
    } else if (callee.is(Kind.OBJECT_MEMBER_ACCESS)) {
      if ("load".equals(functionName) || "loadXML".equals(functionName)) {
        argument(call, "options", 1).ifPresent(x -> checkSimpleXmlOption(x.value()));
      } else if ("setParserProperty".equals(functionName)) {
        checkSetParserProperty(call);
      }
    }
    super.visitFunctionCall(call);
  }

  private void checkSimpleXmlOption(ExpressionTree optionValue) {
    if (optionValue.is(Kind.NAMESPACE_NAME) && "LIBXML_NOENT".equals(((NamespaceNameTree) optionValue).fullName())) {
      createIssue(optionValue);
    } else if (optionValue.is(Kind.BITWISE_OR)) {
      BinaryExpressionTree orExpression = (BinaryExpressionTree) optionValue;
      checkSimpleXmlOption(orExpression.leftOperand());
      checkSimpleXmlOption(orExpression.rightOperand());
    } else if (optionValue.is(Kind.PARENTHESISED_EXPRESSION)) {
      checkSimpleXmlOption(((ParenthesisedExpressionTree) optionValue).expression());
    } else if (optionValue.is(Kind.VARIABLE_IDENTIFIER)) {
      Symbol valueSymbol = context().symbolTable().getSymbol(optionValue);
      assignmentExpressionVisitor.getUniqueAssignedValue(valueSymbol).ifPresent(this::checkSimpleXmlOption);
    }
  }

  private void checkSetParserProperty(FunctionCallTree call) {
    Optional<CallArgumentTree> property = argument(call, "property", 0);
    if (property.isPresent() && "XMLReader::SUBST_ENTITIES".equalsIgnoreCase(nameOf(property.get().value()))) {
      Optional<CallArgumentTree> value = argument(call, "value", 1);
      if (value.isPresent() && isTrueValue(value.get().value())) {
        createIssue(call);
      }
    }
  }

  private void createIssue(Tree tree) {
    context().newIssue(this, tree, "Disable access to external entities in XML parsing.");
  }

}
