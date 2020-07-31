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
package org.sonar.php.checks.phpunit;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;

import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;
import static org.sonar.plugins.php.api.tree.Tree.Kind.CLASS_MEMBER_ACCESS;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NAMESPACE_NAME;
import static org.sonar.plugins.php.api.tree.Tree.Kind.REGULAR_STRING_LITERAL;

@Rule(key = "S3477")
public class TestCatchRuntimeExceptionCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Remove this catch block so that error conditions can be caught in testing, rather than in the field.";
  private static final String EXPECT_EXCEPTION_ASSERTION = "expectException";
  private static final String RUNTIME_EXCEPTION = "RuntimeException";

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    if (isPhpUnitTestMethod()) {
      tree.exceptionTypes().stream()
        .filter(this::isRuntimeException)
        .forEach(this::addIssue);
    }

    super.visitCatchBlock(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (isPhpUnitTestMethod() && EXPECT_EXCEPTION_ASSERTION.equalsIgnoreCase(getFunctionName(tree)) && !tree.arguments().isEmpty()) {
      ExpressionTree exception = tree.arguments().get(0);
      if ((exception.is(REGULAR_STRING_LITERAL) && isRuntimeException((LiteralTree) exception))
        || (exception.is(CLASS_MEMBER_ACCESS) && isRuntimeException((MemberAccessTree) exception))) {
        addIssue(exception);
      }
    }

    super.visitFunctionCall(tree);
  }

  private boolean isRuntimeException(MemberAccessTree mat) {
    return CheckUtils.isClassNameResolution(mat) && mat.object().is(NAMESPACE_NAME) && isRuntimeException((NamespaceNameTree) mat.object());
  }

  private boolean isRuntimeException(LiteralTree lt) {
    return trimQuotes(lt).equalsIgnoreCase(RUNTIME_EXCEPTION);
  }

  private boolean isRuntimeException(NamespaceNameTree nnt) {
    return Symbols.getClass(nnt).qualifiedName().equals(qualifiedName(RUNTIME_EXCEPTION));
  }

  private void addIssue(Tree tree) {
    context().newIssue(this, tree, MESSAGE);
  }

  private String getFunctionName(FunctionCallTree fct) {
    String name = CheckUtils.getFunctionName(fct);
    if (name != null && name.contains("::")) {
      name = name.substring(name.indexOf("::") + 2);
    }
    return name;
  }

}
