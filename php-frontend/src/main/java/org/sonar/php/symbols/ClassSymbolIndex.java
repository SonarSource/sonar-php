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
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class ClassSymbolIndex {

  private final Map<QualifiedName, ClassSymbol> symbolsByQualifiedName = new HashMap<>();
  private final Map<ClassSymbolData, ClassSymbolImpl> symbolsByData = new HashMap<>();
  private final ProjectSymbolData projectSymbolData;

  private ClassSymbolIndex(ProjectSymbolData projectSymbolData) {
    this.projectSymbolData = projectSymbolData;
  }

  public static ClassSymbolIndex create(Set<ClassSymbolData> fileDeclarations, ProjectSymbolData projectSymbolData) {
    ClassSymbolIndex index = new ClassSymbolIndex(projectSymbolData);
    index.init(fileDeclarations);
    return index;
  }

  public ClassSymbol get(QualifiedName qualifiedName) {
    return symbolsByQualifiedName.computeIfAbsent(qualifiedName, qn ->
      projectSymbolData.classSymbolData(qn)
        .<ClassSymbol>map(data -> {
          ClassSymbolImpl symbol = new ClassSymbolImpl(data);
          symbolsByData.put(data, symbol);
          complete(Collections.singleton(data));
          return symbol;
        })
        .orElse(new UnknownClassSymbol(qualifiedName)));
  }

  public ClassSymbol get(ClassSymbolData classSymbolData) {
    return symbolsByData.get(classSymbolData);
  }

  private void init(Set<ClassSymbolData> fileDeclarations) {
    for (ClassSymbolData data : fileDeclarations) {
      ClassSymbolImpl symbol = new ClassSymbolImpl(data);
      symbolsByQualifiedName.put(symbol.qualifiedName, symbol);
      symbolsByData.put(data, symbol);
    }
    complete(fileDeclarations);
  }

  private void complete(Set<ClassSymbolData> fileDeclarations) {
    Deque<ClassSymbolData> toComplete = new ArrayDeque<>();
    toComplete.addAll(fileDeclarations);
    while (!toComplete.isEmpty()) {
      ClassSymbolData data = toComplete.pop();
      Optional<QualifiedName> superClassName = data.superClass();
      if (superClassName.isPresent()) {
        ClassSymbol superClass = symbolsByQualifiedName.get(superClassName.get());
        if (superClass == null) {
          Optional<ClassSymbolData> superClassData = projectSymbolData.classSymbolData(superClassName.get());
          if (superClassData.isPresent()) {
            ClassSymbolImpl knownSuperClass = new ClassSymbolImpl(superClassData.get());
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
  }

  private static class ClassSymbolImpl implements ClassSymbol {

    private final LocationInFile location;
    private final QualifiedName qualifiedName;
    private ClassSymbol superClass;

    private ClassSymbolImpl(ClassSymbolData data) {
      this.location = data.location();
      this.qualifiedName = data.qualifiedName();
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

  }

}
