/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class LineVisitorTest extends ParsingTestUtils {

  @Test
  public void test_lines_of_code_number() throws Exception {
    LineVisitor lineVisitor = new LineVisitor(parse("metrics/lines_of_code.php"));
    assertThat(lineVisitor.getLinesOfCodeNumber()).isEqualTo(7);
  }

  @Test
  public void test_lines_of_code() throws Exception {
    LineVisitor lineVisitor = new LineVisitor(parse("metrics/lines_of_code.php"));
    Set<Integer> linesOfCode = lineVisitor.getLinesOfCode();
    assertThat(linesOfCode).hasSize(7);
    assertThat(linesOfCode).contains(13, 17, 19, 20, 21, 22, 23);
  }

}
