/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.plugins.php.api.symbols;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QualifiedNameTest {

  @Test
  public void qualifiedName() {
    QualifiedName qualifiedName1 = QualifiedName.qualifiedName("A\\B\\C");
    QualifiedName qualifiedName2 = QualifiedName.qualifiedName("a\\b\\c");
    assertThat(qualifiedName1).isEqualTo(qualifiedName2);
  }
}
