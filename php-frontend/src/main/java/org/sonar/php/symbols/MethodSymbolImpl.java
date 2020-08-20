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
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MethodSymbolImpl extends FunctionSymbolIndex.FunctionSymbolImpl implements MethodSymbol {

  private final MethodSymbolData data;
  private final ClassSymbol owner;

  private Trilean isOverriding;
  private final Set<ClassSymbol> visitedClasses = new HashSet<>();

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

  /**
   * Check all interfaces of a class before searching in the super class
   * to validate if a method has been declared in an implemented interface or extended super class
   */
  private Trilean checkSuperClassesAndInterfacesForDeclaration() {
    boolean unknownInterface = false;
    boolean inSuperClass = false;
    Optional<ClassSymbol> currentClass = Optional.of(owner);
    while (currentClass.isPresent()) {
      if (isLoop(currentClass.get())) {
        return Trilean.UNKNOWN;
      }

      if (currentClass.get().isUnknownSymbol()) {
        return Trilean.UNKNOWN;
      }
      // check only super classes for method is declared
      if (inSuperClass && !currentClass.get().getDeclaredMethod(name()).isUnknownSymbol()) {
        return Trilean.TRUE;
      }

      // check all implemented interfaces recursively
      Trilean isDeclaredInInterface = isDeclaredInInterface(currentClass.get().implementedInterfaces());
      if (isDeclaredInInterface.isTrue()) {
        return Trilean.TRUE;
      }
      if (isDeclaredInInterface.isUnknown()) {
        unknownInterface = true;
      }

      currentClass = currentClass.get().superClass();
      inSuperClass = true;
    }
    // if there is no declaration identified
    // and one or more unknown interface or super class were detected return UNKNOWN
    if (unknownInterface) {
      return Trilean.UNKNOWN;
    }
    return Trilean.FALSE;
  }

  private boolean isLoop(ClassSymbol classSymbol) {
    if (visitedClasses.contains(classSymbol)) {
      return true;
    }
    visitedClasses.add(classSymbol);
    return false;
  }

  /**
   * Loop over all implemented interfaces. Check whether the interface declares the method.
   * If an interface implements other interfaces, check these recursive ones.
   */
  private Trilean isDeclaredInInterface(List<ClassSymbol> interfaces) {
    boolean unknownSymbol = false;
    for (ClassSymbol interfaceSymbol : interfaces) {
      if (isLoop(interfaceSymbol)) {
        return Trilean.UNKNOWN;
      }

      if (interfaceSymbol.isUnknownSymbol()) {
        unknownSymbol = true;
      }
      if (!interfaceSymbol.getDeclaredMethod(name()).isUnknownSymbol()) {
        return Trilean.TRUE;
      }

      Trilean inSuperClass = isDeclaredInInterface(interfaceSymbol.implementedInterfaces());
      if (inSuperClass.isTrue()) {
        return Trilean.TRUE;
      }
      if (inSuperClass.isUnknown()) {
        unknownSymbol = true;
      }
    }
    if (unknownSymbol) {
      return Trilean.UNKNOWN;
    }
    return Trilean.FALSE;
  }
}
