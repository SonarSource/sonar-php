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
package org.sonar.plugins.php.api.tree.statement;

import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/language.oop5.traits.php">Trait alias</a>
 * <pre>
 * {@link #methodReference()} as {@link #alias()} ;
 * {@link #methodReference()} as {@link #modifierToken()} {@link #alias()} ;
 * </pre>
 */
public interface TraitAliasTree extends TraitAdaptationStatementTree {

  TraitMethodReferenceTree methodReference();

  SyntaxToken asToken();

  /**
   * Member can be one of:
   *   <li>{@link org.sonar.php.api.PHPKeyword#PUBLIC public}
   *   <li>{@link org.sonar.php.api.PHPKeyword#PROTECTED protected}
   *   <li>{@link org.sonar.php.api.PHPKeyword#PRIVATE private}
   *   <li>{@link org.sonar.php.api.PHPKeyword#STATIC static}
   *   <li>{@link org.sonar.php.api.PHPKeyword#ABSTRACT abstract}
   *   <li>{@link org.sonar.php.api.PHPKeyword#FINAL final}
   *
   */
  @Nullable
  SyntaxToken modifierToken();

  @Nullable
  NameIdentifierTree alias();

}
