/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

public class FunctionSymbolIndexTest {

  @Test
  public void global_function() {
    FunctionSymbolData a = data("a");
    FunctionSymbolIndex index = index(a);

    FunctionSymbol result = index.get(qualifiedName("a"));
    assertThat(result).isInstanceOf(FunctionSymbol.class);
    assertThat(result.isUnknownSymbol()).isFalse();
  }

  @Test
  public void namespace_function() {
    FunctionSymbolData a = data("\\namespace\\a");
    FunctionSymbolIndex index = index(a);

    FunctionSymbol result = index.get(qualifiedName("\\namespace\\a"));
    assertThat(result.isUnknownSymbol()).isFalse();

    result = index.get(qualifiedName("a"));
    assertThat(result.isUnknownSymbol()).isTrue();
  }

  @Test
  public void global_and_namespace_function() {
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
  public void unknown_function() {
    FunctionSymbolData a = data("a");
    FunctionSymbolIndex index = index(a);

    FunctionSymbol result = index.get(qualifiedName("unknown"));
    assertThat(result.isUnknownSymbol()).isTrue();
    assertThat(result.location()).isInstanceOf(UnknownLocationInFile.class);
    assertThat(result.hasReturn()).isFalse();
    assertThat(result.hasFuncGetArgs()).isFalse();
  }

  private FunctionSymbolData data(String name) {
    return new FunctionSymbolData(someLocation(), qualifiedName(name), new ArrayList<>(), new FunctionSymbolData.FunctionSymbolProperties());
  }

  private FunctionSymbolIndex index(FunctionSymbolData... data) {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    for (FunctionSymbolData f : data) {
      projectSymbolData.add(f);
    }
    return FunctionSymbolIndex.create(new ArrayList<>(Arrays.asList(data)), projectSymbolData);
  }

  private LocationInFileImpl someLocation() {
    return new LocationInFileImpl("path", 1, 0, 1, 3);
  }
}
