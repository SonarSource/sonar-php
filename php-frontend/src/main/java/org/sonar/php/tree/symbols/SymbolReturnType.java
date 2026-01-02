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
package org.sonar.php.tree.symbols;

import javax.annotation.Nullable;
import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.plugins.php.api.symbols.ReturnType;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;

public record SymbolReturnType(boolean isPresent, boolean isVoid) implements ReturnType {
  public static SymbolReturnType from(@Nullable ReturnTypeClauseTree returnTypeClause) {
    if (returnTypeClause == null) {
      return new SymbolReturnType(false, false);
    } else {
      if (returnTypeClause.declaredType().isSimple()) {
        TypeTree type = (TypeTree) returnTypeClause.declaredType();
        if (type.typeName() instanceof ClassNamespaceNameTreeImpl name && ("void".equalsIgnoreCase(name.fullName()))) {
          return new SymbolReturnType(true, true);
        }
      }
      return new SymbolReturnType(true, false);
    }
  }

  public static SymbolReturnType notDefined() {
    return new SymbolReturnType(false, false);
  }
}
