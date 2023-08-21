/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.checks.phpunit;

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;

@Rule(key = "S2187")
public class NoTestInTestClassCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Add some tests to this class.";

  private List<String> usesNamespaces = new ArrayList<>();

  @Override
  public void visitScript(ScriptTree tree) {
    super.visitScript(tree);
    usesNamespaces = new ArrayList<>();
  }

  @Override
  protected void visitPhpUnitTestCase(ClassDeclarationTree tree) {
    if (!tree.isAbstract()) {
      boolean hasTestMethod = false;
      for (ClassMemberTree member : tree.members()) {
        if (member.is(Tree.Kind.METHOD_DECLARATION) && isTestCaseMethod((MethodDeclarationTree) member, usesNamespaces)) {
          hasTestMethod = true;
          break;
        }
      }

      if (!hasTestMethod) {
        newIssue(tree.name(), MESSAGE);
      }
    }

    super.visitPhpUnitTestCase(tree);
  }

  @Override
  public void visitUseClause(UseClauseTree tree) {
    super.visitUseClause(tree);
    usesNamespaces.add(tree.namespaceName().fullyQualifiedName());
  }
}
