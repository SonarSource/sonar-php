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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key="S2166")
public class ClassNamedLikeExceptionCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Classes whose name ends with \"Exception\" should directly or indirectly extend the built-in \"Exception\" class.";
  private static final QualifiedName EXCEPTION_FQN = QualifiedName.qualifiedName("Exception");

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    if (tree.is(Tree.Kind.CLASS_DECLARATION) && tree.name().text().endsWith("Exception") && !classExtendsException(tree)) {
      context().newIssue(this, tree.name(), MESSAGE);
    }
    super.visitClassDeclaration(tree);
  }

  private static boolean classExtendsException(ClassDeclarationTree classTree) {
    Set<ClassSymbol> visitedParents = new HashSet<>();
    ClassSymbol classSymbol = Symbols.get(classTree);

    while (!classSymbol.isUnknownSymbol()) {
      Optional<ClassSymbol> superClass = classSymbol.superClass();
      if (!superClass.isPresent()) {
        return false;
      }

      if (visitedParents.contains(superClass.get())) {
        // avoid infinite recursions
        return false;
      }

      visitedParents.add(superClass.get());
      if (EXCEPTION_FQN.equals(superClass.get().qualifiedName())) {
        return true;
      }

      classSymbol = superClass.get();
    }

    return true;
  }
}
