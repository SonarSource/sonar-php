/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.EnumCaseTree;

/**
 * <a href="https://www.php.net/manual/en/language.enumerations.php">Enumerations</a>
 * <pre>
 *   enum {@link #name()} { {@link #members()} }
 *   enum {@link #name()} : {@link #backingType()} { {@link #members()} }
 *   enum {@link #name()} implements {@link #superInterfaces()} { {@link #members()} }
 * </pre>
 */
public interface EnumDeclarationTree extends ClassDeclarationTree {

  /**
   * @deprecated - Use {@link #modifiersToken()} instead.
   */
  @Nullable
  @Override
  @Deprecated(since = "SonarQube 9.7", forRemoval = true)
  SyntaxToken modifierToken();

  @Override
  List<SyntaxToken> modifiersToken();

  @Override
  NameIdentifierTree name();

  @Nullable
  SyntaxToken typeColonToken();

  @Nullable
  TypeTree backingType();

  @Nullable
  @Override
  SyntaxToken implementsToken();

  @Override
  SeparatedList<NamespaceNameTree> superInterfaces();

  @Override
  SyntaxToken openCurlyBraceToken();

  @Override
  List<ClassMemberTree> members();

  /**
   * The cases of the enumeration.
   * These are also part of {@link EnumDeclarationTree#members()}
   */
  List<EnumCaseTree> cases();

  @Override
  SyntaxToken closeCurlyBraceToken();

}
