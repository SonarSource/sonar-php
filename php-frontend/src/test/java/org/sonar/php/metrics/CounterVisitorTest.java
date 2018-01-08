/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.metrics;

import org.junit.Test;
import org.sonar.php.ParsingTestUtils;

import static org.assertj.core.api.Assertions.assertThat;


public class CounterVisitorTest extends ParsingTestUtils {

  @Test
  public void test_class() throws Exception {
    CounterVisitor counterVisitor = new CounterVisitor(parse("metrics/classes.php"));
    assertThat(counterVisitor.getClassNumber()).isEqualTo(4);
  }

  @Test
  public void test_statements() throws Exception {
    CounterVisitor counterVisitor = new CounterVisitor(parse("metrics/statements.php"));
    assertThat(counterVisitor.getStatementNumber()).isEqualTo(29);
  }

  @Test
  public void test_functions() throws Exception {
    CounterVisitor counterVisitor = new CounterVisitor(parse("metrics/functions.php"));
    assertThat(counterVisitor.getFunctionNumber()).isEqualTo(4);
  }

}
