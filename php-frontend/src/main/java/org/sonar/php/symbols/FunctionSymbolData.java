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
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class FunctionSymbolData {
  private final LocationInFile location;
  private final QualifiedName qualifiedName;
  private final List<Parameter> parameters;
  private final FunctionSymbolProperties properties;

  public FunctionSymbolData(LocationInFile location, QualifiedName qualifiedName, List<Parameter> parameters, FunctionSymbolProperties properties) {
    this.location = location;
    this.qualifiedName = qualifiedName;
    this.parameters = parameters;
    this.properties = properties;
  }

  public LocationInFile location() {
    return location;
  }

  public QualifiedName qualifiedName() {
    return qualifiedName;
  }

  public List<Parameter> parameters() {
    return parameters;
  }

  public boolean hasReturn() {
    return properties.hasReturn();
  }

  public boolean hasFuncGetArgs() {
    return properties.hasFuncGetArgs();
  }

  public FunctionSymbolProperties properties() {
    return properties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FunctionSymbolData that = (FunctionSymbolData) o;
    return Objects.equals(location, that.location) && Objects.equals(qualifiedName, that.qualifiedName) && Objects.equals(parameters, that.parameters) && Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(location, qualifiedName, parameters, properties);
  }

  @Override
  public String toString() {
    return "FunctionSymbolData{" +
      "location=" + location +
      ", qualifiedName=" + qualifiedName +
      ", parameters=" + parameters +
      ", properties=" + properties +
      '}';
  }

  public static class FunctionSymbolProperties {
    private boolean hasReturn = false;
    private boolean hasFuncGetArgs = false;

    public FunctionSymbolProperties() {
    }

    public FunctionSymbolProperties(boolean hasReturn, boolean hasFuncGetArgs) {
      this.hasReturn = hasReturn;
      this.hasFuncGetArgs = hasFuncGetArgs;
    }

    public boolean hasReturn() {
      return hasReturn;
    }

    public void hasReturn(boolean hasReturn) {
      this.hasReturn = hasReturn;
    }

    public boolean hasFuncGetArgs() {
      return hasFuncGetArgs;
    }

    public void hasFuncGetArgs(boolean hasFuncGetArgs) {
      this.hasFuncGetArgs = hasFuncGetArgs;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      FunctionSymbolProperties that = (FunctionSymbolProperties) o;
      return hasReturn == that.hasReturn && hasFuncGetArgs == that.hasFuncGetArgs;
    }

    @Override
    public int hashCode() {
      return Objects.hash(hasReturn, hasFuncGetArgs);
    }

    @Override
    public String toString() {
      return "FunctionSymbolProperties{" +
        "hasReturn=" + hasReturn +
        ", hasFuncGetArgs=" + hasFuncGetArgs +
        '}';
    }
  }
}
