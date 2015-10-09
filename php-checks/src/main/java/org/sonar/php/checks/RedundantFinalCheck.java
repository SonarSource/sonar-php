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
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = RedundantFinalCheck.KEY,
  name = "\"final\" should not be used redundantly",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class RedundantFinalCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S1990";

  private static final String MESSAGE = "Remove this \"final\" modifier.";

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Kind.CLASS_DECLARATION);
  }

  @Override
  public void visitNode(Tree tree) {
    ClassDeclarationTree classDeclaration = (ClassDeclarationTree) tree;

    if (isFinalModifier(classDeclaration.modifierToken())) {

      for (ClassMemberTree classMember : classDeclaration.members()) {
        if (classMember.is(Kind.METHOD_DECLARATION) && hasFinalModifier((MethodDeclarationTree) classMember)) {
          context().newIssue(KEY, MESSAGE).tree(classMember);
        }
      }

    }
  }

  private static boolean hasFinalModifier(MethodDeclarationTree methodDeclaration) {
    for (SyntaxToken modifier : methodDeclaration.modifiers()) {
      if (isFinalModifier(modifier)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isFinalModifier(SyntaxToken modifier) {
    return modifier != null && PHPKeyword.FINAL.getValue().equalsIgnoreCase(modifier.text());
  }

}
