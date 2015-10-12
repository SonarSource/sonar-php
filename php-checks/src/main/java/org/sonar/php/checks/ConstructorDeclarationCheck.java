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

import com.google.common.collect.ImmutableList;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = ConstructorDeclarationCheck.KEY,
  name = "PHP 4 constructor declarations should not be used",
  priority = Priority.MAJOR,
  tags = {Tags.PITFALL})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.COMPILER_RELATED_PORTABILITY)
@SqaleConstantRemediation("2min")
public class ConstructorDeclarationCheck extends PHPSubscriptionCheck {
  public static final String KEY = "S1603";

  private static final String MESSAGE_OLD_STYLE_PRESENT = "Replace this function name \"%s\" with \"__construct\".";
  private static final String MESSAGE_BOTH_STYLE_PRESENT = "Replace this function name \"%s\", since a \"__construct\" method has already been defined in this class.";

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Kind.CLASS_DECLARATION);
  }

  @Override
  public void visitNode(Tree tree) {
    ClassDeclarationTree classDec = (ClassDeclarationTree) tree;

    MethodDeclarationTree oldStyleConstructor = null;
    MethodDeclarationTree newStyleConstructor = null;

    for (ClassMemberTree member : classDec.members()) {
      if (member.is(Kind.METHOD_DECLARATION)) {
        MethodDeclarationTree method = (MethodDeclarationTree) member;
        String methodName = method.name().text();

        if (classDec.name().text().equalsIgnoreCase(methodName)) {
          oldStyleConstructor = method;

        } else if (ClassDeclarationTree.PHP5_CONSTRUCTOR_NAME.equalsIgnoreCase(methodName)) {
          newStyleConstructor = method;
        }
      }
    }

    if (oldStyleConstructor != null) {
      String message = String.format(
        newStyleConstructor != null ? MESSAGE_BOTH_STYLE_PRESENT : MESSAGE_OLD_STYLE_PRESENT,
        oldStyleConstructor.name().text());

      context().newIssue(KEY, message).tree(oldStyleConstructor);
    }
  }

}
