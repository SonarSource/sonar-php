/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="https://www.php.net/manual/de/language.attributes.syntax.php">Attributes</a>
 * <pre>
 *   {@link #name()}
 *   {@link #name()}()
 *   {@link #name()}( {@link #arguments()} )
 * </pre>
 */
public interface AttributeTree extends Tree {

  NamespaceNameTree name();

  @Nullable
  SyntaxToken openParenthesisToken();

  SeparatedList<CallArgumentTree> arguments();

  @Nullable
  SyntaxToken closeParenthesisToken();

}
