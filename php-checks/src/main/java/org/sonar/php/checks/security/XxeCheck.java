/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.php.tree.impl.expression.MemberAccessTreeImpl;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
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
  private static final String PROPAGATED_MESSAGE = "Propagated settings.";

  private static final String OPTIONS = "options";
  private static final Tree.Kind[] ARRAY = {Tree.Kind.ARRAY_INITIALIZER_BRACKET, Tree.Kind.ARRAY_INITIALIZER_FUNCTION};

  @Override
  public void visitFunctionCall(FunctionCallTree call) {
    String functionName = CheckUtils.lowerCaseFunctionName(call);
    ExpressionTree callee = call.callee();
    if (callee.is(Kind.NAMESPACE_NAME) && "simplexml_load_string".equals(functionName)) {
      argument(call, OPTIONS, 2).ifPresent(options -> checkSimpleXmlOption(options.value(), options));
    } else if (callee.is(Kind.OBJECT_MEMBER_ACCESS)) {
      if ("load".equals(functionName) || "loadxml".equals(functionName)) {
        argument(call, OPTIONS, 1).ifPresent(options -> checkSimpleXmlOption(options.value(), options));
      } else if ("setparserproperty".equals(functionName)) {
        checkSetParserProperty(call);
      }
    } else if (callee.is(Kind.CLASS_MEMBER_ACCESS) && "build".equals(functionName) &&
      isNamespaceMemberEqualTo("Cake\\Utility\\Xml", callee)) {
        argument(call, OPTIONS, 1).ifPresent(options -> checkXmlBuildOption(options.value(), options));
      }
    super.visitFunctionCall(call);
  }

  private void checkSimpleXmlOption(ExpressionTree optionValue, Tree treeToReport) {
    if (optionValue.is(Kind.NAMESPACE_NAME) && "LIBXML_NOENT".equals(((NamespaceNameTree) optionValue).unqualifiedName())) {
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

  private void checkXmlBuildOption(ExpressionTree optionValue, Tree optionArgument) {
    if (optionValue.is(Kind.VARIABLE_IDENTIFIER)) {
      CheckUtils.uniqueAssignedValue((VariableIdentifierTree) optionValue)
        .filter(uniqueValue -> !uniqueValue.toString().equals(optionArgument.toString()))
        .ifPresent(assignedValue -> checkXmlBuildOption(assignedValue, optionArgument));
    } else if (optionValue.is(ARRAY)) {
      arrayValue((ArrayInitializerTree) optionValue, "loadEntities")
        .ifPresent(loadEntitiesValue -> raiseIssueIfTrue(loadEntitiesValue, optionArgument));
    }
  }

  private void raiseIssueIfTrue(ExpressionTree value, Tree treeToReport) {
    ExpressionTree assignedValue = CheckUtils.assignedValue(value);
    List<Tree> secondaryTrees = new ArrayList<>();
    while (assignedValue.is(Kind.VARIABLE_IDENTIFIER)) {
      secondaryTrees.add(assignedValue);
      assignedValue = CheckUtils.assignedValue(assignedValue);
    }
    if (!isFalseValue(assignedValue)) {
      PreciseIssue issue = createIssue(treeToReport);
      issue.secondary(assignedValue, SECONDARY_MESSAGE);
      secondaryTrees.forEach(tree -> issue.secondary(tree, PROPAGATED_MESSAGE));
    }
  }

  private PreciseIssue createIssue(Tree tree) {
    return context().newIssue(this, tree, MESSAGE);
  }

  private static Optional<String> namespaceMemberFullQualifiedName(ExpressionTree callee) {
    return Optional.of(callee)
      .filter(MemberAccessTreeImpl.class::isInstance)
      .map(c -> ((MemberAccessTreeImpl) c).object())
      .filter(ClassNamespaceNameTreeImpl.class::isInstance)
      .map(ClassNamespaceNameTreeImpl.class::cast)
      .map(c -> c.symbol().qualifiedName().toString());
  }

  private static boolean isNamespaceMemberEqualTo(String targetNamespace, ExpressionTree callee) {
    return namespaceMemberFullQualifiedName(callee)
      .filter(name -> targetNamespace.equalsIgnoreCase(name) || isNameInNamespaceEqualTo(targetNamespace, callee, name))
      .isPresent();
  }

  private static boolean isNameInNamespaceEqualTo(String targetNamespace, ExpressionTree callee, String name) {
    return Optional.ofNullable((NamespaceStatementTree) TreeUtils.findAncestorWithKind(callee, Collections.singletonList(Kind.NAMESPACE_STATEMENT)))
      .map(NamespaceStatementTree::namespaceName)
      .filter(nsName -> (nsName.fullName() + "\\" + targetNamespace).equalsIgnoreCase(name))
      .isPresent();
  }
}
