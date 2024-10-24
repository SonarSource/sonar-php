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

import java.util.List;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.ReturnType;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class FunctionSymbolData {
  private final LocationInFile location;
  private final QualifiedName qualifiedName;
  private final List<Parameter> parameters;
  private final FunctionSymbolProperties properties;
  private final ReturnType returnType;

  public FunctionSymbolData(LocationInFile location, QualifiedName qualifiedName, List<Parameter> parameters, FunctionSymbolProperties properties, ReturnType returnType) {
    this.location = location;
    this.qualifiedName = qualifiedName;
    this.parameters = parameters;
    this.properties = properties;
    this.returnType = returnType;
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

  public ReturnType returnType() {
    return returnType;
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
  }
}
