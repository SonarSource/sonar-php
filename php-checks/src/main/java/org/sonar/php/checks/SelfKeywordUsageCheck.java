/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = SelfKeywordUsageCheck.KEY)
public class SelfKeywordUsageCheck extends PHPVisitorCheck {

  public static final String KEY = "S2037";
  private static final String MESSAGE = "Use \"static\" keyword instead of \"self\".";

  /**
   * Use stacks in order to handle nested classes.
   */
  private Deque<Boolean> isFinalClassStack = new ArrayDeque<>();

  private Deque<Set<String>> finalOrPrivateMethodsStack = new ArrayDeque<>();

  private Deque<Set<String>> privatePropertiesStack = new ArrayDeque<>();

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    isFinalClassStack.addLast(isFinalClass(tree));
    finalOrPrivateMethodsStack.addLast(getFinalOrPrivateMethods(tree));
    privatePropertiesStack.addLast(getPrivateProperties(tree));

    super.visitClassDeclaration(tree);

    isFinalClassStack.removeLast();
    finalOrPrivateMethodsStack.removeLast();
    privatePropertiesStack.removeLast();
  }

  private static Set<String> getFinalOrPrivateMethods(ClassDeclarationTree tree) {
    Set<String> finalOrPrivateMethods = new HashSet<>();

    for (ClassMemberTree classMemberTree : tree.members()) {
      if (classMemberTree.is(Kind.METHOD_DECLARATION)) {
        MethodDeclarationTree methodDeclaration = (MethodDeclarationTree) classMemberTree;
        List<SyntaxToken> modifiers = methodDeclaration.modifiers();
        if (CheckUtils.hasModifier(modifiers, "final") || CheckUtils.hasModifier(modifiers, "private")) {
          finalOrPrivateMethods.add(methodDeclaration.name().text());
        }
      }
    }
    return finalOrPrivateMethods;
  }

  private static Set<String> getPrivateProperties(ClassDeclarationTree tree) {
    Set<String> privateProperties = new HashSet<>();

    for (ClassMemberTree classMemberTree : tree.members()) {
      if (classMemberTree.is(Kind.CLASS_PROPERTY_DECLARATION)) {
        ClassPropertyDeclarationTree propertyDeclaration = (ClassPropertyDeclarationTree) classMemberTree;
        List<SyntaxToken> modifiers = propertyDeclaration.modifierTokens();
        if (CheckUtils.hasModifier(modifiers, "private")) {
          propertyDeclaration.declarations().forEach(varDec -> privateProperties.add(varDec.identifier().text()));
        }
      }
    }
    return privateProperties;
  }

  private static boolean isFinalClass(ClassDeclarationTree tree) {
    return tree.modifierToken() != null && "final".equals(tree.modifierToken().text());
  }

  @Override
  public void visitMemberAccess(MemberAccessTree tree) {
    if (tree.is(Tree.Kind.CLASS_MEMBER_ACCESS) && "self".equals(tree.object().toString()) && !isException(tree)) {
      context().newIssue(this, tree.object(), MESSAGE);
    }

    super.visitMemberAccess(tree);
  }

  /**
   * Return true if member can't be overridden
   */
  private boolean isException(MemberAccessTree tree) {
    Tree member = tree.member();

    if (!isFinalClassStack.isEmpty()) {
      return isFinalClassStack.getLast()
        || isFinalOrPrivateMethod(member)
        || isPrivateProperty(member)
        || isConstProperty(member);
    }

    return false;
  }

  private boolean isFinalOrPrivateMethod(Tree member) {
    return member.is(Kind.NAME_IDENTIFIER) && finalOrPrivateMethodsStack.getLast().contains(((NameIdentifierTree) member).text());
  }

  private boolean isPrivateProperty(Tree member) {
    return member.is(Tree.Kind.VARIABLE_IDENTIFIER) && privatePropertiesStack.getLast().contains(((IdentifierTree) member).text());
  }

  private boolean isConstProperty(Tree member) {
    Symbol symbol = context().symbolTable().getSymbol(member);
    return symbol != null && symbol.hasModifier("const");
  }

}
