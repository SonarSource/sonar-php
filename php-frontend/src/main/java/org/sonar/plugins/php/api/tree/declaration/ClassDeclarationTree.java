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
package org.sonar.plugins.php.api.tree.declaration;

import com.google.common.annotations.Beta;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.StatementTree;

import javax.annotation.Nullable;
import java.util.List;

/**
 * <p><a href="http://php.net/manual/en/language.oop5.php">Class</a> and <a href="http://php.net/manual/en/language.oop5.traits.php">Trait</a>
 * <pre>
 *  class {@link #name()} { {@link #members()} }
 *  trait {@link #name()} { {@link #members()} }
 *
 *  abstract class {@link #name()} { {@link #members()} }
 *  final class {@link #name()} { {@link #members()} }
 *
 *  class {@link #name()} extends {@link #superClass()} { {@link #members()} }
 *  class {@link #name()} extends {@link #superClass()} implements {@link #superInterfaces()} { {@link #members()} }
 * </pre>
 *
 * <p><a href="http://php.net/manual/en/language.oop5.interfaces.php">Interface</a>
 * <pre>
 *  interface {@link #name()} { {@link #members()} }
 *  interface {@link #name()} implements {@link #superInterfaces()} { {@link #members()} }
 * </pre>
 */
@Beta
public interface ClassDeclarationTree extends StatementTree {

  /**
   * PHP 5 introduce a new way to declare constructors: by defining a method named "__construct".
   * Previously constructors were declared by defining a method with the same name as the class.
   */
  String PHP5_CONSTRUCTOR_NAME = "__construct";

  /**
   * Either {@link PHPKeyword#ABSTRACT abstract} or {@link PHPKeyword#FINAL final}
   */
  @Nullable
  SyntaxToken modifierToken();

  /**
   * Either {@link PHPKeyword#CLASS class}, {@link PHPKeyword#TRAIT trait}
   * or {@link PHPKeyword#INTERFACE interface}
   */
  SyntaxToken classEntryTypeToken();

  NameIdentifierTree name();

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
