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
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * This interface represents type clause (appearing in function return type or parameter type),
 * possibly prefixed with a <code>?</code> which stands for "optional".
 */
public interface TypeTree extends DeclaredTypeTree {

  /**
   * Optional leading <code>?</code> token, as in <code>?int</code>, to mark the object
   * (parameter, returned value) as optional.
   */
  @Nullable
  SyntaxToken questionMarkToken();

  /**
   * The underlying type, e.g., <code>int</code> in <code>?int</code>.
   */
  TypeNameTree typeName();

}
