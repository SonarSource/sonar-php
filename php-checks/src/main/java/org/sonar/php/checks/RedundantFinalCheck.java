/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = RedundantFinalCheck.KEY)
public class RedundantFinalCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S1990";

  private static final String MESSAGE = "Remove this \"final\" modifier.";

  @Override
  public List<Kind> nodesToVisit() {
    return Collections.singletonList(Kind.CLASS_DECLARATION);
  }

  @Override
  public void visitNode(Tree tree) {
    ClassDeclarationTree classDeclaration = (ClassDeclarationTree) tree;

    if (classDeclaration.isFinal()) {
      for (ClassMemberTree classMember : classDeclaration.members()) {
        checkClassMember(classMember);
      }
    }
  }

  private void checkClassMember(ClassMemberTree classMember) {
    if (classMember.is(Kind.METHOD_DECLARATION)) {
      SyntaxToken finalModifier = getFinalModifier((MethodDeclarationTree) classMember);
      if (finalModifier != null) {
        context().newIssue(this, finalModifier, MESSAGE);
      }
    }
  }

  @Nullable
  private static SyntaxToken getFinalModifier(MethodDeclarationTree methodDeclaration) {
    for (SyntaxToken modifier : methodDeclaration.modifiers()) {
      if (isFinalModifier(modifier)) {
        return modifier;
      }
    }
    return null;
  }

  private static boolean isFinalModifier(@Nullable SyntaxToken modifier) {
    return modifier != null && PHPKeyword.FINAL.getValue().equalsIgnoreCase(modifier.text());
  }

}
