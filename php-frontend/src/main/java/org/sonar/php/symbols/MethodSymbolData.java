/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.List;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.ReturnType;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class MethodSymbolData extends FunctionSymbolData {
  private final Visibility visibility;
  private final String name;
  private final boolean isAbstract;
  private final boolean isTestMethod;

  public MethodSymbolData(LocationInFile location,
    String name,
    List<Parameter> parameters,
    FunctionSymbolProperties properties,
    Visibility visibility,
    ReturnType returnType) {
    this(location, name, parameters, properties, visibility, returnType, false, false);
  }

  public MethodSymbolData(LocationInFile location,
    String name,
    List<Parameter> parameters,
    FunctionSymbolProperties properties,
    Visibility visibility,
    ReturnType returnType,
    boolean isAbstract,
    boolean isTestMethod) {
    super(location, QualifiedName.qualifiedName(name), parameters, properties, returnType);
    this.name = name;
    this.visibility = visibility;
    this.isAbstract = isAbstract;
    this.isTestMethod = isTestMethod;
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

  public boolean isTestMethod() {
    return isTestMethod;
  }
}
