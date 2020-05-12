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
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Rule(key = ClassNamedLikeExceptionCheck.KEY)
public class ClassNamedLikeExceptionCheck extends PHPVisitorCheck {
  public static final String KEY = "S2166";
  private static final String EXCEPTION_KEYWORD = "Exception";

  Map<String, ClassDeclarationTree> classNamesToTree = new HashMap<>();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    classNamesToTree.clear();
    super.visitCompilationUnit(tree);
    checkClasses();
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    classNamesToTree.put(tree.name().text(), tree);
    super.visitClassDeclaration(tree);
  }

  private void checkClasses() {
    // Get the classes whose name ends with "Exception"
    Set<ClassDeclarationTree> classesToConsider = classNamesToTree.entrySet().stream()
      .filter(entry -> entry.getKey().endsWith(EXCEPTION_KEYWORD))
      .map(Map.Entry::getValue)
      .collect(Collectors.toSet());

    for (ClassDeclarationTree classTree : classesToConsider) {
      if (!classExtendsException(classTree)) {
        context().newIssue(this, classTree.name(), "class should extend Exception");
      }
    }
  }

  private boolean classExtendsException(ClassDeclarationTree classTree) {
    NamespaceNameTree parent = classTree.superClass();
    if (parent == null) {
      return false;
    }

    if (parent.qualifiedName().equalsIgnoreCase(EXCEPTION_KEYWORD) ||
      !classNamesToTree.containsKey(parent.qualifiedName())) { // Exception when we do not have information about the class
      return true;
    }

    return classExtendsException(classNamesToTree.get(parent.qualifiedName()));
  }
}
