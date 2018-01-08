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
package org.sonar.plugins.php.api.tree.declaration;

import com.google.common.annotations.Beta;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Commont interface for {@link ClassDeclarationTree} and {@link AnonymousClassTree}
 */
@Beta
public interface ClassTree extends Tree {
  /**
   * PHP 5 introduce a new way to declare constructors: by defining a method named "__construct".
   * Previously constructors were declared by defining a method with the same name as the class.
   */
  String PHP5_CONSTRUCTOR_NAME = "__construct";

  SyntaxToken classToken();

  @Nullable
  SyntaxToken extendsToken();

  @Nullable
  NamespaceNameTree superClass();

  @Nullable
  SyntaxToken implementsToken();

  SeparatedList<NamespaceNameTree> superInterfaces();

  SyntaxToken openCurlyBraceToken();

  List<ClassMemberTree> members();

  SyntaxToken closeCurlyBraceToken();

  /**
   * Fetch class constructor declaration within class {@link #members() members}.
   * Will look for a method named "__construct", if cannot be found it will search for
   * the old-style constructor function, by the name of the class.
   *
   * @return the class constructor method declaration if defined, null otherwise.
   */
  @Nullable
  MethodDeclarationTree fetchConstructor();

}
