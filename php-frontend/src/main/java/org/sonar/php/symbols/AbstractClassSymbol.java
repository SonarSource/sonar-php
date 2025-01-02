/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.symbols;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import org.sonar.plugins.php.api.symbols.QualifiedName;

public abstract class AbstractClassSymbol implements ClassSymbol {

  private Set<ClassSymbol> allSuperTypes;

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

  @Override
  public Trilean isSubTypeOf(QualifiedName... typeNames) {
    for (QualifiedName typeName : typeNames) {
      if (allSuperTypes().stream().anyMatch(s -> s.qualifiedName().equals(typeName))) {
        return Trilean.TRUE;
      }
    }
    if (allSuperTypes().stream().anyMatch(Symbol::isUnknownSymbol)) {
      return Trilean.UNKNOWN;
    }
    return Trilean.FALSE;
  }

  /**
   * Returns back the class symbol of all its super types and itself
   */
  @Override
  public Set<ClassSymbol> allSuperTypes() {
    if (allSuperTypes == null) {
      allSuperTypes = new HashSet<>();
      Deque<ClassSymbol> workList = new ArrayDeque<>();
      workList.push(this);
      while (!workList.isEmpty()) {
        ClassSymbol symbol = workList.pop();
        if (!allSuperTypes.add(symbol)) {
          continue;
        }
        symbol.superClass().ifPresent(workList::push);
        symbol.implementedInterfaces().forEach(workList::push);
      }
    }
    return allSuperTypes;
  }
}
