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
package org.sonar.plugins.php.api.symbols;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public interface Symbol {
  List<SyntaxToken> modifiers();

  boolean hasModifier(String modifier);

  List<SyntaxToken> usages();

  String name();

  /**
   * @return qualified name for class, function, method or class constant, null otherwise
   */
  @Nullable
  QualifiedName qualifiedName();

  IdentifierTree declaration();

  boolean is(Kind kind);

  boolean called(String name);

  Kind kind();

  enum Kind {
    VARIABLE,
    FUNCTION,
    PARAMETER,
    CLASS,
    FIELD;

    public boolean hasQualifiedName() {
      return this == CLASS || this == FUNCTION || this == FIELD;
    }
  }
}
