/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.List;
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
}
