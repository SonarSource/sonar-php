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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.Set;

@Rule(
  key = ClassCouplingCheck.KEY,
  name = "Classes should not be coupled to too many other classes (Single Responsibility Principle)",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("2h")
public class ClassCouplingCheck extends PHPVisitorCheck {

  public static final String KEY = "S1200";
  private static final String MESSAGE = "Split this class into smaller and more specialized ones " +
    "to reduce its dependencies on other classes from %s to the maximum authorized %s or less.";

  public static final int DEFAULT = 20;
  private Set<String> types = Sets.newHashSet();
  private static final Set<String> DOC_TAGS = ImmutableSet.of(
    "@var", "@global", "@staticvar", "@throws", "@param", "@return");

  private static final Set<String> EXCLUDED_TYPES = Sets.newTreeSet(String.CASE_INSENSITIVE_ORDER);

  static {
    EXCLUDED_TYPES.addAll(ImmutableSet.of(
      "INTEGER", "INT", "DOUBLE", "FLOAT",
      "STRING", "ARRAY", "OBJECT", "BOOLEAN",
      "BOOL", "BINARY", "NULL", "MIXED"));
  }

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  public int max = DEFAULT;

  @Override
  public void visitScript(ScriptTree tree) {
    types.clear();
    super.visitScript(tree);
  }

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    retrieveInstantiatedClassName(tree);

    super.visitNewExpression(tree);
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    if (tree.is(Kind.CLASS_DECLARATION)) {
      retrieveCoupledTypes(tree);
    }

    super.visitClassDeclaration(tree);

    if (tree.is(Kind.CLASS_DECLARATION)) {
      int nbType = types.size();

      if (nbType > max) {
        String message = String.format(MESSAGE, nbType, max);
        context().newIssue(KEY, message).tree(tree);
      }
      types.clear();
    }
  }

  private void retrieveCoupledTypes(ClassDeclarationTree classDeclaration) {
    for (ClassMemberTree classMember : classDeclaration.members()) {
      switch (classMember.getKind()) {
        case CLASS_PROPERTY_DECLARATION:
        case CLASS_CONSTANT_PROPERTY_DECLARATION:
          retrieveTypeFromDoc(classMember);
          break;
        case METHOD_DECLARATION:
          retrieveTypeFromDoc(classMember);
          retrieveTypeFromParameter((MethodDeclarationTree)classMember);
          break;
        default:
          break;
      }
    }
  }

  private void retrieveTypeFromParameter(MethodDeclarationTree methodDeclaration) {
    for (ParameterTree parameter : methodDeclaration.parameters().parameters()) {
      Tree type = parameter.type();
      if (type != null && type.is(Kind.NAMESPACE_NAME)) {
        types.add(getTypeName((NamespaceNameTree) type));
      }
    }
  }

  private void retrieveTypeFromDoc(ClassMemberTree varDeclaration) {
    SyntaxToken varDecToken = ((PHPTree) varDeclaration).getFirstToken();

    for (SyntaxTrivia comment : varDecToken.trivias()) {
      for (String line : comment.text().split("[" + LexicalConstant.LINE_TERMINATOR + "]++")) {
        retrieveTypeFromCommentLine(line);
      }
    }
  }

  private void retrieveTypeFromCommentLine(String line) {
    String[] commentLine = line.trim().split("[" + LexicalConstant.WHITESPACE + "]++");

    if (commentLine.length > 2 && DOC_TAGS.contains(commentLine[1])) {
      for (String type : commentLine[2].split("\\|")) {
        type = StringUtils.removeEnd(type, "[]");

        if (!EXCLUDED_TYPES.contains(type)) {
          types.add(type);
        }
      }
    }
  }

  private void retrieveInstantiatedClassName(NewExpressionTree newExpression) {
    ExpressionTree expression = newExpression.expression();

    if (expression.is(Kind.FUNCTION_CALL)) {
      ExpressionTree callee = ((FunctionCallTree) expression).callee();

      if (callee.is(Kind.NAMESPACE_NAME)) {
        types.add(getTypeName((NamespaceNameTree) callee));
      }

    } else if (expression.is(Kind.NAMESPACE_NAME)) {
      types.add(getTypeName((NamespaceNameTree) expression));
    }
  }

  private String getTypeName(NamespaceNameTree namespaceName) {
    String name = namespaceName.fullName();
    String prefix = "namespace\\";
    if (StringUtils.startsWithIgnoreCase(name, prefix)) {
      // fixme (SONARPHP-552): Handle namespaces properly
      name = name.substring(prefix.length() - 1);
    }

    return name;
  }

}
