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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sonar.plugins.php.api.symbols.QualifiedName;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.sonar.php.symbols.UnknownLocationInFile.UNKNOWN_LOCATION;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

public enum BuiltinSymbolData {

  BUILTINS;

  private final Map<QualifiedName, ClassSymbolData> classSymbolsByQualifiedName = init();

  private Map<QualifiedName, ClassSymbolData> init() {
    List<ClassSymbolData> data = Arrays.asList(
      new ClassSymbolData(UNKNOWN_LOCATION, qualifiedName("Throwable"), null, emptyList(), ClassSymbol.Kind.ABSTRACT, Collections.emptyList()),
      new ClassSymbolData(UNKNOWN_LOCATION, qualifiedName("Exception"), null, singletonList(qualifiedName("Throwable")), Collections.emptyList()),
      new ClassSymbolData(UNKNOWN_LOCATION, qualifiedName("RuntimeException"), qualifiedName("Exception"), emptyList(), Collections.emptyList()));
    return data.stream().collect(Collectors.toMap(ClassSymbolData::qualifiedName, a -> a));
  }

  public Optional<ClassSymbolData> classSymbolData(QualifiedName qualifiedName) {
    return Optional.ofNullable(classSymbolsByQualifiedName.get(qualifiedName));
  }

}
