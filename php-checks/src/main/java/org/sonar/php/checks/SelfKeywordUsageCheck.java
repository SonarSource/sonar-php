/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;

@Rule(
  key = SelfKeywordUsageCheck.KEY,
  name = "Static members should be referenced with \"static::\"",
  priority = Priority.MAJOR,
  tags = {Tags.PITFALL})
@ActivatedByDefault
@SqaleConstantRemediation("2min")
public class SelfKeywordUsageCheck extends PHPVisitorCheck {

  public static final String KEY = "S2037";
  private static final String MESSAGE = "Use \"static\" keyword instead of \"self\".";

  private Deque<Boolean> isFinalClassStack = new ArrayDeque<>();
  private Deque<Set<String>> finalMethodsStack = new ArrayDeque<>();

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    isFinalClassStack.addLast(isFinalClass(tree));
    finalMethodsStack.addLast(getFinalMethods(tree));

    super.visitClassDeclaration(tree);

    isFinalClassStack.removeLast();
    finalMethodsStack.removeLast();
  }

  private static Set<String> getFinalMethods(ClassDeclarationTree tree) {
    Set<String> finalMethods = new HashSet<>();

    for (ClassMemberTree classMemberTree : tree.members()) {
      if (classMemberTree.is(Kind.METHOD_DECLARATION)) {
        MethodDeclarationTree methodDeclaration = (MethodDeclarationTree) classMemberTree;
        if (CheckUtils.hasModifier(methodDeclaration.modifiers(), "final")) {
          finalMethods.add(methodDeclaration.name().text());
        }
      }
    }
    return finalMethods;
  }

  private static boolean isFinalClass(ClassDeclarationTree tree) {
    return tree.modifierToken() != null && "final".equals(tree.modifierToken().text());
  }

  @Override
  public void visitClassPropertyDeclaration(ClassPropertyDeclarationTree tree) {
    // don't enter inside class property declarations
  }

  @Override
  public void visitMemberAccess(MemberAccessTree tree) {
    if (tree.is(Tree.Kind.CLASS_MEMBER_ACCESS) && "self".equals(tree.object().toString()) && !isException(tree)) {
      context().newIssue(this, MESSAGE).tree(tree);
    }

    super.visitMemberAccess(tree);
  }

  /**
   * Return true if member can't be overridden
   */
  private boolean isException(MemberAccessTree tree) {
    return !isFinalClassStack.isEmpty() && (isFinalClassStack.getLast() || isFinalMethod(tree.member()));
  }

  private boolean isFinalMethod(Tree member) {
    return member.is(Kind.NAME_IDENTIFIER) && finalMethodsStack.getLast().contains(((NameIdentifierTree) member).text());
  }

}
