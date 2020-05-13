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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.regex.Pattern;

@Rule(key = "S1186")
public class EmptyMethodCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Add a nested comment explaining why this method is empty, throw an Exception or complete the implementation.";

  private static final int MINIMUM_NUMBER_OF_WORD_CHARS_IN_COMMENT = 3;
  private static final String VALUABLE_COMMENT_FORMAT = "(//|#|/\\*).*\\w{%d,}";

  private static final Pattern VALUABLE_COMMENT_PATTERN = Pattern.compile(String.format(VALUABLE_COMMENT_FORMAT, MINIMUM_NUMBER_OF_WORD_CHARS_IN_COMMENT));


  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (tree.body().is(Kind.BLOCK) && !(hasValuableBody((BlockTree) tree.body()) || isClassAbstract(tree))) {
      context().newIssue(this, tree, MESSAGE);
    }

    super.visitMethodDeclaration(tree);
  }

  private static boolean isClassAbstract(MethodDeclarationTree tree) {
    ClassDeclarationTree classTree = (ClassDeclarationTree) CheckUtils.getParentOfKind(tree, Kind.CLASS_DECLARATION);
    return classTree.modifierToken() != null && classTree.modifierToken().text().equalsIgnoreCase("abstract");
  }

  private static boolean hasValuableBody(BlockTree tree) {
    if (!tree.statements().isEmpty()) {
      return true;
    }

    // Check whether there is a valuable comment in method body
    for (SyntaxTrivia trivia : tree.closeCurlyBraceToken().trivias()) {
      if (VALUABLE_COMMENT_PATTERN.matcher(trivia.text()).find()) {
        return true;
      }
    }

    return false;
  }

}
