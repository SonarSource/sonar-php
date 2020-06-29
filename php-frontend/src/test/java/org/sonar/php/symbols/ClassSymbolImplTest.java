/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

public class ClassSymbolImplTest {

  @Test
  public void class_without_superclass() {
    ClassSymbolData a = data("ns1\\a");
    ClassSymbolData b = data("ns1\\b");
    Map<ClassSymbolData, ClassSymbol> symbols = createSymbols(projectData(), a, b);
    assertThat(symbols.get(a).superClass()).isEmpty();
    assertThat(symbols.get(b).superClass()).isEmpty();
  }

  @Test
  public void superclass_in_current_file() {
    ClassSymbolData a = data("ns1\\a");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    Map<ClassSymbolData, ClassSymbol> symbols = createSymbols(projectData(), a, b);
    assertThat(symbols.get(a).superClass()).isEmpty();
    assertThat(symbols.get(b).superClass()).containsSame(symbols.get(a));
  }

  @Test
  public void superclass_outside_current_file() {
    ClassSymbolData a = data("ns1\\a");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    Map<ClassSymbolData, ClassSymbol> symbols = createSymbols(projectData(a), b);
    assertThat(symbols.get(a)).isNull();
    assertThat(symbols.get(b).superClass().get().qualifiedName()).isEqualTo(a.qualifiedName());
  }

  @Test
  public void two_classes_with_same_superclass_outside_current_file() {
    ClassSymbolData a = data("ns1\\a");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    ClassSymbolData c = data("ns1\\c", "ns1\\a");
    Map<ClassSymbolData, ClassSymbol> symbols = createSymbols(projectData(a), b, c);
    assertThat(symbols.get(a)).isNull();
    assertThat(symbols.get(b).superClass()).containsSame(symbols.get(c).superClass().get());
  }

  @Test
  public void superclass_of_superclass_outside_current_file() {
    ClassSymbolData a = data("ns1\\a");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    ClassSymbolData c = data("ns1\\c", "ns1\\b");
    Map<ClassSymbolData, ClassSymbol> symbols = createSymbols(projectData(a, b), c);
    ClassSymbol cSuperClass = symbols.get(c).superClass().get();
    assertThat(cSuperClass.qualifiedName()).isEqualTo(b.qualifiedName());
    assertThat(cSuperClass.superClass().get().qualifiedName()).isEqualTo(a.qualifiedName());
  }

  @Test
  public void cycle_between_classes_in_current_file() {
    ClassSymbolData a = data("ns1\\a", "ns1\\c");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    ClassSymbolData c = data("ns1\\c", "ns1\\b");
    Map<ClassSymbolData, ClassSymbol> symbols = createSymbols(projectData(), a, b, c);
    assertThat(symbols.get(a).superClass()).containsSame(symbols.get(c));
    assertThat(symbols.get(b).superClass()).containsSame(symbols.get(a));
    assertThat(symbols.get(c).superClass()).containsSame(symbols.get(b));
  }

  @Test
  public void cycle_between_classes_outside_current_file() {
    ClassSymbolData a = data("ns1\\a", "ns1\\b");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    ClassSymbolData c = data("ns1\\c", "ns1\\a");
    Map<ClassSymbolData, ClassSymbol> symbols = createSymbols(projectData(a, b), c);
    ClassSymbol cSuperClass = symbols.get(c).superClass().get();
    assertThat(cSuperClass.superClass().get().superClass()).containsSame(cSuperClass);
  }

  @Test
  public void unknown_super_class() {
    ClassSymbolData a = data("ns1\\a", "ns1\\b");
    Optional<ClassSymbol> superClass = createSymbols(projectData(), a).get(a).superClass();
    assertThat(superClass).isNotEmpty();
    assertThat(superClass.get().isUnknownSymbol()).isTrue();
  }

  private Map<ClassSymbolData, ClassSymbol> createSymbols(ProjectSymbolData projectData, ClassSymbolData... data) {
    Map<ClassSymbolData, ClassSymbol> result = ClassSymbolImpl.createSymbols(new HashSet<>(Arrays.asList(data)), projectData);
    assertThat(result).hasSameSizeAs(data);
    assertThat(result).containsKeys(data);
    for (ClassSymbolData d : data) {
      assertThat(result.get(d).qualifiedName()).isEqualTo(d.qualifiedName());
      assertThat(result.get(d).isUnknownSymbol()).isFalse();
    }
    return result;
  }

  private ProjectSymbolData projectData(ClassSymbolData... data) {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    for (ClassSymbolData d : data) {
      projectSymbolData.add(d);
    }
    return projectSymbolData;
  }

  private ClassSymbolData data(String fqn) {
    return new ClassSymbolData(someLocation(), qualifiedName(fqn), null);
  }

  private ClassSymbolData data(String fqn, String superClassFqn) {
    return new ClassSymbolData(someLocation(), qualifiedName(fqn), qualifiedName(superClassFqn));
  }

  private LocationInFileImpl someLocation() {
    return new LocationInFileImpl("path", 1, 0, 1, 3);
  }
}
