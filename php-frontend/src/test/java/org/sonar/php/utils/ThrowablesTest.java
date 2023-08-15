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
package org.sonar.php.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThrowablesTest {

  @Test
  void testGetStackTraceAsString() {
    Exception e = new Exception("My exception");
    assertThat(Throwables.getStackTraceAsString(e))
      .startsWith("java.lang.Exception: My exception")
      .contains("\tat org.sonar.php.utils.ThrowablesTest.testGetStackTraceAsString");
  }

  @Test
  void testGetRootCause() {
    Exception root = new Exception("root");
    Exception child = new Exception("child", root);

    assertThat(Throwables.getRootCause(child)).isSameAs(root);
    assertThat(Throwables.getRootCause(root)).isSameAs(root);
  }

  @Test
  void testGetRootCauseLoop() {
    Exception recursive = mock(Exception.class);
    Exception child = new Exception("child", recursive);
    when(recursive.getCause()).thenReturn(recursive);

    assertThat(Throwables.getRootCause(child)).isSameAs(recursive);
  }
}
