/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.DeclaredTypeTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = "S1200")
public class ClassCouplingCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Split this class into smaller and more specialized ones " +
    "to reduce its dependencies on other classes from %s to the maximum authorized %s or less.";
  private static final String SECONDARY_MESSAGE = "Dependency on %s.";

  public static final int DEFAULT = 20;
  private final Deque<Map<String, Tree>> types = new ArrayDeque<>();
  private static final Set<String> DOC_TAGS = Set.of(
    "@var", "@global", "@staticvar", "@throws", "@param", "@return");

  private static final Set<String> EXCLUDED_TYPES = Set.of(
    "integer", "int", "double", "float",
    "string", "array", "object", "boolean",
    "bool", "binary", "null", "mixed");

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  public int max = DEFAULT;

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    retrieveInstantiatedClassName(tree);

    super.visitNewExpression(tree);
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    if (tree.is(Kind.CLASS_DECLARATION)) {
      enterClass(tree);
    }

    super.visitClassDeclaration(tree);

    if (tree.is(Kind.CLASS_DECLARATION)) {
      leaveClass(tree);
    }
  }

  private void leaveClass(ClassTree tree) {
    Map<String, Tree> coupledTrees = types.removeLast();
    int numberOfTypes = coupledTrees.size();

    if (numberOfTypes > max) {
      String message = String.format(MESSAGE, numberOfTypes, max);
      PreciseIssue preciseIssue = context().newIssue(this, tree.classToken(), message);

      raiseSecondaryLocations(preciseIssue, coupledTrees);
    }
  }

  private static void raiseSecondaryLocations(PreciseIssue preciseIssue, Map<String, Tree> coupledTrees) {
    Map<Tree, String> treeToNames = new HashMap<>();

    // calculate which tree's aggregate multiple dependencies
    for (Map.Entry<String, Tree> nameAndTree : coupledTrees.entrySet()) {
      treeToNames.compute(nameAndTree.getValue(),
        (k, oldValue) -> oldValue == null ? nameAndTree.getKey() : oldValue.concat(", " + nameAndTree.getKey()));
    }

    for (Map.Entry<Tree, String> treeAndNames : treeToNames.entrySet()) {
      preciseIssue.secondary(treeAndNames.getKey(), String.format(SECONDARY_MESSAGE, treeAndNames.getValue()));
    }
  }

  private void enterClass(ClassTree tree) {
    types.addLast(new HashMap<>());
    retrieveCoupledTypes(tree);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    enterClass(tree);

    super.visitAnonymousClass(tree);

    leaveClass(tree);
  }

  private void retrieveCoupledTypes(ClassTree classTree) {
    for (ClassMemberTree classMember : classTree.members()) {
      switch (classMember.getKind()) {
        case CLASS_PROPERTY_DECLARATION:
        case CLASS_CONSTANT_PROPERTY_DECLARATION:
          retrieveTypeFromDoc(classMember);
          break;
        case METHOD_DECLARATION:
          retrieveTypeFromDoc(classMember);
          retrieveTypeFromParameter((MethodDeclarationTree) classMember);
          break;
        default:
          break;
      }
    }
  }

  private void retrieveTypeFromParameter(MethodDeclarationTree methodDeclaration) {
    for (ParameterTree parameter : methodDeclaration.parameters().parameters()) {
      DeclaredTypeTree type = parameter.declaredType();
      if (type != null && type.isSimple() && ((TypeTree) type).typeName().is(Kind.NAMESPACE_NAME)) {
        addType(getTypeName((NamespaceNameTree) ((TypeTree) type).typeName()), type);
      }
    }
  }

  private void retrieveTypeFromDoc(ClassMemberTree varDeclaration) {
    SyntaxToken varDecToken = ((PHPTree) varDeclaration).getFirstToken();

    for (SyntaxTrivia comment : varDecToken.trivias()) {
      for (String line : comment.text().split("[" + LexicalConstant.LINE_TERMINATOR + "]++")) {
        retrieveTypeFromCommentLine(line, comment);
      }
    }
  }

  private void retrieveTypeFromCommentLine(String line, Tree trivia) {
    String[] commentLine = line.trim().split("[" + LexicalConstant.WHITESPACE + "]++");

    if (commentLine.length > 2 && DOC_TAGS.contains(commentLine[1])) {
      for (String type : commentLine[2].split("\\|")) {
        type = StringUtils.removeEnd(type, "[]");

        if (!EXCLUDED_TYPES.contains(type.toLowerCase(Locale.ROOT))) {
          addType(type, trivia);
        }
      }
    }
  }

  private void retrieveInstantiatedClassName(NewExpressionTree newExpression) {
    ExpressionTree expression = newExpression.expression();

    if (expression.is(Kind.FUNCTION_CALL)) {
      ExpressionTree callee = ((FunctionCallTree) expression).callee();

      if (callee.is(Kind.NAMESPACE_NAME)) {
        addType(getTypeName((NamespaceNameTree) callee), callee);
      }

    } else if (expression.is(Kind.NAMESPACE_NAME)) {
      addType(getTypeName((NamespaceNameTree) expression), expression);
    }
  }

  private static String getTypeName(NamespaceNameTree namespaceName) {
    String name = namespaceName.fullName();
    String prefix = "namespace\\";
    if (StringUtils.startsWithIgnoreCase(name, prefix)) {
      // fixme (SONARPHP-552): Handle namespaces properly
      name = name.substring(prefix.length() - 1);
    }

    return name;
  }

  private void addType(String type, Tree tree) {
    if (!types.isEmpty()) {
      types.getLast().compute(type, (k, v) -> v == null || v instanceof SyntaxTrivia ? tree : v);
    }
  }

}
