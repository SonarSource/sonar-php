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
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.Set;

@Rule(
  key = MissingMethodVisibilityCheck.KEY,
  name = "Method visibility should be explicitly declared",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION, Tags.PSR2})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("2min")
public class MissingMethodVisibilityCheck extends PHPVisitorCheck {

  public static final String KEY = "S1784";
  private static final String MESSAGE = "Explicitly mention the visibility of this %s \"%s\".";

  private static final Set<String> VISIBILITIES = ImmutableSet.of(
    PHPKeyword.PRIVATE.getValue(),
    PHPKeyword.PROTECTED.getValue(),
    PHPKeyword.PUBLIC.getValue());

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);

    if (tree.is(Kind.CLASS_DECLARATION)) {

      for (ClassMemberTree member : tree.members()) {

        if (member.is(Kind.METHOD_DECLARATION)) {
          checkMethod((MethodDeclarationTree) member, tree.name().text());
        }
      }
    }
  }

  private void checkMethod(MethodDeclarationTree method, String className) {
    if (!hasVisibilityModifier(method)) {
      String methodName = method.name().text();
      String message = String.format(MESSAGE, getMethodKind(methodName, className), methodName);
      context().newIssue(KEY, message).tree(method);
    }
  }

  // fixme (Lena) : could be replaced with method implemented in https://github.com/pynicolas/sonar-php/commit/c8ba74d43c0816871e928d9415da68791fbde5e8
  private boolean hasVisibilityModifier(MethodDeclarationTree method) {
    for (SyntaxToken modifier : method.modifiers()) {
      if (VISIBILITIES.contains(modifier.text().toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private String getMethodKind(String methodName, String className) {
    if ("__construct".equalsIgnoreCase(methodName) || methodName.equalsIgnoreCase(className)) {
      return "constructor";

    } else if ("__destruct".equalsIgnoreCase(methodName)) {
      return "destructor";

    } else {
      return "method";
    }
  }

}
