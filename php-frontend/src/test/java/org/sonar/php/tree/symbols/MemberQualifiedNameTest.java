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
package org.sonar.php.tree.symbols;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberQualifiedNameTest {

  @Test
  public void test_equal() {
    SymbolQualifiedName classA = SymbolQualifiedName.create("n", "A");
    MemberQualifiedName name1 = new MemberQualifiedName(classA, "foo");
    MemberQualifiedName name2 = new MemberQualifiedName(SymbolQualifiedName.create("n", "a"), "foo");
    assertThat(name1).isEqualTo(name2);
  }

  @Test
  public void test_not_equal() {
    SymbolQualifiedName classA = SymbolQualifiedName.create("n", "A");
    MemberQualifiedName name1 = new MemberQualifiedName(classA, "bar");
    MemberQualifiedName name2 = new MemberQualifiedName(SymbolQualifiedName.create("n", "a"), "foo");
    assertThat(name1).isNotEqualTo(name2);
    assertThat(name1).isNotEqualTo(SymbolQualifiedName.create("n", "a", "bar"));
  }

}
