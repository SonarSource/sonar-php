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

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = TooManyMethodsInClassCheck.KEY)
public class TooManyMethodsInClassCheck extends PHPVisitorCheck {

  public static final String KEY = "S1448";
  private static final String MESSAGE = "Class \"%s\" has %s methods, which is greater than %s authorized. Split it into smaller classes.";
  private static final String MESSAGE_ANONYMOUS_CLASS = "This anonymous class has %s methods, which is greater than %s authorized. Split it into smaller classes.";

  private static final int DEFAULT_THRESHOLD = 20;
  private static final boolean DEFAULT_NON_PUBLIC = true;

  @RuleProperty(
    key = "maximumMethodThreshold",
    defaultValue = "" + DEFAULT_THRESHOLD)
  public int maximumMethodThreshold = DEFAULT_THRESHOLD;

  @RuleProperty(
    key = "countNonpublicMethods",
    defaultValue = "" + DEFAULT_NON_PUBLIC,
    type = "BOOLEAN")
  public boolean countNonpublicMethods = DEFAULT_NON_PUBLIC;


  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);
    if (tree.is(Kind.CLASS_DECLARATION, Kind.INTERFACE_DECLARATION)) {
      checkClass(tree);
    }
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    super.visitAnonymousClass(tree);
    checkClass(tree);
  }

  private void checkClass(ClassTree tree) {
    int nbMethod = getNumberOfMethods(tree);

    if (nbMethod > maximumMethodThreshold) {
      String message;
      if (tree.is(Kind.ANONYMOUS_CLASS)) {
        message = String.format(MESSAGE_ANONYMOUS_CLASS, nbMethod, maximumMethodThreshold);
      } else {
        message = String.format(MESSAGE, ((ClassDeclarationTree) tree).name().text(), nbMethod, maximumMethodThreshold);
      }
      context().newIssue(this, tree.classToken(), message);
    }
  }

  private int getNumberOfMethods(ClassTree tree) {
    int nbMethod = 0;

    for (ClassMemberTree classMember : tree.members()) {
      if (classMember.is(Kind.METHOD_DECLARATION) && !isExcluded((MethodDeclarationTree) classMember)) {
        nbMethod++;
      }
    }

    return nbMethod;
  }

  /**
   * Return true if method is private or protected.
   */
  private boolean isExcluded(MethodDeclarationTree tree) {
    if (!countNonpublicMethods) {

      for (SyntaxToken modifierToken : tree.modifiers()) {
        String modifier = modifierToken.text();
        if (PHPKeyword.PROTECTED.getValue().equals(modifier) || PHPKeyword.PRIVATE.getValue().equals(modifier)) {
          return true;
        }
      }
    }
    return false;
  }

}
