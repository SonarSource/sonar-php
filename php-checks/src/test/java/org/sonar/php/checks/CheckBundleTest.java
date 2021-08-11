/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.checks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckBundleTest {

  private static final PhpFile FILE = mock(PhpFile.class);
  private static final CompilationUnitTree UNIT_TREE = mock(CompilationUnitTree.class);
  private static final SymbolTable TABLE = mock(SymbolTable.class);

  @Test
  public void test_bundle_part_analysis() {
    CheckBundle bundle = new TestCheckBundle();
    assertThat(bundle.analyze(FILE, UNIT_TREE, TABLE)).hasSize(3);
  }

  // Bundle which contains out of 3 identical parts which all raise a single dummy issue
  static class TestCheckBundle extends CheckBundle {

    @Override
    protected List<PHPCheck> checks() {
      PHPCheck checkPart = mock(PHPVisitorCheck.class);
      when(checkPart.analyze(any(PhpFile.class), any(CompilationUnitTree.class), any(SymbolTable.class))).thenReturn(
        Collections.singletonList(mock(PhpIssue.class))
      );
      return Arrays.asList(checkPart, checkPart, checkPart);
    }
  }
}
