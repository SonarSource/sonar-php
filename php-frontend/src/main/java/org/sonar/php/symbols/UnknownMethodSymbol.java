/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import org.sonar.plugins.php.api.symbols.QualifiedName;

public class UnknownMethodSymbol extends UnknownFunctionSymbol implements MethodSymbol {
  private final String name;

  public UnknownMethodSymbol(String name) {
    super(QualifiedName.qualifiedName(name));
    this.name = name;
  }

  @Override
  public Visibility visibility() {
    return Visibility.PUBLIC;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Trilean isOverriding() {
    return Trilean.UNKNOWN;
  }

  @Override
  public Trilean isAbstract() {
    return Trilean.UNKNOWN;
  }

  @Override
  public Trilean isTestMethod() {
    return Trilean.UNKNOWN;
  }

  @Override
  public ClassSymbol owner() {
    return new UnknownClassSymbol(QualifiedName.qualifiedName("unknown"));
  }
}
