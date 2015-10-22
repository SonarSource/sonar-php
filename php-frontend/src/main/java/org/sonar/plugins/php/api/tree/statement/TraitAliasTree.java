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
package org.sonar.plugins.php.api.tree.statement;

import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import com.google.common.annotations.Beta;

import javax.annotation.Nullable;

@Beta
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
