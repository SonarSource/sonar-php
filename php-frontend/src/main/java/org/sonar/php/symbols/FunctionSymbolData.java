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

import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.visitors.LocationInFile;

import javax.annotation.Nullable;
import java.util.List;

public class FunctionSymbolData {
  private final LocationInFile location;
  private final QualifiedName qualifiedName;
  private final List<Parameter> parameters;

  public FunctionSymbolData(LocationInFile location, QualifiedName qualifiedName, List<Parameter> parameters) {
    this.location = location;
    this.qualifiedName = qualifiedName;
    this.parameters = parameters;
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

  public static class Parameter {
    private final String name;
    private final String type; // TODO: create and use a Type class
    private final boolean hasDefault;
    private final boolean isReference;
    private final boolean hasEllipsisOperator;

    public Parameter(String name, @Nullable String type, boolean hasDefault, boolean isReference, boolean hasEllipsisOperator) {
      this.type = type;
      this.name = name;
      this.hasDefault = hasDefault;
      this.isReference = isReference;
      this.hasEllipsisOperator = hasEllipsisOperator;
    }

    public static Parameter fromTree(ParameterTree parameter) {
      String parameterName = parameter.variableIdentifier().text();
      String parameterType = parameter.type() != null ? parameter.type().toString() : null;

      return new FunctionSymbolData.Parameter(
        parameterName,
        parameterType,
        parameter.initValue() != null,
        parameter.referenceToken() != null,
        parameter.ellipsisToken() != null
      );
    }

    public String name() {
      return name;
    }

    public String type() {
      return type;
    }

    public boolean hasDefault() {
      return hasDefault;
    }

    public boolean isReference() {
      return isReference;
    }

    public boolean hasEllipsisOperator() {
      return hasEllipsisOperator;
    }
  }
}
