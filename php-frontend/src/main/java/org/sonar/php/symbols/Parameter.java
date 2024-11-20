/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.symbols;

import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;

public class Parameter {
  private final String name;
  private final String type;
  private final boolean hasDefault;
  private final boolean hasEllipsisOperator;

  public Parameter(String name, @Nullable String type, boolean hasDefault, boolean hasEllipsisOperator) {
    this.type = type;
    this.name = name;
    this.hasDefault = hasDefault;
    this.hasEllipsisOperator = hasEllipsisOperator;
  }

  public static Parameter fromTree(ParameterTree parameter) {
    String parameterName = parameter.variableIdentifier().text();
    String parameterType = parameter.declaredType() != null ? parameter.declaredType().toString() : null;

    return new Parameter(
      parameterName,
      parameterType,
      parameter.initValue() != null,
      parameter.ellipsisToken() != null);
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

  public boolean hasEllipsisOperator() {
    return hasEllipsisOperator;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    Parameter parameter = (Parameter) other;
    return hasDefault == parameter.hasDefault &&
      hasEllipsisOperator == parameter.hasEllipsisOperator &&
      Objects.equals(name, parameter.name) &&
      Objects.equals(type, parameter.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, hasDefault, hasEllipsisOperator);
  }
}
