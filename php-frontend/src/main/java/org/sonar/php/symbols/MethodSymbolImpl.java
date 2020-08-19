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

import java.util.List;
import java.util.Optional;

public class MethodSymbolImpl extends FunctionSymbolIndex.FunctionSymbolImpl implements MethodSymbol {

  private final MethodSymbolData data;
  private final ClassSymbol owner;

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
    Optional<ClassSymbol> superClass = owner.superClass();
    while (superClass.isPresent()) {
      if (superClass.get().isUnknownSymbol()) {
        return Trilean.UNKNOWN;
      }
      if (!superClass.get().getDeclaredMethod(name()).isUnknownSymbol()) {
        return Trilean.TRUE;
      }
      superClass = superClass.get().superClass();
    }
    return Trilean.FALSE;
  }

  /**
   * Check all interfaces of a class before searching in the super class
   * to validate if a method has been declared in an implemented interface
   */
  @Override
  public Trilean isImplementing() {
    boolean unknownInterface = false;
    Optional<ClassSymbol> currentClass = Optional.of(owner);
    while (currentClass.isPresent()) {
      if (currentClass.get().isUnknownSymbol()) {
        return Trilean.UNKNOWN;
      }
      Trilean inDeclaredInInterface = findDeclarationInInterfaces(currentClass.get().implementedInterfaces());
      if (inDeclaredInInterface.isTrue()) {
        return Trilean.TRUE;
      }
      if (inDeclaredInInterface.isUnknown()) {
        unknownInterface = true;
      }
      currentClass = currentClass.get().superClass();
    }
    if (unknownInterface) {
      return Trilean.UNKNOWN;
    }
    return Trilean.FALSE;
  }

  /**
   * Loop over all implemented interfaces. Check whether the interface declares the method.
   * If an interface implements other interfaces, check these recursive ones.
   */
  private Trilean findDeclarationInInterfaces(List<ClassSymbol> interfaces) {
    boolean unknownSymbol = false;
    for (ClassSymbol interfaceSymbol : interfaces) {
      if (interfaceSymbol.isUnknownSymbol()) {
        unknownSymbol = true;
      }
      if (!interfaceSymbol.getDeclaredMethod(name()).isUnknownSymbol()) {
        return Trilean.TRUE;
      }

      Trilean inSuperClass = findDeclarationInInterfaces(interfaceSymbol.implementedInterfaces());
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
