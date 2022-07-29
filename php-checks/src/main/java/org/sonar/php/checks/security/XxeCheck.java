/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import static org.sonar.php.checks.utils.CheckUtils.argument;
import static org.sonar.php.checks.utils.CheckUtils.arrayValue;
import static org.sonar.php.checks.utils.CheckUtils.isFalseValue;
import static org.sonar.php.checks.utils.CheckUtils.isTrueValue;
import static org.sonar.php.checks.utils.CheckUtils.nameOf;
import static org.sonar.plugins.php.api.tree.Tree.Kind;

@Rule(key = "S2755")
public class XxeCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Disable access to external entities in XML parsing.";
  private static final String SECONDARY_MESSAGE = "This value enables external entities in XML parsing.";
  private static final Tree.Kind[] ARRAY = {Tree.Kind.ARRAY_INITIALIZER_BRACKET, Tree.Kind.ARRAY_INITIALIZER_FUNCTION};

  @Override
  public void visitFunctionCall(FunctionCallTree call) {
    String functionName = CheckUtils.lowerCaseFunctionName(call);
    ExpressionTree callee = call.callee();
    if (callee.is(Kind.NAMESPACE_NAME) && "simplexml_load_string".equals(functionName)) {
      argument(call, "options", 2).ifPresent(x -> checkSimpleXmlOption(x.value(), x));
    } else if (callee.is(Kind.OBJECT_MEMBER_ACCESS)) {
      if ("load".equals(functionName) || "loadxml".equals(functionName)) {
        argument(call, "options", 1).ifPresent(x -> checkSimpleXmlOption(x.value(), x));
      } else if ("setparserproperty".equals(functionName)) {
        checkSetParserProperty(call);
      }
    } else if (callee.is(Kind.CLASS_MEMBER_ACCESS) && "Xml::build".equals(callee.toString())) {
      argument(call, "values", 1).ifPresent(x -> checkXmlBuildOption(x.value(), x));
    }
    super.visitFunctionCall(call);
  }

  private void checkSimpleXmlOption(ExpressionTree optionValue, Tree treeToReport) {
    if (optionValue.is(Kind.NAMESPACE_NAME) && "LIBXML_NOENT".equals(((NamespaceNameTree) optionValue).fullName())) {
      createIssue(treeToReport);
    } else if (optionValue.is(Kind.BITWISE_OR)) {
      BinaryExpressionTree orExpression = (BinaryExpressionTree) optionValue;
      checkSimpleXmlOption(orExpression.leftOperand(), treeToReport);
      checkSimpleXmlOption(orExpression.rightOperand(), treeToReport);
    } else if (optionValue.is(Kind.PARENTHESISED_EXPRESSION)) {
      checkSimpleXmlOption(((ParenthesisedExpressionTree) optionValue).expression(), treeToReport);
    } else if (optionValue.is(Kind.VARIABLE_IDENTIFIER)) {
      CheckUtils.uniqueAssignedValue((VariableIdentifierTree) optionValue).ifPresent(x -> checkSimpleXmlOption(x, treeToReport));
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

  private void checkXmlBuildOption(ExpressionTree valueTree, Tree primaryTree) {
    if (valueTree.is(Kind.VARIABLE_IDENTIFIER)) {
      CheckUtils.uniqueAssignedValue((VariableIdentifierTree) valueTree).ifPresent(x -> checkXmlBuildOption(x, primaryTree));
    } else if (valueTree.is(ARRAY)) {
      arrayValue((ArrayInitializerTree) valueTree, "loadEntities")
        .ifPresent(x -> raiseIssueIfTrue(x, primaryTree));
    }
  }

  private void raiseIssueIfTrue(ExpressionTree value, Tree treeToReport) {
    ExpressionTree assignedValue = CheckUtils.assignedValue(value);
    List<Tree> secondaryTrees = new ArrayList<>();
    while (assignedValue.is(Kind.VARIABLE_IDENTIFIER)) {
      secondaryTrees.add(assignedValue);
      assignedValue = CheckUtils.assignedValue(assignedValue);
    }
    secondaryTrees.add(assignedValue);
    if (!isNotTrue(assignedValue)) {
      PreciseIssue issue = createIssue(treeToReport);
      if (value != treeToReport) {
        secondaryTrees.forEach(tree -> issue.secondary(tree, SECONDARY_MESSAGE));
      }
    }
  }

  private static boolean isNotTrue(ExpressionTree tree) {
    return tree.is(Tree.Kind.NULL_LITERAL) || isFalseValue(tree);
  }


  private PreciseIssue createIssue(Tree tree) {
    return context().newIssue(this, tree, MESSAGE);
  }

}
