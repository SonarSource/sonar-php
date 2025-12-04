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

import java.util.Collections;
import java.util.List;
import org.sonar.php.tree.symbols.SymbolReturnType;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.ReturnType;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class UnknownFunctionSymbol implements FunctionSymbol {
  private final QualifiedName qualifiedName;

  public UnknownFunctionSymbol(QualifiedName qualifiedName) {
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
  public boolean hasReturn() {
    return false;
  }

  @Override
  public boolean hasFuncGetArgs() {
    return false;
  }

  @Override
  public List<Parameter> parameters() {
    return Collections.emptyList();
  }

  @Override
  public ReturnType returnType() {
    return SymbolReturnType.notDefined();
  }

  @Override
  public boolean isUnknownSymbol() {
    return true;
  }
}
