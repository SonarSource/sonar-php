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

import java.util.List;

import javax.annotation.Nullable;

import org.sonar.php.tree.impl.SeparatedList;

import com.google.common.annotations.Beta;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

@Beta
public interface ClassFieldDeclarationTree extends ClassMemberTree {

  /**
   * Members can be only:
   * <ul>
   *   <li>{@link org.sonar.php.api.PHPKeyword#CONST const}
   *  <p>
   * or only:
   *   <li>{@link org.sonar.php.api.PHPKeyword#VAR var}
   *  <p>
   * or a combination of:
   *   <li>{@link org.sonar.php.api.PHPKeyword#PUBLIC public}
   *   <li>{@link org.sonar.php.api.PHPKeyword#PROTECTED protected}
   *   <li>{@link org.sonar.php.api.PHPKeyword#PRIVATE private}
   *   <li>{@link org.sonar.php.api.PHPKeyword#STATIC static}
   *   <li>{@link org.sonar.php.api.PHPKeyword#ABSTRACT abstract}
   *   <li>{@link org.sonar.php.api.PHPKeyword#FINAL final}
   *
   */
  List<SyntaxToken> modifierTokens();

  SeparatedList<VariableDeclarationTree> declarations();

  @Nullable
  SyntaxToken semicolonToken();

}
