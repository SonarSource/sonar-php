/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <p>Class <a href="http://php.net/manual/en/language.oop5.properties.php">Properties</a>
 * <pre>
 *  var {@link #declarations()} ;
 *
 *  public {@link #declarations()} ;
 *  protected {@link #declarations()} ;
 *  private {@link #declarations()} ;
 *
 *  public static {@link #declarations()} ;
 * </pre>
 *
 * <p>Class <a href="http://php.net/manual/en/language.oop5.constants.php">Constants</a>
 * <pre>
 *  const {@link #declarations()} ;
 * </pre>
 */
public interface ClassPropertyDeclarationTree extends ClassMemberTree, HasAttributes {

  List<SyntaxToken> modifierTokens();

  /**
   * @deprecated since 3.11 - Use {@link #declaredType()} instead.
   */
  @Deprecated
  @Nullable
  TypeTree typeAnnotation();

  @Nullable
  DeclaredTypeTree declaredType();

  SeparatedList<VariableDeclarationTree> declarations();

  /**
   * @return the {@link PropertyHookListTree} if it exists.
   * In this case there exists only one {@link VariableDeclarationTree} in the <code>declarations</code>.
   */
  @Nullable
  PropertyHookListTree propertyHookList();

  @Nullable
  SyntaxToken eosToken();

  boolean hasModifiers(String... modifiers);

}
