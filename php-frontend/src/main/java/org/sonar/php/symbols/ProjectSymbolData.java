/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.sonar.plugins.php.api.symbols.QualifiedName;

/**
 * Instances of this class should never hold references to an AST node: we want to have a low memory usage for the
 * analysis of a project and an AST node basically keeps references to the whole AST of a file.
 */
public class ProjectSymbolData {

  private static final BuiltinSymbolData builtinSymbolData = BuiltinSymbolData.BUILTINS;
  private final Map<QualifiedName, ClassSymbolData> classSymbolsByQualifiedName = new HashMap<>();
  private final Map<QualifiedName, List<FunctionSymbolData>> functionSymbolsByQualifiedName = new HashMap<>();

  public void add(ClassSymbolData classSymbolData) {
    classSymbolsByQualifiedName.put(classSymbolData.qualifiedName(), classSymbolData);
  }

  public void add(FunctionSymbolData functionSymbolData) {
    functionSymbolsByQualifiedName
      .computeIfAbsent(functionSymbolData.qualifiedName(), k -> new ArrayList<>(1))
      .add(functionSymbolData);
  }

  public Optional<ClassSymbolData> classSymbolData(QualifiedName qualifiedName) {
    ClassSymbolData value = classSymbolsByQualifiedName.get(qualifiedName);
    return value == null ? builtinSymbolData.classSymbolData(qualifiedName) : Optional.of(value);
  }

  public List<FunctionSymbolData> functionSymbolData(QualifiedName qualifiedName) {
    return functionSymbolsByQualifiedName.getOrDefault(qualifiedName, Collections.emptyList());
  }

  public Map<QualifiedName, ClassSymbolData> classSymbolsByQualifiedName() {
    return classSymbolsByQualifiedName;
  }

  public Map<QualifiedName, List<FunctionSymbolData>> functionSymbolsByQualifiedName() {
    return functionSymbolsByQualifiedName;
  }
}
