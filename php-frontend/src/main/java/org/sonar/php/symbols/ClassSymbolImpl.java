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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class ClassSymbolImpl implements ClassSymbol {

  private final LocationInFile location;
  private final QualifiedName qualifiedName;
  private ClassSymbol superClass;

  public ClassSymbolImpl(LocationInFile location, QualifiedName qualifiedName) {
    this.location = location;
    this.qualifiedName = qualifiedName;
  }

  @Override
  public LocationInFile location() {
    return location;
  }

  @Override
  public QualifiedName qualifiedName() {
    return qualifiedName;
  }

  @Override
  public Optional<ClassSymbol> superClass() {
    return Optional.ofNullable(superClass);
  }

  @Override
  public boolean isUnknownSymbol() {
    return false;
  }

  public static Map<ClassSymbolData, ClassSymbol> createSymbols(Set<ClassSymbolData> fileDeclarations, ProjectSymbolData projectSymbolData) {
    Map<ClassSymbolData, ClassSymbolImpl> symbolsByData = new HashMap<>();
    Map<ClassSymbolData, ClassSymbol> result = new HashMap<>();
    Deque<ClassSymbolData> toComplete = new ArrayDeque<>();
    Map<QualifiedName, ClassSymbol> symbolsByQualifiedName = new HashMap<>();
    fileDeclarations.forEach(data -> {
      ClassSymbolImpl symbol = new ClassSymbolImpl(data.location(), data.qualifiedName());
      result.put(data, symbol);
      toComplete.push(data);
      symbolsByQualifiedName.put(symbol.qualifiedName, symbol);
      symbolsByData.put(data, symbol);
    });

    while (!toComplete.isEmpty()) {
      ClassSymbolData data = toComplete.pop();
      Optional<QualifiedName> superClassName = data.superClass();
      if (superClassName.isPresent()) {
        ClassSymbol superClass = symbolsByQualifiedName.get(superClassName.get());
        if (superClass == null) {
          Optional<ClassSymbolData> superClassData = projectSymbolData.classSymbolData(superClassName.get());
          if (superClassData.isPresent()) {
            ClassSymbolImpl knownSuperClass = new ClassSymbolImpl(superClassData.get().location(), superClassName.get());
            toComplete.push(superClassData.get());
            symbolsByData.put(superClassData.get(), knownSuperClass);
            superClass = knownSuperClass;
          } else {
            superClass = new UnknownClassSymbol(superClassName.get());
          }
          symbolsByQualifiedName.put(superClassName.get(), superClass);
        }
        symbolsByData.get(data).superClass = superClass;
      }
    }

    return result;
  }
}
