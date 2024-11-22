/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.api.symbols.QualifiedName;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

class ProjectSymbolDataTest {

  @Test
  void add() {
    QualifiedName qualifiedName = qualifiedName("ns1\\Class1");
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();

    assertThat(projectSymbolData.classSymbolData(qualifiedName)).isEmpty();

    ClassSymbolData class1 = new ClassSymbolData(new LocationInFileImpl("f1", 1, 2, 3, 4), qualifiedName, null, emptyList(), emptyList());
    projectSymbolData.add(class1);
    assertThat(projectSymbolData.classSymbolData(qualifiedName)).contains(class1);
  }

  @Test
  void builtins() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    assertThat(projectSymbolData.classSymbolData(qualifiedName("Exception")).get().implementedInterfaces()).containsExactly(qualifiedName("Throwable"));

    ClassSymbolData myException = new ClassSymbolData(new LocationInFileImpl("f1", 1, 2, 3, 4), qualifiedName("Exception"), null, emptyList(), emptyList());
    projectSymbolData.add(myException);
    assertThat(projectSymbolData.classSymbolData(qualifiedName("Exception"))).contains(myException);
  }
}
