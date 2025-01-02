/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.StatementTree;

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
 *  interface {@link #name()} extends {@link #superInterfaces()} { {@link #members()} }
 * </pre>
 */
public interface ClassDeclarationTree extends StatementTree, ClassTree {

  /**
   * Either {@link PHPKeyword#ABSTRACT abstract} or {@link PHPKeyword#FINAL final}
   * @deprecated - Use {@link #modifiersToken()} instead.
   */
  @Nullable
  @Deprecated(since = "SonarQube 9.7", forRemoval = true)
  SyntaxToken modifierToken();

  /**
   * Contain modifier tokens : {@link PHPKeyword#ABSTRACT abstract},
   * {@link PHPKeyword#FINAL final} and/or {@link PHPKeyword#READONLY readonly}
   */
  List<SyntaxToken> modifiersToken();

  /**
   * Either {@link PHPKeyword#CLASS class}, {@link PHPKeyword#TRAIT trait},
   *  {@link PHPKeyword#INTERFACE interface} or {@link PHPKeyword#ENUM enum}
   */
  @Override
  SyntaxToken classToken();

  NameIdentifierTree name();

  @Override
  @Nullable
  SyntaxToken extendsToken();

  @Override
  @Nullable
  NamespaceNameTree superClass();

  @Override
  @Nullable
  SyntaxToken implementsToken();

  @Override
  SeparatedList<NamespaceNameTree> superInterfaces();

  @Override
  SyntaxToken openCurlyBraceToken();

  @Override
  List<ClassMemberTree> members();

  @Override
  SyntaxToken closeCurlyBraceToken();

  boolean isAbstract();

  boolean isFinal();

  boolean isReadOnly();
}
