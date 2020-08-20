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
package org.sonar.php.utils;

import com.google.common.collect.ImmutableSet;
import org.sonar.php.tree.TreeUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;

import static org.sonar.plugins.php.api.tree.Tree.Kind.ANONYMOUS_CLASS;
import static org.sonar.plugins.php.api.tree.Tree.Kind.CLASS_DECLARATION;
import static org.sonar.plugins.php.api.tree.Tree.Kind.CLASS_MEMBER_ACCESS;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NAMESPACE_NAME;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NAME_IDENTIFIER;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NEW_EXPRESSION;
import static org.sonar.plugins.php.api.tree.Tree.Kind.OBJECT_MEMBER_ACCESS;
import static org.sonar.plugins.php.api.tree.Tree.Kind.VARIABLE_IDENTIFIER;

public class SymbolUtils {

  private SymbolUtils() {}

  public static boolean isFunctionCall(FunctionCallTree functionCallTree) {
    return functionCallTree.callee().is(NAMESPACE_NAME) && !isNewExpressionCall(functionCallTree);
  }

  public static boolean isNewExpressionCall(FunctionCallTree functionCallTree) {
    return functionCallTree.getParent() != null && functionCallTree.getParent().is(NEW_EXPRESSION);
  }

  public static boolean isMethodCall(FunctionCallTree functionCallTree) {
    return functionCallTree.callee().is(OBJECT_MEMBER_ACCESS) || functionCallTree.callee().is(CLASS_MEMBER_ACCESS);
  }

  /**
   * check whether member access of dynamic method call is resolvable as class inner call (e.g. $this->foo())
   */
  private static boolean isResolvableInnerObjectMemberAccess(MemberAccessTree memberAccessTree) {
    if (memberAccessTree.member().is(NAME_IDENTIFIER)) {
      Tree object = memberAccessTree.object();
      return object.is(VARIABLE_IDENTIFIER) && ((VariableIdentifierTree) object).text().equals("$this");
    }

    return false;
  }

  /**
   * check whether member access of static method call is resolvable as class inner call (e.g. self::foo(), or static::foo())
   */
  private static boolean isResolvableInnerClassMemberAccess(MemberAccessTree memberAccessTree) {
    if (memberAccessTree.member().is(NAME_IDENTIFIER)) {
      Tree object = memberAccessTree.object();
      return ((object.is(NAMESPACE_NAME) && ((NamespaceNameTree) object).fullName().equals("self"))
        || (object.is(NAME_IDENTIFIER) && ((NameIdentifierTree) object).text().equals("static")));
    }
    return false;
  }

  private static boolean isInnerClassMemberAccess(MemberAccessTree memberAccessTree) {
    return TreeUtils.findAncestorWithKind(memberAccessTree, ImmutableSet.of(CLASS_DECLARATION, ANONYMOUS_CLASS)) != null;
  }

  public static boolean isResolvableInnerMemberAccess(MemberAccessTree memberAccessTree) {
    return isInnerClassMemberAccess(memberAccessTree)
      && (isResolvableInnerClassMemberAccess(memberAccessTree) || isResolvableInnerObjectMemberAccess(memberAccessTree));
  }

  /**
   * check whether member access of static method call is resolvable as known receiver call (e.g. KnownClass::foo())
   */
  public static boolean isResolvableMemberAccess(MemberAccessTree memberAccessTree) {
    return memberAccessTree.is(CLASS_MEMBER_ACCESS) && memberAccessTree.member().is(NAME_IDENTIFIER) && memberAccessTree.object().is(NAMESPACE_NAME);
  }

  /**
   * check whether member access of dynamic or static method call is resolvable
   */
  public static boolean isResolvable(MemberAccessTree memberAccessTree) {
    return isResolvableMemberAccess(memberAccessTree) || isResolvableInnerMemberAccess(memberAccessTree);
  }
}
