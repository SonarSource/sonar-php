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
package org.sonar.php.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class StringTableTest {

  @Test
  public void test() {
    StringTable stringTable = new StringTable();
    assertEquals(0, stringTable.getIndex("a0"));
    assertEquals(1, stringTable.getIndex("a1"));
    assertEquals(2, stringTable.getIndex("a2"));

    assertThrows(IndexOutOfBoundsException.class, () -> stringTable.getString(3));
    assertEquals("a0", stringTable.getString(0));
    assertEquals("a2", stringTable.getString(2));
    assertEquals("a1", stringTable.getString(1));
  }

  @Test
  public void test_create_from_list() {
    List<String> list = new ArrayList<>(Arrays.asList("a0", "a1", "a2"));
    StringTable stringTable = new StringTable(list);
    stringTable.getIndex("a3");

    assertThrows(IndexOutOfBoundsException.class, () -> stringTable.getString(4));
    assertEquals("a0", stringTable.getString(0));
    assertEquals("a2", stringTable.getString(2));
    assertEquals("a1", stringTable.getString(1));
    assertEquals("a3", stringTable.getString(3));
  }
}
