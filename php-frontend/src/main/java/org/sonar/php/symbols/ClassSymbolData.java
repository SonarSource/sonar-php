/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class ClassSymbolData {

  private final LocationInFile location;
  private final QualifiedName qualifiedName;
  @Nullable
  private final QualifiedName superClass;
  private final List<QualifiedName> implementedInterfaces;
  private final ClassSymbol.Kind kind;
  private final List<MethodSymbolData> methods;

  public ClassSymbolData(LocationInFile location, QualifiedName qualifiedName, @Nullable QualifiedName superClass,
    List<QualifiedName> implementedInterfaces, ClassSymbol.Kind kind, List<MethodSymbolData> methods) {
    this.location = location;
    this.qualifiedName = qualifiedName;
    this.superClass = superClass;
    this.implementedInterfaces = implementedInterfaces;
    this.kind = kind;
    this.methods = methods;
  }

  public ClassSymbolData(LocationInFile location, QualifiedName qualifiedName, @Nullable QualifiedName superClass,
    List<QualifiedName> implementedInterfaces, List<MethodSymbolData> methods) {
    this(location, qualifiedName, superClass, implementedInterfaces, ClassSymbol.Kind.NORMAL, methods);
  }

  public LocationInFile location() {
    return location;
  }

  public QualifiedName qualifiedName() {
    return qualifiedName;
  }

  public Optional<QualifiedName> superClass() {
    return Optional.ofNullable(superClass);
  }

  public List<QualifiedName> implementedInterfaces() {
    return implementedInterfaces;
  }

  public List<MethodSymbolData> methods() {
    return methods;
  }

  public ClassSymbol.Kind kind() {
    return kind;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ClassSymbolData that = (ClassSymbolData) o;
    return Objects.equals(location, that.location) && Objects.equals(qualifiedName, that.qualifiedName) && Objects.equals(superClass, that.superClass) && Objects.equals(implementedInterfaces, that.implementedInterfaces) && kind == that.kind && Objects.equals(methods, that.methods);
  }

  @Override
  public int hashCode() {
    return Objects.hash(location, qualifiedName, superClass, implementedInterfaces, kind, methods);
  }

  @Override
  public String toString() {
    return "ClassSymbolData{" +
      "location=" + location +
      ", qualifiedName=" + qualifiedName +
      ", superClass=" + superClass +
      ", implementedInterfaces=" + implementedInterfaces +
      ", kind=" + kind +
      ", methods=" + methods +
      '}';
  }
}
