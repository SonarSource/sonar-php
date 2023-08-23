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
package org.sonar.php.tree.symbols;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;

import static org.assertj.core.api.Assertions.assertThat;

class MemberQualifiedNameTest {

  @Test
  void testEqual() {
    SymbolQualifiedName classA = SymbolQualifiedName.create("n", "A");
    MemberQualifiedName name1 = new MemberQualifiedName(classA, "foo", Kind.FUNCTION);
    MemberQualifiedName name2 = new MemberQualifiedName(SymbolQualifiedName.create("n", "a"), "foo", Kind.FUNCTION);
    assertThat(name1).isEqualTo(name2);
  }

  @Test
  void testNotEqual() {
    SymbolQualifiedName classA = SymbolQualifiedName.create("n", "A");
    MemberQualifiedName name1 = new MemberQualifiedName(classA, "bar", Kind.FUNCTION);
    MemberQualifiedName name2 = new MemberQualifiedName(SymbolQualifiedName.create("n", "a"), "foo", Kind.FUNCTION);
    assertThat(name1).isNotEqualTo(name2).isNotEqualTo(SymbolQualifiedName.create("n", "a", "bar"));
  }

  @Test
  void testCaseSensitiveMembers() {
    SymbolQualifiedName classA = SymbolQualifiedName.create("n", "A");
    MemberQualifiedName functionName1 = new MemberQualifiedName(classA, "Foo", Kind.FUNCTION);
    MemberQualifiedName functionName2 = new MemberQualifiedName(classA, "foo", Kind.FUNCTION);
    MemberQualifiedName fieldName1 = new MemberQualifiedName(classA, "Bar", Kind.FIELD);
    MemberQualifiedName fieldName2 = new MemberQualifiedName(classA, "bar", Kind.FIELD);
    assertThat(functionName1).isEqualTo(functionName2);
    assertThat(fieldName1).isNotEqualTo(fieldName2);
  }

}
