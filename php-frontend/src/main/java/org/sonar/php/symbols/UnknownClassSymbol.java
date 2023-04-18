/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

class UnknownClassSymbol extends AbstractClassSymbol {

  private final QualifiedName qualifiedName;

  UnknownClassSymbol(QualifiedName qualifiedName) {
    this.qualifiedName = qualifiedName;
  }

  @Override
  public LocationInFile location() {
    return UnknownLocationInFile.UNKNOWN_LOCATION;
  }

  @Override
  public QualifiedName qualifiedName() {
    return qualifiedName;
  }

  @Override
  public Optional<ClassSymbol> superClass() {
    return Optional.empty();
  }

  @Override
  public List<ClassSymbol> implementedInterfaces() {
    return Collections.emptyList();
  }

  @Override
  public List<MethodSymbol> declaredMethods() {
    return Collections.emptyList();
  }

  @Override
  public MethodSymbol getDeclaredMethod(String name) {
    return new UnknownMethodSymbol(qualifiedName() + "::" + name);
  }

  @Override
  public boolean isUnknownSymbol() {
    return true;
  }

  @Override
  public boolean is(Kind kind) {
    return false;
  }
}
