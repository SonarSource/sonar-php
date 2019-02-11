/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

@Rule(key = "S4825")
public class HttpRequestCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that this http request is sent safely.";

  private static final List<QualifiedName> SUSPICIOUS_CLASS_INSTANTIATIONS = Arrays.asList(
    qualifiedName("http\\Client\\Request"),
    qualifiedName("GuzzleHttp\\Client"));

  private static final List<FunctionMatcher> IO_FUNCTIONS = Arrays.asList(
    new FunctionMatcher(qualifiedName("copy"), 0, 1),
    new FunctionMatcher(qualifiedName("curl_exec")),
    new FunctionMatcher(qualifiedName("file"), 0),
    new FunctionMatcher(qualifiedName("file_get_contents"), 0),
    new FunctionMatcher(qualifiedName("fopen"), 0),
    new FunctionMatcher(qualifiedName("readfile"), 0),
    new FunctionMatcher(qualifiedName("get_headers")),
    new FunctionMatcher(qualifiedName("get_meta_tags"), 0));

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (isSuspiciousGlobalFunction(tree) || isSuspiciousClassInstantiation(tree.callee())) {
      context().newIssue(this, tree, MESSAGE);
    }
    super.visitFunctionCall(tree);
  }

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    if (isSuspiciousClassInstantiation(tree.expression())) {
      context().newIssue(this, tree, MESSAGE);
    }
    super.visitNewExpression(tree);
  }

  private boolean isSuspiciousClassInstantiation(ExpressionTree expression) {
    if (expression.is(Tree.Kind.NAMESPACE_NAME)) {
      NamespaceNameTree namespaceName = (NamespaceNameTree) expression;
      QualifiedName className = getFullyQualifiedName(namespaceName);
      return SUSPICIOUS_CLASS_INSTANTIATIONS.stream().anyMatch(className::equals);
    }
    return false;
  }

  private boolean isSuspiciousGlobalFunction(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();
    if (callee.is(Tree.Kind.NAMESPACE_NAME)) {
      QualifiedName qualifiedName = getFullyQualifiedName((NamespaceNameTree) callee);
      SeparatedList<ExpressionTree> args = tree.arguments();
      return IO_FUNCTIONS.stream().anyMatch(matcher -> matcher.matches(qualifiedName, args));
    }
    return false;
  }

  private static class FunctionMatcher {

    private static final Pattern HTTP_SCHEME = Pattern.compile("(^|/)(http|https)://");

    private final QualifiedName qualifiedName;
    private final int[] urlIndexes;

    private FunctionMatcher(QualifiedName qualifiedName, int... urlIndexes) {
      this.qualifiedName = qualifiedName;
      this.urlIndexes = urlIndexes;
    }

    private boolean matches(QualifiedName qualifiedName, SeparatedList<ExpressionTree> arguments) {
      return this.qualifiedName.equals(qualifiedName) && (urlIndexes.length == 0 || hasHttpUrl(arguments));
    }

    private boolean hasHttpUrl(SeparatedList<ExpressionTree> arguments) {
      for (int urlIndex : urlIndexes) {
        if (urlIndex < arguments.size()) {
          ExpressionTree argExpression = arguments.get(urlIndex);
          if (argExpression.is(Tree.Kind.REGULAR_STRING_LITERAL) &&
            HTTP_SCHEME.matcher(CheckUtils.trimQuotes((LiteralTree) argExpression)).find()) {
            return true;
          }
        }
      }
      return false;
    }
  }

}
