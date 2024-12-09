/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
import org.sonar.php.api.PHPPunctuator;
import org.sonar.plugins.php.api.symbols.HasMethodSymbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <p>Class <a href="http://php.net/manual/en/language.oop5.basic.php">Methods</a>
 * <pre>
 *  public {@link #name()} {@link Tree.Kind#BLOCK {}}
 *  protected {@link #name()} {@link Tree.Kind#BLOCK {}}
 *  private {@link #name()} {@link Tree.Kind#BLOCK {}}
 *
 *  public static {@link #name()} {@link Tree.Kind#BLOCK {}}
 *  public final {@link #name()} {@link Tree.Kind#BLOCK {}}
 *
 *  abstract public {@link #name()} ;
 * </pre>
 */
public interface MethodDeclarationTree extends ClassMemberTree, FunctionTree, HasMethodSymbol {

  /**
   * Members can be:
   * <ul>
   *   <li>{@link PHPKeyword#PUBLIC public}
   *   <li>{@link PHPKeyword#PROTECTED protected}
   *   <li>{@link PHPKeyword#PRIVATE private}
   *   <li>{@link PHPKeyword#STATIC static}
   *   <li>{@link PHPKeyword#ABSTRACT abstract}
   *   <li>{@link PHPKeyword#FINAL final}
   * <ul/>
   */
  List<SyntaxToken> modifiers();

  @Override
  SyntaxToken functionToken();

  @Override
  @Nullable
  SyntaxToken referenceToken();

  NameIdentifierTree name();

  @Override
  ParameterListTree parameters();

  @Override
  @Nullable
  ReturnTypeClauseTree returnTypeClause();

  /**
   * Either {@link PHPPunctuator#SEMICOLON ;} or {@link Tree.Kind#BLOCK block}
   */
  @Override
  Tree body();
}
