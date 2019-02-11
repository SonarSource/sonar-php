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

import com.google.common.collect.ImmutableSet;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.type.StaticFunctionCall;
import org.sonar.php.tree.impl.expression.MemberAccessTreeImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.type.StaticFunctionCall.staticFunctionCall;

@Rule(key = "S4529")
public class HttpEndpointCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that exposing this HTTP endpoint is safe here.";
  private static final ImmutableSet<StaticFunctionCall> SUSPICIOUS_STATIC_FUNCTIONS = ImmutableSet.of(
    staticFunctionCall("Cake\\Routing\\Router::scope"),
    staticFunctionCall("Cake\\Routing\\Router::connect"),
    staticFunctionCall("Cake\\Routing\\Router::plugin"),
    staticFunctionCall("Cake\\Routing\\Router::prefix"));

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (isSuspiciousStaticFunction(tree.callee())) {
      context().newIssue(this, tree.callee(), MESSAGE);
    }

    super.visitFunctionCall(tree);
  }

  private boolean isSuspiciousStaticFunction(ExpressionTree callee) {
    if (callee.is(Tree.Kind.CLASS_MEMBER_ACCESS)) {
      MemberAccessTreeImpl memberAccess = (MemberAccessTreeImpl) callee;
      if (memberAccess.object().is(Tree.Kind.NAMESPACE_NAME) && memberAccess.member().is(Tree.Kind.NAME_IDENTIFIER)) {
        QualifiedName className = getFullyQualifiedName((NamespaceNameTree) memberAccess.object());
        String memberName = ((NameIdentifierTree) memberAccess.member()).text();
        return SUSPICIOUS_STATIC_FUNCTIONS.stream().anyMatch(staticFunctionCall -> staticFunctionCall.matches(className, memberName));
      }
    }
    return false;
  }

}
