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
package org.sonar.php.tree.symbols;

import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.plugins.php.api.symbols.ReturnType;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;

import javax.annotation.Nullable;

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
