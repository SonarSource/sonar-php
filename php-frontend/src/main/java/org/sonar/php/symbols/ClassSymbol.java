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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public interface ClassSymbol extends Symbol {

  LocationInFile location();

  QualifiedName qualifiedName();

  Optional<ClassSymbol> superClass();

  Set<ClassSymbol> allSuperTypes();

  List<ClassSymbol> implementedInterfaces();

  Trilean isOrSubClassOf(QualifiedName qualifiedName);

  Trilean isSubTypeOf(QualifiedName... typeName);

  List<MethodSymbol> declaredMethods();

  MethodSymbol getDeclaredMethod(String name);

  boolean is(Kind kind);

  enum Kind {
    NORMAL,
    ABSTRACT,
    INTERFACE
  }
}
