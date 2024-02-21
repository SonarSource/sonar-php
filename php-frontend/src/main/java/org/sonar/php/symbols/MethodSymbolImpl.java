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
package org.sonar.php.symbols;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import org.sonar.php.tree.symbols.QualifiedNames;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;

public class MethodSymbolImpl extends FunctionSymbolIndex.FunctionSymbolImpl implements MethodSymbol {

  private final MethodSymbolData data;
  private final ClassSymbol owner;
  private Trilean isOverriding;

  public MethodSymbolImpl(MethodSymbolData data, ClassSymbol owner) {
    super(new FunctionSymbolData(data.location(), data.qualifiedName(), data.parameters(), data.properties()));
    this.data = data;
    this.owner = owner;
  }

  @Override
  public Visibility visibility() {
    return data.visibility();
  }

  @Override
  public String name() {
    return data.name();
  }

  @Override
  public QualifiedName qualifiedName() {
    return QualifiedNames.memberName(owner.qualifiedName(), name(), Symbol.Kind.FUNCTION);
  }

  @Override
  public Trilean isOverriding() {
    if (isOverriding == null) {
      isOverriding = computeIsOverriding();
    }
    return isOverriding;
  }

  private Trilean computeIsOverriding() {
    if (visibility().equals(Visibility.PRIVATE) || name().equals("__construct")) {
      return Trilean.FALSE;
    }

    Deque<ClassSymbol> workList = new ArrayDeque<>();
    Set<ClassSymbol> visitedClasses = new HashSet<>();
    visitedClasses.add(owner);

    pushOnIsOverridingWorkList(owner, workList);

    boolean isUnknown = false;
    while (!workList.isEmpty()) {
      ClassSymbol visitedClass = workList.removeLast();
      if (!visitedClasses.add(visitedClass)) {
        continue;
      }

      if (visitedClass.isUnknownSymbol()) {
        isUnknown = true;
        continue;
      }

      MethodSymbol methodSymbol = visitedClass.getDeclaredMethod(name());
      if (!methodSymbol.isUnknownSymbol() && !methodSymbol.visibility().equals(Visibility.PRIVATE)) {
        return Trilean.TRUE;
      }

      pushOnIsOverridingWorkList(visitedClass, workList);
    }

    if (isUnknown) {
      return Trilean.UNKNOWN;
    }
    return Trilean.FALSE;
  }

  /**
   * Push super classes and interfaces to the work list if they were not on the list.
   */
  private static void pushOnIsOverridingWorkList(ClassSymbol classSymbol, Deque<ClassSymbol> workList) {
    classSymbol.superClass().ifPresent(workList::add);
    workList.addAll(classSymbol.implementedInterfaces());
  }

  @Override
  public Trilean isAbstract() {
    return data.isAbstract() ? Trilean.TRUE : Trilean.FALSE;
  }

  @Override
  public Trilean isTestMethod() {
    return data.isTestMethod() ? Trilean.TRUE : Trilean.FALSE;
  }

  @Override
  public ClassSymbol owner() {
    return owner;
  }
}
