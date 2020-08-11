package org.sonar.php.symbols;

import org.sonar.plugins.php.api.tree.declaration.ParameterTree;

import javax.annotation.Nullable;

public class Parameter {
  private final String name;
  private final String type;
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

    return new Parameter(
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
