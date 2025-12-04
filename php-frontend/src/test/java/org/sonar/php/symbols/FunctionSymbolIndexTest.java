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

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.sonar.php.tree.symbols.SymbolReturnType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

class FunctionSymbolIndexTest {

  @Test
  void globalFunction() {
    FunctionSymbolData a = data("a");
    FunctionSymbolIndex index = index(a);

    FunctionSymbol result = index.get(qualifiedName("a"));
    assertThat(result).isInstanceOf(FunctionSymbol.class);
    assertThat(result.isUnknownSymbol()).isFalse();
  }

  @Test
  void namespaceFunction() {
    FunctionSymbolData a = data("\\namespace\\a");
    FunctionSymbolIndex index = index(a);

    FunctionSymbol result = index.get(qualifiedName("\\namespace\\a"));
    assertThat(result.isUnknownSymbol()).isFalse();

    result = index.get(qualifiedName("a"));
    assertThat(result.isUnknownSymbol()).isTrue();
  }

  @Test
  void globalAndNamespaceFunction() {
    FunctionSymbolData globalA = data("a");
    FunctionSymbolData namespaceA = data("\\namespace\\a");
    FunctionSymbolIndex index = index(globalA, namespaceA);

    FunctionSymbol namespaceResult = index.get(qualifiedName("\\namespace\\a"));
    assertThat(namespaceResult.isUnknownSymbol()).isFalse();

    FunctionSymbol globalResult = index.get(qualifiedName("a"));
    assertThat(globalResult.isUnknownSymbol()).isFalse();

    assertThat(namespaceResult).isNotEqualTo(globalResult);
  }

  @Test
  void unknownFunction() {
    FunctionSymbolData a = data("a");
    FunctionSymbolIndex index = index(a);

    FunctionSymbol result = index.get(qualifiedName("unknown"));
    assertThat(result.isUnknownSymbol()).isTrue();
    assertThat(result.location()).isInstanceOf(UnknownLocationInFile.class);
    assertThat(result.hasReturn()).isFalse();
    assertThat(result.hasFuncGetArgs()).isFalse();
    assertThat(result.returnType().isPresent()).isFalse();
    assertThat(result.returnType().isVoid()).isFalse();
  }

  private FunctionSymbolData data(String name) {
    return new FunctionSymbolData(someLocation(), qualifiedName(name), new ArrayList<>(), new FunctionSymbolData.FunctionSymbolProperties(), SymbolReturnType.notDefined());
  }

  private FunctionSymbolIndex index(FunctionSymbolData... data) {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    for (FunctionSymbolData f : data) {
      projectSymbolData.add(f);
    }
    return FunctionSymbolIndex.create(Arrays.asList(data), projectSymbolData);
  }

  private LocationInFileImpl someLocation() {
    return new LocationInFileImpl("path", 1, 0, 1, 3);
  }
}
