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
package org.sonar.plugins.php.api.visitors;

import org.junit.Test;
import org.sonar.php.utils.DummyCheck;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class LineIssueTest {


  private static final PHPCheck CHECK = new DummyCheck();

  @Test
  public void test() throws Exception {
    LineIssue lineIssue = new LineIssue(CHECK, 42, "Test message");

    assertThat(lineIssue.check()).isEqualTo(CHECK);
    assertThat(lineIssue.cost()).isNull();
    assertThat(lineIssue.line()).isEqualTo(42);
    assertThat(lineIssue.message()).isEqualTo("Test message");
  }

  @Test
  public void with_cost() throws Exception {
    LineIssue lineIssue = new LineIssue(CHECK, 42, "Test message").cost(5);

    assertThat(lineIssue.check()).isEqualTo(CHECK);
    assertThat(lineIssue.cost()).isEqualTo(5);
    assertThat(lineIssue.line()).isEqualTo(42);
    assertThat(lineIssue.message()).isEqualTo("Test message");
  }
}
