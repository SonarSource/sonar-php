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

import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/functions.arguments.php">Function/method parameter</a>
 * <pre>
 *   {@link #variableIdentifier()}
 *
 *   {@link #variableIdentifier()} = {@link #initValue()}
 *   ... {@link #variableIdentifier()}
 *   & {@link #variableIdentifier()}
 *   {@link #type()} {@link #variableIdentifier()}
 * </pre>
 *
 */
public interface ParameterTree extends Tree, HasAttributes {

  @Nullable
  SyntaxToken visibility();

  /**
   * @deprecated since 3.11 - Use {@link #declaredType()} instead.
   */
  @Nullable
  @Deprecated
  TypeTree type();

  @Nullable
  DeclaredTypeTree declaredType();

  @Nullable
  SyntaxToken referenceToken();

  @Nullable
  SyntaxToken ellipsisToken();

  VariableIdentifierTree variableIdentifier();

  @Nullable
  SyntaxToken equalToken();

  @Nullable
  ExpressionTree initValue();

  /**
   * @return the {@link PropertyHookListTree} if it exists.
   */
  @Nullable
  PropertyHookListTree propertyHookList();

  @Nullable
  SyntaxToken readonlyToken();

  @Nullable
  SyntaxToken finalToken();

  default boolean isReadonly() {
    return readonlyToken() != null;
  }

  default boolean isPropertyPromotion() {
    return visibility() != null || finalToken() != null;
  }

  default boolean isFinal() {
    return finalToken() != null;
  }

}
