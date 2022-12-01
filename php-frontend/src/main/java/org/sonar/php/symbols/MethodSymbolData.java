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

import java.util.Objects;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

import java.util.List;

public class MethodSymbolData extends FunctionSymbolData {
  private Visibility visibility;
  private String name;
  private boolean isAbstract;

  public MethodSymbolData(LocationInFile location,
    String name,
    List<Parameter> parameters,
    FunctionSymbolProperties properties,
    Visibility visibility) {
    this(location, name, parameters, properties, visibility, false);
  }

  public MethodSymbolData(LocationInFile location,
    String name,
    List<Parameter> parameters,
    FunctionSymbolProperties properties,
    Visibility visibility,
    boolean isAbstract) {
    super(location, QualifiedName.qualifiedName(name), parameters, properties);
    this.name = name;
    this.visibility = visibility;
    this.isAbstract = isAbstract;
  }

  public Visibility visibility() {
    return visibility;
  }

  public String name() {
    return name;
  }

  public boolean isAbstract() {
    return isAbstract;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MethodSymbolData that = (MethodSymbolData) o;
    return isAbstract == that.isAbstract && visibility == that.visibility && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(visibility, name, isAbstract);
  }

  @Override
  public String toString() {
    return "MethodSymbolData{" +
      "visibility=" + visibility +
      ", name='" + name + '\'' +
      ", isAbstract=" + isAbstract +
      "} " + super.toString();
  }
}
