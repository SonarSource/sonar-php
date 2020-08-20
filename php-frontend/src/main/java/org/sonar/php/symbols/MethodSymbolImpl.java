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
import java.util.HashSet;
import java.util.Set;

public class MethodSymbolImpl extends FunctionSymbolIndex.FunctionSymbolImpl implements MethodSymbol {

  private final MethodSymbolData data;
  private final ClassSymbol owner;

  private Trilean isOverriding;
  private final Set<ClassSymbol> visitedCaslasses = new HashSet<>();

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
  public Trilean isOverriding() {
    if (isOverriding == null) {
      isOverriding = checkSuperClassesAndInterfacesForDeclaration();
    }
    return isOverriding;
  }

  private Trilean checkSuperClassesAndInterfacesForDeclaration() {
    ArrayDeque<ClassSymbol> workList = new ArrayDeque<>();
    HashSet<ClassSymbol> visitClasses = new HashSet<>();
    visitClasses.add(owner);

    pushOnWorkList(owner, workList, visitClasses);

    boolean isUnknown = false;
    while (!workList.isEmpty()) {
      ClassSymbol visitClass = workList.removeLast();
      if (visitClass.isUnknownSymbol()) {
        isUnknown = true;
        continue;
      }

      if (!visitClass.getDeclaredMethod(name()).isUnknownSymbol()) {
        return Trilean.TRUE;
      }

      pushOnWorkList(visitClass, workList, visitClasses);
    }

    if (isUnknown) {
      return Trilean.UNKNOWN;
    }
    return Trilean.FALSE;
  }

  /**
   * Push super classes and interfaces to the work list if they were not on the list.
   */
  private static void pushOnWorkList(ClassSymbol classSymbol, ArrayDeque<ClassSymbol> workList, HashSet<ClassSymbol> visitClasses) {
    classSymbol.superClass().ifPresent(e -> {if (!visitClasses.contains(e)){visitClasses.add(e); workList.push(e);}});
    classSymbol.implementedInterfaces().forEach(e -> {if (!visitClasses.contains(e)){visitClasses.add(e); workList.push(e);}});
  }

}
