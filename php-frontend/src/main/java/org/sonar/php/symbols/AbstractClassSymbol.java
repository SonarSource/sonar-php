/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.symbols;

import java.util.HashSet;
import java.util.Set;
import org.sonar.plugins.php.api.symbols.QualifiedName;

public abstract class AbstractClassSymbol implements ClassSymbol {

  @Override
  public Trilean isOrSubClassOf(QualifiedName qualifiedName) {
    Set<ClassSymbol> visitedClasses = new HashSet<>();
    ClassSymbol superClass = this;
    while (superClass != null) {
      if (qualifiedName.equals(superClass.qualifiedName())) {
        return Trilean.TRUE;
      }
      if (superClass.isUnknownSymbol()) {
        return Trilean.UNKNOWN;
      }
      if (!visitedClasses.add(superClass)) {
        return Trilean.FALSE;
      }
      superClass = superClass.superClass().orElse(null);
    }
    return Trilean.FALSE;
  }
}
