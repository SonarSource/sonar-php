/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.api.tree.declaration;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Common interface for {@link ClassDeclarationTree} and {@link AnonymousClassTree}
 */
public interface ClassTree extends Tree, HasAttributes {
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
